package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.exception.JDBCClusterException;
import com.keta.rule.model.RuleVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RequiredArgsConstructor
@Log4j2
public class JDBCClusterManager implements ClusterManager {

    private final ClusterState clusterState = new ClusterState();
    private final SQLConnector sqlConnector;
    private final String memberId;
    private final JDBCMessageReceiver messageReceiver;
    private final JDBCMessageSender jdbcMessageSender;
    private String hostName;

    @Override
    public void setMessageReceiver(MessageReceiver receiver) {

    }

    @PostConstruct
    @Override
    public void join() {
        log.info("Joining the JDBC cluster");
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            sqlConnector.register(hostName, messageReceiver.getPort());
        } catch (UnknownHostException e) {
            log.error("Cannot get host name ", e);
            throw new JDBCClusterException("Cannot get host name ", e);
        }
    }

    @Override
    @PreDestroy
    public void leave() {
        log.info("Leaving the JDBC cluster");
        sqlConnector.leave();
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
