package com.keta.rule.cluster.jdbc;

import com.keta.rule.exception.JDBCClusterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Log4j2
public class SQLConnector {

    private static final String ID_COLUMN = "member_id";
    private static final String HOST_COLUMN = "host_name";
    private static final String PORT_COLUMN = "port";
    private static final String SELECT_ALL_MEMBERS = "SELECT member_id,host_name,port FROM cluster_member";
    private static final String REGISTER = "INSERT INTO cluster_member (member_id,host_name,port) VALUES (?,?,?)";
    private static final String LEAVE = "DELETE FROM cluster_member WHERE member_id = ?";

    private final DataSource dataSource;
    private final String memberId;

    public void register(String hostName, int port) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(REGISTER)) {
                preparedStatement.setString(1, memberId);
                preparedStatement.setString(2, hostName);
                preparedStatement.setInt(3, port);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            log.error("Failed to add member to the database ", sqlException);
            throw new JDBCClusterException("Failed to add member to the database ", sqlException);
        }
    }

    public void leave() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(LEAVE)) {
                preparedStatement.setString(1, memberId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            log.error("Failed to delete member to the database ", sqlException);
            throw new JDBCClusterException("Failed to delete member to the database ", sqlException);
        }
    }

    public List<JDBCMembers> members() {

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                boolean hasResults = statement.execute(SELECT_ALL_MEMBERS);
                if (hasResults) {
                    List<JDBCMembers> members = new ArrayList<>();
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        String id = resultSet.getString(ID_COLUMN);
                        String hostName = resultSet.getString(HOST_COLUMN);
                        int port = resultSet.getInt(PORT_COLUMN);
                        members.add(new JDBCMembers(id, hostName, port));
                    }
                    return members;
                }
            }
        } catch (SQLException sqlException) {
            log.error("Failed getting members from the database ", sqlException);
            throw new JDBCClusterException("Failed getting members from the database ", sqlException);
        }
        return Collections.emptyList();
    }


}
