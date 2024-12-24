package aistudio.data.domain.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.rest.uris}")
    private String elasticsearchUris;

    @Bean
    public RestClient restClient() {
        log.info("elasticsearchUris in RestClient bean: {}", elasticsearchUris);
        
        String uriWithoutProtocol = elasticsearchUris.replace("http://", "");
        String[] uriParts = uriWithoutProtocol.split(":");

        String hostname = uriParts[0];
        int port = (uriParts.length > 1) ? Integer.parseInt(uriParts[1]) : 9200;

        return RestClient.builder(
                new HttpHost(hostname, port, "http")
        ).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return new ElasticsearchClient(
            new RestClientTransport(restClient, new JacksonJsonpMapper())
        );
    }
}
