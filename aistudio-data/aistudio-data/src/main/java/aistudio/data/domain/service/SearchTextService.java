package aistudio.data.domain.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import aistudio.data.domain.dto.search.SearchTextRequestDTO;
import aistudio.data.domain.service.common.KafkaProducerService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import aistudio.data.domain.service.common.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchTextService {

    private final KafkaProducerService kafkaProducerService;
    private final ElasticsearchClient elasticsearchClient;
    private final RedisService redisService;

    public String searchText(SearchTextRequestDTO searchTextRequestDTO) throws Exception {

        Map<String, String> kafkaMap = new HashMap<>();
        String key = UUID.randomUUID().toString();
        kafkaMap.put("redisKey", key);
        kafkaMap.put("searchText", searchTextRequestDTO.getSearchText());

        ObjectMapper objectMapper = new ObjectMapper();
        String kafkaMessage = objectMapper.writeValueAsString(kafkaMap);

        kafkaProducerService.sendMessage("search-text", kafkaMessage);

        return redisService.getResultByKey(key);
    }

    public boolean deleteData(String fileId) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("data_features")
                    .query(q -> q
                            .term(m -> m
                                    .field("file_id")
                                    .value(fileId))));

            SearchResponse<Object> searchResponse = elasticsearchClient.search(searchRequest, Object.class);
            if (searchResponse.hits().total().value() > 0) {
                String documentId = searchResponse.hits().hits().get(0).id();

                DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                        .index("data_features")
                        .id(documentId));

                elasticsearchClient.delete(deleteRequest);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Error executing search query", e);
            return false;
        }
    }

}
