package com.keta.rule.cluster;

import com.keta.rule.cluster.notify.Update;

public interface MessageReceiver {

    void handleRefresh();

    void handleUpdate(Update update);

}
