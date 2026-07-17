package com.one.security;

import com.one.common.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter tokenFilter,
                                            ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/health", "/auth/wechat", "/auth/admin", "/actuator/health/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/activities/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("MODERATOR", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, error) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), ApiError.of("UNAUTHORIZED", "请先登录"));
                }))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
