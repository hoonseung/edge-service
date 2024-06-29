package com.polarbookshop.edgeservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain config(ServerHttpSecurity http,
                                         ReactiveClientRegistrationRepository clientRegistrationRepository){
        return http.authorizeExchange(exchange -> exchange
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/books/**")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                        new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                ))
                .oauth2Login(Customizer.withDefaults())
                .logout(logoutSpec -> logoutSpec.logoutSuccessHandler(oidLogoutSuccessHandler(clientRegistrationRepository)))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler((exchange, csrfToken) ->
                                new ServerCsrfTokenRequestAttributeHandler().handle(exchange, csrfToken))
                ).build();

    }

    // 키클록으로 로그아웃 요청 후 완료 시 애플리케이션 리다이렉트 설정
    private ServerLogoutSuccessHandler oidLogoutSuccessHandler(
            ReactiveClientRegistrationRepository
                    clientRegistrationRepository){
        var oidLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}"); // {baseUrl} 스프링이 동적으로 지정하는 url
        return oidLogoutSuccessHandler;
    }

    // Cookie 에 csrf 토큰을 담아 전송하고 있으나 리액티브 스트림을 활성화하기 위해서는 구독이 필요하다
    // CookieServerCsrfTokenRepository 는 구독을 보장하지 않으므로 따로 필터를 만들어서 csrf 리액티브 스트림을 구독하여 쿠키를 추출하도록한다.
    @Bean
    public WebFilter csrfWebFilter(){
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
                Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
                return csrfToken != null ? csrfToken.then() : Mono.empty();
            }));
            return chain.filter(exchange);
        };
    }

    // 웹 세션에 엑세스 토큰틀 저장하는 구현 클래스
    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository(){
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }





}
