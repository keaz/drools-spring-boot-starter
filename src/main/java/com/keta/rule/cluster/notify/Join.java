package com.keta.rule.cluster.notify;

import lombok.Value;

@Value
public class Join extends ClusterMessage {

    public Join(String memberId) {
        super(memberId);
    }

}
