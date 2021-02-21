package com.keta.rule.cluster.notify;

import lombok.Value;

@Value
public class Update extends ClusterMessage {

    private final String commitId;

    public Update(String commitId,String address) {
        super(address);
        this.commitId = commitId;
    }

}
