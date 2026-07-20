package com.one.recommendation;

import com.one.config.OneProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
class DeepSeekClientConfiguration {
    @Bean("deepSeekRestClient")
    RestClient deepSeekRestClient(OneProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.deepseek().timeout()).build());
        factory.setReadTimeout(properties.deepseek().timeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
