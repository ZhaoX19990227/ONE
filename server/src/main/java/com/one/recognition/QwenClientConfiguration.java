package com.one.recognition;

import com.one.config.OneProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class QwenClientConfiguration {
    @Bean("qwenRestClient")
    RestClient qwenRestClient(OneProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(properties.qwen().timeout()).build());
        factory.setReadTimeout(properties.qwen().timeout());
        return RestClient.builder().requestFactory(factory).build();
    }
}
