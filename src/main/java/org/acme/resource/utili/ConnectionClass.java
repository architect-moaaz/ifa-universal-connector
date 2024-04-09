package org.acme.resource.utili;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.acme.dto.UserConnectorConfigDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {

    public static String POSTGRES_URL = "jdbc:postgresql://";
    public static String MYSQL_URL = "jdbc:mysql://";

    public static String DB2_URL="jdbc:db2:";

    public static String SQL_SERVER_URL="jdbc:sqlserver://";

    public static String ORACLE_URL = "jdbc:oracle:thin:@";

    public static String MONGO_DB_URL = "jdbc:mongodb://";

    public static String CASSANDRA_URL="jdbc:cassandra://";

    public static Connection createCassandraConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(CASSANDRA_URL).append("localhost").append(":").append(userConnectorConfigDto.getPort()).append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }
    public static Connection createDB2ConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(DB2_URL).append("localhost").append(":").append(userConnectorConfigDto.getPort()).append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static Connection createMONGODbConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(MONGO_DB_URL).append("localhost").append("/").append(userConnectorConfigDto.getPort()).append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static Connection createOracleConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(ORACLE_URL).append("localhost").append(":").append(userConnectorConfigDto.getPort()).append(":").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static Connection createPostgresConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(POSTGRES_URL).append(userConnectorConfigDto.getHost()).append(":").append(userConnectorConfigDto.getPort()).append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static Connection createMysqlConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(MYSQL_URL).append(userConnectorConfigDto.getHost()).append(":").append(userConnectorConfigDto.getPort()).append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static Connection createSqlServerConnectionUrl(UserConnectorConfigDto userConnectorConfigDto) throws SQLException {
        StringBuilder result = new StringBuilder();
        return DriverManager.getConnection(result.append(SQL_SERVER_URL).append("localhost").append("/").append(userConnectorConfigDto.getDbName()).toString(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword());
    }

    public static ChannelSftp createSftpConnection(UserConnectorConfigDto userConnectorConfigDto) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getHost(), Integer.valueOf(userConnectorConfigDto.getPort()));
            session.setPassword(userConnectorConfigDto.getDbPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            return sftpChannel;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        }
    }
