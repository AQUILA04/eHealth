package com.sih.shared.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class TenantRoutingDataSource extends DelegatingDataSource {

    public TenantRoutingDataSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        setupTenantContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // Ignore caller credentials (e.g. Hibernate H2 defaults sa/"") so the
        // pooled PostgreSQL user/password from Spring are always used.
        return getConnection();
    }

    private void setupTenantContext(Connection connection) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            try (Statement stmt = connection.createStatement()) {
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                if ("PostgreSQL".equalsIgnoreCase(dbProduct)) {
                    // PostgreSQL Row-Level Security setting
                    stmt.execute("SET app.current_tenant = '" + tenantId + "'");
                } else {
                    // H2 or other dev/test database support (Session variable)
                    stmt.execute("SET @app_current_tenant = '" + tenantId + "'");
                }
                log.debug("Database session tenant set to '{}' for database product '{}'", tenantId, dbProduct);
            } catch (SQLException e) {
                log.warn("Failed to set database session tenant context: {}", e.getMessage());
            }
        }
    }
}
