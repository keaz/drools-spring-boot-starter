package com.keta.rule.cluster.jdbc;

import lombok.Value;

@Value
public class JDBCMembers {

    private String id;
    private String hostName;
    private int port;

}
