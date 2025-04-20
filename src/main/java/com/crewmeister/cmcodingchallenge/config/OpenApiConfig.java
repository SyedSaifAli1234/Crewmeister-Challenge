package com.crewmeister.cmcodingchallenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Foreign Exchange Rate API")
                        .version("1.0")
                        .description("API for retrieving foreign exchange rates from Bundesbank\n\n" +
                                   "### Rate Limiting\n" +
                                   "This API implements rate limiting:\n" +
                                   "* 100 requests per minute per client\n" +
                                   "* Status 429 is returned when rate limit is exceeded")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                .tags(Arrays.asList(
                        new Tag()
                                .name("Exchange Rates")
                                .description("Operations related to EUR exchange rates"),
                        new Tag()
                                .name("Currencies")
                                .description("Operations related to available currencies")
                ));
    }
} 