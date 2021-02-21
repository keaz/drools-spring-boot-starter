package com.keta.rule.cluster;

import com.keta.rule.cluster.notify.State;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.model.UpdateRequest;

public interface ClusterManager {

    void join();

    void leave();

    void notifyRefresh();

    void notify(UpdateRequest updateRequest);

    void notify(State state);

    ClusterState getClusterState();

    String getMemberId();

}
