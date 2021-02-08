package com.keta.rule.config;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.jgroup.JGroupClusterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jgroups.JChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

@Log4j2
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties({ConfigData.class})
@EnableScheduling
@EnableAsync
@ConditionalOnMissingBean(DataSource.class)
public class JGroupConfiguration {

    private JChannel channel;
    private final ConfigData configData;

    @PostConstruct
    public void init(){
        log.info("Initializing JGroupConfiguration..");
    }

    @Bean
    public JChannel jChannel(){
        try {
            channel = new JChannel("src/main/resources/tcp.xml");
            channel.connect("drools-cluster");
            return channel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ClusterManager clusterManager(JChannel jChannel){
        return new JGroupClusterManager(jChannel);
    }

    @PreDestroy
    public void cleanup(){
        channel.close();
    }
}
