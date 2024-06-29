package com.polarbookshop.edgeservice;


import com.polarbookshop.edgeservice.config.SecurityConfig;
import com.polarbookshop.edgeservice.user.User;
import com.polarbookshop.edgeservice.user.UserController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
 class UserControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean // 클라이언트 등록에 대한 정보를 가져올 떄 키클록과 실제로 상호작용 x 모의 빈
    ReactiveClientRegistrationRepository clientRegistrationRepository;


    @Test
    void whenNotAuthenticationThen401(){
        webTestClient.get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    void wheAuthenticatedThenReturnUser(){
        webTestClient.mutateWith(configureMockOidLogin(expectedUser()))
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .value(user -> Assertions.assertThat(user).isEqualTo(expectedUser()));
    }





    private User expectedUser(){
        return new User("json.snow", "Jon", "Snow", List.of("employee", "customer"));
    }

    // 모의 id 토큰 생성
    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidLogin(User expectedUser){
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(builder -> {
            builder.claim(StandardClaimNames.PREFERRED_USERNAME, expectedUser.username());
            builder.claim(StandardClaimNames.GIVEN_NAME, expectedUser.firstName());
            builder.claim(StandardClaimNames.FAMILY_NAME, expectedUser.lastName());
            builder.claim("roles", expectedUser.roles());
        });
    }

}
