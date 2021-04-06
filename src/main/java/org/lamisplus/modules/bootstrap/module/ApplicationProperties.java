package org.lamisplus.modules.bootstrap.module;

import com.foreach.across.core.annotations.Exposed;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "lamis", ignoreUnknownFields = false)
@Configuration
@Exposed
@Getter
@Setter
public class ApplicationProperties {
    private String modulePath = "modules";
    private String databaseDir;
    private String tempDir;
    private String serverUrl;
}
