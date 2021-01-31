package com.keta.rule.cluster;

import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.model.RuleVersion;

public interface ClusterManager {

    void join();

    void notifyForRefresh();

    void notifyUpdateUpdate(Update update);

    ClusterState getClusterState();

    void notifyState(RuleVersion ruleVersion);

}
