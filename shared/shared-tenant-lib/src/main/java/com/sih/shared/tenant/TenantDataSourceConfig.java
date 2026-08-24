package com.sih.shared.tenant;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Builds Hikari from the fully initialized {@link DataSourceProperties} (env + prod YAML),
 * then wraps it for tenant RLS.
 *
 * <p>A {@code @Configuration} that <em>implements</em> {@code BeanPostProcessor} is created in
 * Spring's early BPP phase and can snapshot {@code username=sa} from {@code application.yml}
 * before {@code SPRING_DATASOURCE_USERNAME} is applied. That made GAP/DPI/tenant authenticate
 * as {@code sa} against Postgres while EMPI (no wrapper) used {@code ehealth} and succeeded.
 */
@AutoConfiguration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnClass(HikariDataSource.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class TenantDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    @ConditionalOnMissingBean(name = "tenantHikariDataSource")
    public HikariDataSource tenantHikariDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(TenantRoutingDataSource.class)
    public DataSource dataSource(HikariDataSource tenantHikariDataSource) {
        return new TenantRoutingDataSource(tenantHikariDataSource);
    }
}
