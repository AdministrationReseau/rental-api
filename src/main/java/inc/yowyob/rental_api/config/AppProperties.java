package inc.yowyob.rental_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Subscription subscription = new Subscription();
    private File file = new File();

    @Data
    public static class Jwt {
        private String secret;
        private long expiration;
        private long refreshExpiration;
    }

    @Data
    public static class Cors {
        private String[] allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
    }

    @Data
    public static class Subscription {
        private int trialDurationDays;
    }

    @Data
    public static class File {
        private String uploadDir;
        private String maxFileSize;
    }
}

