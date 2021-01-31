package com.keta.rule.cluster.notify;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClusterMessage implements Serializable {

    private final String address;

    public ClusterMessage(String address) {
        this.address = address;
    }

}
