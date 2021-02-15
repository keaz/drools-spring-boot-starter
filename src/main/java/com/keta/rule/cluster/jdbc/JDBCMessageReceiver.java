package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@RequiredArgsConstructor
@Log4j2
public class JDBCMessageReceiver implements MessageReceiver {

    private final ServerSocket socket;
    private volatile boolean running;

    public int getPort() {
        return socket.getLocalPort();
    }

    @PostConstruct
    public void init() {
        new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        Socket clientSocket = socket.accept();
                    } catch (IOException e) {
                        log.error("Failed to accept incoming message", e);
                        throw new JDBCClusterException("Failed to accept incoming message", e);
                    }
                }
            }
        }.start();
    }

    private void eventLoop() {

    }

    @Override
    public void handleRefresh() {

    }

    @Override
    public void handleUpdate(Update update) {

    }

    @PreDestroy
    public void clean() {
        running = false;
    }
}
