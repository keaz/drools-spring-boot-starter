package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.*;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.cluster.state.Member;
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
    private final ClusterState clusterState;
    private Selector selector;
    private final Set<SocketChannel> dataMapper = new HashSet<>();

    @PostConstruct
    public void init() throws IOException {
        this.selector = Selector.open();
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        log.info("Server Started..");
        Thread messageReceiverThread = new Thread(this::startServer);
        messageReceiverThread.setName("receiver-thread");
        messageReceiverThread.start();
    }

    private void startServer() {
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
            channel.register(this.selector, SelectionKey.OP_READ);
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
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int read = channel.read(byteBuffer);
            if (read == -1) {
                log.info("Got connection close.");
                channel.close();
                key.cancel();
                return;
            }

            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array()));
            ClusterMessage clusterMessage = (ClusterMessage) objectInputStream.readObject();
            log.info("Message: {}", clusterMessage);
            eventLoop(clusterMessage);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to read incoming message", e);
            throw new JDBCClusterException("Failed to read incoming message", e);
        }

    }

    private void eventLoop(ClusterMessage message) {
        String memberId = message.getMemberId();
        if (message instanceof Join) {
            handleJoin(memberId);
            return;
        }

        if (message instanceof State) {
            handleState(message, memberId);
            return;
        }

        if (message instanceof Update) {
            handleUpdate((Update) message);
            return;
        }

        if (message instanceof Leave) {
            handleUpdate(memberId);
        }

    }

    private void handleJoin(String memberId) {
        if (clusterState.getMembers().containsKey(memberId)) {
            log.warn("Duplicate Join message for member {}", memberId);
        } else {
            log.info("Adding new Member to the cluster state {}", memberId);
            Member member = new Member();
            member.setMemberId(memberId);
            clusterState.getMembers().put(memberId, member);
        }
    }

    private void handleUpdate(String memberId) {
        log.info("Handling Leave event from {}", memberId);

        if (clusterState.getMembers().containsKey(memberId)) {
            clusterState.getMembers().remove(memberId);
        } else {
            log.warn("Cannot find member {} ", memberId);
        }
    }

    private void handleState(ClusterMessage message, String memberId) {
        log.info("Handling State event {}", message);

        State state = (State) message;
        if (clusterState.getMembers().containsKey(memberId)) {
            Member member = clusterState.getMembers().get(memberId);
            createMember(state, member);
        } else {
            log.warn("Cannot find member {} ", memberId);
        }
    }

    private void createMember(State state, Member member) {
        member.setCommitAuthor(state.getCommitAuthor());
        member.setCommitDate(state.getCommitDate());
        member.setCommitId(state.getCommitId());
        member.setCommitMessage(state.getCommitMessage());
        member.setGitTag(state.getGitTag());
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
        try {
            log.info("Stopping server ");
            serverChannel.close();
        } catch (IOException e) {
            log.error("Error occurred when closing server ", e);
        }
    }


}
