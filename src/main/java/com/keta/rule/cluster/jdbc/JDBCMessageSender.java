package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.notify.*;
import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class JDBCMessageSender {


    public void notifyJoin(List<JDBCMembers> members, Join join) {
        members.forEach(jdbcMembers -> sendMessage(jdbcMembers, join));
    }

    public void notifyForRefresh(List<JDBCMembers> members, Refresh refresh) {
        members.forEach(member -> sendMessage(member, refresh));
    }

    public void notifyUpdate(List<JDBCMembers> members, Update update) {
        members.forEach(member -> sendMessage(member, update));
    }

    public void notifyState(List<JDBCMembers> members, State state) {
        members.forEach(member -> sendMessage(member, state));
    }


    private void sendMessage(JDBCMembers jdbcMember, ClusterMessage clusterMessage) {
        String messageName = clusterMessage.getClass().getSimpleName();
        InetSocketAddress hostAddress = new InetSocketAddress(jdbcMember.getHostName(), jdbcMember.getPort());
        try (SocketChannel client = SocketChannel.open(hostAddress)) {
            client.configureBlocking(true);
            log.info("Client... started");
            ByteBuffer byteBuffer = getByteBuffer(clusterMessage);
            client.write(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            log.error("Failed to connect to member {} ", jdbcMember.getId(), e);
            throw new JDBCClusterException("Failed to connect to member " + jdbcMember.getId(), e);
        } finally {
            log.info("Closing connection after sending {} message to member: {}", messageName, jdbcMember.getId());
        }

    }

    private ByteBuffer getByteBuffer(ClusterMessage message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

}
