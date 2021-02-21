package com.keta.rule.cluster;

import com.keta.rule.cluster.jdbc.JDBCMembers;
import com.keta.rule.cluster.notify.*;

import java.util.List;

public interface MessageSender {

    void notifyJoin(List<JDBCMembers> members, Join join);

    void notifyForRefresh(List<JDBCMembers> members, Refresh refresh);

    void notifyUpdate(List<JDBCMembers> members, Update update);

    void notifyState(List<JDBCMembers> members, State state);

    void notifyLeave(List<JDBCMembers> members, Leave leave);

}
