package com.one.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";
    private final SessionTokenService tokenService;

    public BearerTokenFilter(SessionTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(PREFIX)) {
            tokenService.verify(authorization.substring(PREFIX.length())).ifPresent(principal -> {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + principal.role().name());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, List.of(authority)));
            });
        }
        filterChain.doFilter(request, response);
    }
}
