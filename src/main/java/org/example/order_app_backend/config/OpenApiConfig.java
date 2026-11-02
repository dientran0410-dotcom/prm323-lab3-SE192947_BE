package org.example.order_app_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Food Ordering App API – PRM393 Lab 3")
                        .version("1.0.0")
                        .description("Hệ thống Backend RESTful API phục vụ ứng dụng Đặt món & Gom đơn KTX ĐHQG / FPT. "
                                + "Hỗ trợ chia nhỏ phí giao hàng theo ca, thanh toán VietQR / COD, đối soát nhận hàng bằng QR / PIN, đánh giá và khiếu nại.")
                        .contact(new Contact()
                                .name("PRM393 Lab 3 Engineering Team")
                                .email("contact@fpt.edu.vn"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT / Firebase Token")
                                .description("Nhập Bearer Token hoặc Firebase ID Token để xác thực người dùng.")));
    }
}
