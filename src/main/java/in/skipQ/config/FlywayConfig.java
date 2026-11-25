package in.skipQ.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Executing Flyway repair to fix any failed migration records and reconcile checksums...");
            try {
                flyway.repair();
            } catch (Exception e) {
                log.warn("Flyway repair warning: {}", e.getMessage());
            }
            log.info("Executing Flyway migrate...");
            flyway.migrate();
            log.info("Flyway migration completed successfully.");
        };
    }
}
