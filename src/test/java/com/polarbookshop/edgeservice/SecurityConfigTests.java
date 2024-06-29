package com.polarbookshop.edgeservice;

import com.polarbookshop.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(SecurityConfig.class)
@Import(SecurityConfig.class)
 class SecurityConfigTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;


    @Test
    void whenLogoutAuthenticatedAndWithCsrfTokenThen302(){
        Mockito.when(clientRegistrationRepository.findByRegistrationId("test"))
                .thenReturn(Mono.just(testClientRegistration()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockOidcLogin()) // 모의 id 토큰 사용
                .mutateWith(SecurityMockServerConfigurers.csrf()) // csrf 토큰 추가
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isFound(); // 302
    }


    private ClientRegistration testClientRegistration(){
        return ClientRegistration.withRegistrationId("test")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test")
                .authorizationUri("https://sso.polarbookshop.com/auth")
                .tokenUri("https://sso.polarbookshop.com/token")
                .redirectUri("https://polarbookshop.com")
                .build();
    }
}
