package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@RequiredArgsConstructor
@Log4j2
public class JDBCMessageReceiver implements MessageReceiver {

    public static final String FAILED_TO_ACCEPT_INCOMING_MESSAGE = "Failed to accept incoming message";
    private final ServerSocketChannel serverChannel;
    private Selector selector;
    private final Set<SocketChannel> dataMapper = new HashSet<>();
    private volatile boolean running = true;


    @PostConstruct
    public void init() throws IOException {
        this.selector = Selector.open();
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        log.info("Server Started..");
        new Thread(() -> {
            while (serverChannel.isOpen()) {
                try {
                    // wait for events
                    this.selector.select();
                    //work on selected keys
                    Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        handle(key);
                    }
                } catch (IOException e) {
                    log.error(FAILED_TO_ACCEPT_INCOMING_MESSAGE, e);
                    throw new JDBCClusterException(FAILED_TO_ACCEPT_INCOMING_MESSAGE, e);
                }
            }
        }).start();
    }

    private void handle(SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        if (key.isAcceptable()) {
            this.accept(key);
            return;
        }
        if (key.isReadable()) {
            this.read(key);
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        try {
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            log.info("Connected to {}", remoteAddr);

            // register channel with selector for further IO
            dataMapper.add(channel);
        } catch (IOException e) {
            log.error(FAILED_TO_ACCEPT_INCOMING_MESSAGE, e);
            throw new JDBCClusterException(FAILED_TO_ACCEPT_INCOMING_MESSAGE, e);
        }

    }

    private void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        log.info("Start reading message..");
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(256);
            channel.read(byteBuffer);

            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array()));
            Object o = objectInputStream.readObject();
            log.info("Message: {}", o);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to read incoming message", e);
            throw new JDBCClusterException("Failed to read incoming message", e);
        }

    }

    @Override
    public void handleRefresh() {
        //#TODO not implemented yet
    }

    @Override
    public void handleUpdate(Update update) {
        //#TODO not implemented yet
    }

    @PreDestroy
    public void clean() {
        running = false;
    }
}
