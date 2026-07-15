package com.company.resourceallocation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cấu hình metadata cho Swagger UI / OpenAPI (Story 6.1). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resourceAllocationOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Resource Allocation Management System API")
                .version("v1")
                .description("""
                        REST API quản lý phân bổ nhân sự vào dự án.
                        Cưỡng chế Business Rule: BR1 (0 < allocation <= 100),
                        BR2 (tổng allocation mỗi nhân viên <= 100%, pessimistic lock),
                        BR3 (không phân bổ vào dự án COMPLETED).
                        Mọi lỗi trả về theo error envelope chuẩn (code, message, details).
                        """));
    }
}
