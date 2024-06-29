package com.polarbookshop.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    // 사용자 별 사용률 제한
    @Bean
    public KeyResolver keyResolver(){
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous");
    }
}
