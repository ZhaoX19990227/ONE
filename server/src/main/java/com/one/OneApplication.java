package com.one;

import com.one.config.OneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(OneProperties.class)
public class OneApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneApplication.class, args);
    }
}
