package com.keta.rule.cluster.notify;

import lombok.Value;

@Value
public class Refresh extends ClusterMessage {

    public Refresh(String address){
        super(address);
    }

}
