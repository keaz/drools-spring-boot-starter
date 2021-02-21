package com.keta.rule.cluster.notify;

import lombok.Value;

@Value
public class Leave extends ClusterMessage{

    public Leave(String memberId){
        super(memberId);
    }

}
