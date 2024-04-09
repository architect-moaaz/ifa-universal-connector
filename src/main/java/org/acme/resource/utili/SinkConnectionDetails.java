package org.acme.resource.utili;

public class SinkConnectionDetails {
    public  String Connector;
    public  String DbName;
    public  String DbUser;
    public  String DbPassword;
    public  String Host;
    public  String Port;

    public String getConnector() {
        return Connector;
    }

    public void setConnector(String connector) {
        Connector = connector;
    }

    public String getDbName() {
        return DbName;
    }

    public void setDbName(String dbName) {
        DbName = dbName;
    }

    public String getDbUser() {
        return DbUser;
    }

    public void setDbUser(String dbUser) {
        DbUser = dbUser;
    }

    public String getDbPassword() {
        return DbPassword;
    }

    public void setDbPassword(String dbPassword) {
        DbPassword = dbPassword;
    }

    public String getHost() {
        return Host;
    }

    public void setHost(String host) {
        Host = host;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String port) {
        Port = port;
    }

    public String getMiniAppName() {
        return MiniAppName;
    }

    public void setMiniAppName(String miniAppName) {
        MiniAppName = miniAppName;
    }

    public String getWorkSpaceName() {
        return WorkSpaceName;
    }

    public void setWorkSpaceName(String workSpaceName) {
        WorkSpaceName = workSpaceName;
    }

    public String getKafkaHost() {
        return KafkaHost;
    }

    public void setKafkaHost(String kafkaHost) {
        KafkaHost = kafkaHost;
    }

    public String getKafkaPort() {
        return KafkaPort;
    }

    public void setKafkaPort(String kafkaPort) {
        KafkaPort = kafkaPort;
    }

    public  String MiniAppName;
    public  String WorkSpaceName;
    public  String KafkaHost;
    public  String KafkaPort;
}