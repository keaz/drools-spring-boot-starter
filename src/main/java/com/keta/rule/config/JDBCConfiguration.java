package com.keta.rule.config;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.jdbc.JDBCClusterManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;


@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ConfigData.class})
@ConditionalOnBean(DataSource.class)
@ComponentScan("com.keta.rule.cluster.jdbc")
@ConditionalOnProperty(prefix = "keta.rule", name = "cluster-type", havingValue = "jdbc", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class JDBCConfiguration {

    @PostConstruct
    public void init() {
        log.info("Initializing JDBCConfiguration..");
    }


    @Bean
    public ClusterManager clusterManager(DataSource dataSource){
        return new JDBCClusterManager(dataSource);
    }

}
