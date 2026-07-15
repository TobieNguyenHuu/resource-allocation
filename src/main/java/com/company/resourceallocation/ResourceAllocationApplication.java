package com.company.resourceallocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi động ứng dụng Resource Allocation Management System.
 *
 * <p>Kiến trúc phân tầng nghiêm ngặt: Controller → Service → Repository.
 * Business Rule (BR1–BR3) chỉ nằm ở Service layer.
 */
@SpringBootApplication
public class ResourceAllocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceAllocationApplication.class, args);
    }
}
