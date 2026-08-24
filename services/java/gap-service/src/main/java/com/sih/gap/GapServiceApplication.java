package com.sih.gap;

import com.sih.shared.tenant.TenantDataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.sih"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = TenantDataSourceConfig.class))
public class GapServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GapServiceApplication.class, args);
    }
}
