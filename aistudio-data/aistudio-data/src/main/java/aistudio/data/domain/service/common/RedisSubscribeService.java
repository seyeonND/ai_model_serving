package aistudio.data.domain.service.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import aistudio.data.domain.dto.progress.ProgressDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscribeService implements MessageListener {

    private final RedisTemplate<String, Object> template;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = template.getStringSerializer().deserialize(message.getBody());
            ProgressDto progressDto = objectMapper.readValue(publishMessage, ProgressDto.class);
            
            log.info("Redis SUB Message: {}", publishMessage);

            // 메시지를 전송할 대상 SseEmitter 찾기
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                SseEmitter emitter = entry.getValue();
                if (emitter != null) {
                    emitter.send(SseEmitter.event().data(progressDto));  // progressDto를 클라이언트에 전송
                }
            }

        } catch (Exception e) {
            log.error("Error while sending message to client: {}", e.getMessage());
        }
    }

    public void subscribeToChannel(String channelName, SseEmitter emitter) {
        // Emitters에 emitter 추가
        emitters.put(channelName, emitter);

        // Redis 채널에 대한 메시지 리스너를 등록
        listenerContainer.addMessageListener(this, new ChannelTopic(channelName));

        // emitter가 완료되거나 타임아웃될 때 unsubscribe 처리
        emitter.onCompletion(() -> unsubscribeToChannel(channelName));
        emitter.onTimeout(() -> unsubscribeToChannel(channelName));
    }

    public void unsubscribeToChannel(String channelName) {
        listenerContainer.removeMessageListener(this, ChannelTopic.of(channelName));
    }
}
