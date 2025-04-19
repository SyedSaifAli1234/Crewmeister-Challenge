package com.crewmeister.cmcodingchallenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Crewmeister FX Rate API")
                        .version("1.0")
                        .description("API for fetching and converting foreign exchange rates against EUR.")
                        .termsOfService("http://swagger.io/terms/") // Replace with actual terms
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))); // Replace with actual license
    }
} 