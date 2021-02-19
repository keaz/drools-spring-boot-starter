package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Join;
import com.keta.rule.cluster.notify.State;
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
import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class JDBCClusterManager implements ClusterManager {

    private final ClusterState clusterState = new ClusterState();
    private final SQLConnector sqlConnector;
    private final String memberId;
    private final JDBCMessageReceiver messageReceiver;
    private final JDBCMessageSender jdbcMessageSender;
    private final int port;
    private String hostName;

    @Override
    public void setMessageReceiver(MessageReceiver receiver) {
        //#TODO not implemented yet
    }

    @PostConstruct
    @Override
    public void join() {
        log.info("Joining the JDBC cluster");
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            sqlConnector.register(hostName, port);
            List<JDBCMembers> members = sqlConnector.members();
            notifyJoin(members, memberId);
        } catch (UnknownHostException e) {
            log.error("Cannot get host name ", e);
            throw new JDBCClusterException("Cannot get host name ", e);
        }
    }

    private void notifyJoin(List<JDBCMembers> members, String memberId) {
        jdbcMessageSender.notifyJoin(members, new Join(memberId));
    }

    @Override
    @PreDestroy
    public void leave() {
        log.info("Leaving the JDBC cluster");
        sqlConnector.leave();
    }

    @Override
    public void notifyForRefresh() {
        //#TODO not implemented yet
    }

    @Override
    public void notifyUpdateUpdate(Update update) {
        List<JDBCMembers> members = sqlConnector.members();
        jdbcMessageSender.notifyUpdate(members, update);
    }

    @Override
    public ClusterState getClusterState() {
        return null;
    }

    @Override
    public void notifyState(RuleVersion ruleVersion) {
        List<JDBCMembers> members = sqlConnector.members();
        State state = new State(memberId, ruleVersion.getGitTag(), ruleVersion.getCommitId(), ruleVersion.getCommitAuthor(),
                ruleVersion.getCommitDate(), ruleVersion.getCommitMessage());
        jdbcMessageSender.notifyState(members, state);
    }
}
