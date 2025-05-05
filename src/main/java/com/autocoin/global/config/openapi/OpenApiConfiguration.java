package com.autocoin.global.config.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Autocoin API",
        version = "v1.0.0",
        description = "Autocoin 프로젝트의 RESTful API 문서",
        contact = @Contact(
            name = "Autocoin Team",
            email = "contact@autocoin.com",
            url = "https://autocoin.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    security = {
        @SecurityRequirement(name = "bearerAuth")
    },
    servers = {
        @Server(url = "/", description = "로컬 서버")
    }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
    )
})
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Autocoin API")
                        .version("v1.0.0")
                        .description("Autocoin 프로젝트의 RESTful API 문서"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
    
    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("1. 인증 API")
                .displayName("인증 API")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2. 사용자 API")
                .displayName("사용자 API")
                .pathsToMatch("/api/v1/users/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
                .group("3. 게시글 API")
                .displayName("게시글 API")
                .pathsToMatch("/api/v1/posts/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("4. 파일 API")
                .displayName("파일 API")
                .pathsToMatch("/api/v1/files/**")
                .build();
    }
}
