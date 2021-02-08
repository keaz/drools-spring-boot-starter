package com.keta.rule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "keta.rule")
@Configuration
public class ConfigData {

    private String clonedDirectory;
    private String gitUrl;
    private String gitUserName;
    private String gitToken;
    private String gitBranch;
    private String directory;
    private ClusterType clusterType;

}
