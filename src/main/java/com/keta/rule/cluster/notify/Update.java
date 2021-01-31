package com.keta.rule.cluster.notify;

import lombok.Value;

@Value
public class Update extends ClusterMessage {

    public Update(String address) {
        super(address);
    }

}
