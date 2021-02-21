package com.keta.rule.config;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.controller.RuleController;
import com.keta.rule.service.GitService;
import com.keta.rule.service.RuleService;
import com.keta.rule.service.Session;
import com.keta.rule.service.impl.DroolsSessionImpl;
import com.keta.rule.service.impl.GitServiceImpl;
import com.keta.rule.service.impl.RuleServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@Log4j2
@Configuration
@EnableConfigurationProperties({ConfigData.class})
@EnableScheduling
@EnableAsync
@AutoConfigureAfter({JDBCConfiguration.class})
public class RuleConfiguration {

    @PostConstruct
    public void init() {
        log.info("Initializing RuleConfiguration..");
    }

    @Bean
    @DependsOn({"gitService"})
    public Session droolsSession(ConfigData configData, GitService gitService, ClusterManager clusterManager) {
        return new DroolsSessionImpl(configData, gitService, clusterManager);
    }

    @Bean
    public GitService gitService(ConfigData configData) {
        return new GitServiceImpl(configData.getClonedDirectory(), configData.getGitUserName(), configData.getGitToken(),
                configData.getGitUrl(), configData.getGitBranch());
    }

    @Bean
    public RuleService ruleService(Session session) {
        return new RuleServiceImpl(session);
    }

    @Bean
    @DependsOn("ruleService")
    public RuleController ruleController(ClusterManager clusterManager) {
        return new RuleController(clusterManager);
    }

}
