package com.keta.rule.cluster.jgroup;

import org.jgroups.Message;
import org.jgroups.Receiver;
import org.springframework.stereotype.Service;

@Service
public class JGroupMessageReceiver implements Receiver {

    @Override
    public void receive(Message msg) {
        System.out.println((String)msg.getObject() );
    }
}
