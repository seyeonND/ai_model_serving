package aistudio.data.domain.controller.preprocess;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import aistudio.data.domain.service.common.RedisSubscribeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {
    private final RedisSubscribeService redisSubscribeService;

    @GetMapping("/subscribe")
    public SseEmitter subscribeToProgress(@RequestParam String channel) {
        SseEmitter emitter = new SseEmitter();
        redisSubscribeService.subscribeToChannel(channel, emitter);
        return emitter;
    }

}
