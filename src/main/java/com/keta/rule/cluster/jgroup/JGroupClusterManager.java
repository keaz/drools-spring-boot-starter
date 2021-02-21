package com.keta.rule.cluster.jgroup;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.notify.Refresh;
import com.keta.rule.cluster.notify.State;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.exception.NotifyException;
import com.keta.rule.model.UpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@RequiredArgsConstructor
@Log4j2
public class JGroupClusterManager implements ClusterManager {

    private final JChannel channel;
    private final ClusterState clusterState = new ClusterState();
    private final JGroupMessageReceiver messageReceiver;

    @PostConstruct
    @Override
    public void join() {
        log.info("Joining the JGroup cluster");
    }

    @Override
    public void notifyRefresh() {
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();
        message.setObject(new Refresh(addressAsString));
        try {
            log.info("Notifying refresh to cluster ");
            channel.send(message);
        } catch (Exception e) {
            throw new NotifyException("Failed to notify refresh to cluster", e);
        }
    }

    @Override
    public void notify(UpdateRequest updateRequest) {

    }


    @Override
    public ClusterState getClusterState() {
        return clusterState;
    }

    @Override
    public String getMemberId() {
        return null;
    }

    @Override
    public void notify(State state) {
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();

        message.setObject(state);
        try {
            log.info("Notifying state to cluster");
            channel.send(message);
        } catch (Exception e) {
            log.error("Failed to notify state to cluster", e);
            throw new NotifyException("Failed to notify to cluster ", e);
        }
    }

    @Override
    @PreDestroy
    public void leave() {
        channel.close();
    }
}
