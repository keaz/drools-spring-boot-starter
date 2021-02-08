package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.exception.JDBCClusterException;
import com.keta.rule.model.RuleVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RequiredArgsConstructor
@Log4j2
public class JDBCClusterManager implements ClusterManager {

    private String hostName;
    private final DataSource dataSource;

    @Override
    public void setMessageReceiver(MessageReceiver receiver) {

    }

    @PostConstruct
    @Override
    public void join() {
        log.info("Joining the JDBC cluster");
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new JDBCClusterException("Cannot get host name ", e);
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public void notifyForRefresh() {

    }

    @Override
    public void notifyUpdateUpdate(Update update) {

    }

    @Override
    public ClusterState getClusterState() {
        return null;
    }

    @Override
    public void notifyState(RuleVersion ruleVersion) {

    }
}
