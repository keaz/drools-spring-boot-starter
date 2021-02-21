package com.keta.rule.config;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.jdbc.JDBCClusterManager;
import com.keta.rule.cluster.jdbc.JDBCMessageReceiver;
import com.keta.rule.cluster.jdbc.JDBCMessageSender;
import com.keta.rule.cluster.jdbc.SQLConnector;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.UUID;


@Log4j2
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ConfigData.class})
@ConditionalOnBean(DataSource.class)
@ComponentScan("com.keta.rule.cluster.jdbc")
@ConditionalOnProperty(prefix = "keta.rule", name = "cluster-type", havingValue = "jdbc", matchIfMissing = true)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class JDBCConfiguration {

    private static final String MEMBER_ID = UUID.randomUUID().toString();
    private static final ClusterState CLUSTER_STATE = new ClusterState();

    @PostConstruct
    public void init() {
        log.info("Initializing JDBCConfiguration..");
    }

    private final ConfigData configData;

    @Bean
    @DependsOn("jdbcMessageReceiver")
    public ClusterManager clusterManager(DataSource dataSource) {
        return new JDBCClusterManager(CLUSTER_STATE,new SQLConnector(dataSource, MEMBER_ID), MEMBER_ID, new JDBCMessageSender(), configData.getPort());
    }

    @Bean
    public JDBCMessageReceiver jdbcMessageReceiver() {
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), configData.getPort());
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(inetSocketAddress);
            JDBCMessageReceiver jdbcMessageReceiver = new JDBCMessageReceiver(serverChannel,CLUSTER_STATE);
            return jdbcMessageReceiver;
        } catch (IOException e) {
            log.error("Failed to create Server Socket", e);
            throw new JDBCClusterException("Failed to create Server Socket", e);
        }
    }

}
