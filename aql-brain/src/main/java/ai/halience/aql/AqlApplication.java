package ai.halience.aql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AQL, the Halience brain. Halie fronts it; Settle, GTM and later Build, Acquire and Invest consume it. The brain's
 * behaviour lives in Markdown primitives under {@code brain/}; this application loads, validates, governs and runs them.
 */
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(considerNestedRepositories = true)
public class AqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AqlApplication.class, args);
    }
}
