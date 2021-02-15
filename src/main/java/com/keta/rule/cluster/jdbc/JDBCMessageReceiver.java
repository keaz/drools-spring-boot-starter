package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.ClusterMessage;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

@RequiredArgsConstructor
@Log4j2
public class JDBCMessageReceiver implements MessageReceiver {

    private final ServerSocket socket;
    private volatile boolean running = true;

    public int getPort() {
        return socket.getLocalPort();
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = socket.accept();
                    InputStream inputStream = clientSocket.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    ClusterMessage o = (ClusterMessage)objectInputStream.readObject();
                    System.out.println(o);
                    objectInputStream.close();
                    inputStream.close();
                    clientSocket.close();
                } catch (IOException | ClassNotFoundException e) {
                    log.error("Failed to accept incoming message", e);
                    throw new JDBCClusterException("Failed to accept incoming message", e);
                }
            }
        }).start();
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
