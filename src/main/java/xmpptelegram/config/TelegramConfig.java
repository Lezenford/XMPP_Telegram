package xmpptelegram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramConfig {
    @NotBlank
    private String token;

    @NotBlank
    private String username;

    @NotBlank
    private String path;

    private String cert;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationConfiguration{");
        sb.append("token='").append(token).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
