package com.keta.rule.cluster.jdbc;

import com.keta.rule.cluster.notify.Refresh;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.exception.JDBCClusterException;
import com.keta.rule.model.RuleVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.HashedMap;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Log4j2
public class JDBCMessageSender {

    private final Map<String, ConnectedMember> connectedMembers = new HashedMap<>();

    public boolean addMember(String memberId, String hostName, int port) {
        if (connectedMembers.containsKey(memberId)) {
            return false;
        }

        try {
            Socket socket = new Socket(hostName, port);
            connectedMembers.put(memberId, new ConnectedMember(memberId, socket));
            return true;
        } catch (IOException e) {
            log.error("Failed to connect to member {} ", memberId, e);
            throw new JDBCClusterException("Failed to connect to member " + memberId, e);
        }
    }

    public void notifyForRefresh(String memberId) {
        Refresh refresh = new Refresh(memberId);
    }


    public void notifyUpdateUpdate(Update update) {

    }


    public void notifyState(RuleVersion ruleVersion) {

    }


    @PreDestroy
    public void close() {
        connectedMembers.values().stream().forEach(ConnectedMember::close);
    }

    private class ConnectedMember {

        private final String id;
        private final Socket socket;

        public ConnectedMember(String id, Socket socket) {
            this.id = id;
            this.socket = socket;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConnectedMember that = (ConnectedMember) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        public void send(Object message) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(message);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(byteArrayOutputStream.toByteArray());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log.error("Failed to serialize message", e);
                throw new JDBCClusterException("Failed to serialize message", e);
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                log.info("Failed to close client socket {}", id, e);
                throw new JDBCClusterException("Failed to close client socket " + id, e);
            }
        }

    }

}
