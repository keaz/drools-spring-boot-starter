package com.keta.rule.cluster.notify;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ClusterMessage implements Serializable {

    private final String memberId;

    public ClusterMessage(String memberId) {
        this.memberId = memberId;
    }

}
