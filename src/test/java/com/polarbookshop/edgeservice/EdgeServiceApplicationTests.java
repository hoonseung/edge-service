package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// 레디스 설정이 정상적으로 적용되었는지 테스트
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EdgeServiceApplicationTests {

    private static final int REDIS_PORT = 6379;



    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(REDIS_PORT);



    @DynamicPropertySource
     static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring,redis.port", () -> redis.getMappedPort(REDIS_PORT));
        registry.add("spring.redis.host", () -> redis.getHost());
    }


    @Test
    void verifyThatSpringContextLoads() {
    }

}
