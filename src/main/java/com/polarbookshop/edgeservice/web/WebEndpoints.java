package com.polarbookshop.edgeservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

// 서킷 브레이커 폴백 엔드 포인트
@Configuration
public class WebEndpoints {


    // 함수형 REST 엔드포인트가 빈 내부에서 정의
    // 실제로는 클라이언트가 처리할 수 있도록 사용자 예외를 발생하던지 원래 요청에 대한 마지막 캐시값을 반환하는등 다양한 전략이 있다.
    @Bean
    public RouterFunction<ServerResponse> routerFunction(){
        return RouterFunctions.route()
                .GET("/catalog-fallback", request
                        -> ServerResponse.ok().body(Mono.just(""), String.class)) // 폴백 정의
                .POST("/catalog-fallback", request
                        -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build())
                .build();
    }
}
