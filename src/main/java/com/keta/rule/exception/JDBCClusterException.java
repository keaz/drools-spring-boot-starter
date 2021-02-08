package com.keta.rule.exception;

public class JDBCClusterException extends ClusterException {

    public JDBCClusterException(String message, Exception root) {
        super(message, root);
    }
}
