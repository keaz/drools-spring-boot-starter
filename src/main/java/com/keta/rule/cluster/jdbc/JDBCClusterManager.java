package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.notify.*;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.exception.JDBCClusterException;
import com.keta.rule.model.UpdateRequest;
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

    private final ClusterState clusterState;
    private final SQLConnector sqlConnector;
    private final String memberId;
    private final JDBCMessageSender jdbcMessageSender;
    private final int port;
    private String hostName;

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
        jdbcMessageSender.notifyLeave(sqlConnector.members(),new Leave(memberId));
    }

    @Override
    public void notifyRefresh() {
        List<JDBCMembers> members = sqlConnector.members();
        jdbcMessageSender.notifyForRefresh(members,new Refresh(memberId));
    }

    @Override
    public void notify(UpdateRequest updateRequest) {
        List<JDBCMembers> members = sqlConnector.members();
        jdbcMessageSender.notifyUpdate(members, new Update(updateRequest.getCommitId(),memberId));
    }

    @Override
    public ClusterState getClusterState() {
        return clusterState;
    }

    @Override
    public void notify(State state) {
        List<JDBCMembers> members = sqlConnector.members();
        jdbcMessageSender.notifyState(members, state);
    }

    @Override
    public String getMemberId() {
        return memberId;
    }

}
