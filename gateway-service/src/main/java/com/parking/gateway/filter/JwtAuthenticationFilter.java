package com.parking.gateway.filter;

import com.parking.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Global Filter
 * Validates JWT tokens for all requests except whitelisted paths
 *
 * @author Parking Management System
 * @version 1.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    @Value("#{'${auth.whitelist}'.split(',')}")
    private List<String> whitelist;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("【Gateway Filter】Processing request: {}", path);

        // Check if path is in whitelist
        if (isWhitelisted(path)) {
            log.debug("【Gateway Filter】Path is whitelisted: {}", path);
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Check if Authorization header exists
        if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
            log.warn("【Gateway Filter】Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(tokenPrefix.length() + 1);

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            log.warn("【Gateway Filter】Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract username from token
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            log.warn("【Gateway Filter】Failed to extract username from token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Add username to request header for downstream services
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Name", username)
                .build();

        log.info("【Gateway Filter】JWT validation successful for user: {} on path: {}", username, path);

        // Continue with the mutated request
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * Check if the path is in the whitelist
     *
     * @param path the request path
     * @return true if whitelisted, false otherwise
     */
    private boolean isWhitelisted(String path) {
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set filter order (execute early in the filter chain)
     *
     * @return order value (-100 for high priority)
     */
    @Override
    public int getOrder() {
        return -100;
    }

}
