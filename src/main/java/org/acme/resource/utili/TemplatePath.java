package org.acme.resource.utili;

public class TemplatePath {
    public final static String ROOT_PATH_CDC = "connectors/cdc/";
    public final static String ROOT_PATH_JDBC = "connectors/jdbc/";
    public final static String ROOT_PATH_SFTP = "connectors/sftp/";
    public final static String MYSQL_SOURCE= ROOT_PATH_CDC +"source/MYSQL-CONNECTOR-SOURCE.json";
    public final static String POSTGRES_SOURCE= ROOT_PATH_CDC +"source/POSTGRES-CONNECTOR-SOURCE.json";
    public final static String MONGODB_SOURCE= ROOT_PATH_CDC +"source/MONGODB-CONNECTOR-SOURCE.json";

    public final static String SFTP_CSV_SOURCE= ROOT_PATH_SFTP +"source/SFTP-CONNECTOR-SOURCE-CSV.json";
    public final static String SFTP_JSON_SOURCE= ROOT_PATH_SFTP +"source/SFTP-CONNECTOR-SOURCE-JSON.json";
    public final static String MYSQL_SOURCE_JDBC= ROOT_PATH_JDBC +"source/MYSQL-CONNECTOR-SOURCE.json";
    public final static String POSTGRES_SOURCE_JDBC= ROOT_PATH_JDBC +"source/POSTGRES-CONNECTOR-SOURCE.json";
    public final static String MONGODB_SOURCE_JDBC= ROOT_PATH_JDBC +"source/MONGODB-CONNECTOR-SOURCE.json";
    public final static String MONGODB_SINK_JDBC= ROOT_PATH_JDBC +"sink/MONGODB-CONNECTOR-SINK.json";


    public final static String MYSQL_SINK= ROOT_PATH_CDC +"sink/MYSQL-CONNECTOR-SINK.json";
    public final static String POSTGRES_SINK= ROOT_PATH_CDC +"sink/POSTGRES-CONNECTOR-SINK.json";
    public final static String MONGODB_SINK= ROOT_PATH_CDC +"sink/MONGODB-CONNECTOR-SINK.json";
    public final static String MONGODB_SINK_CDC= ROOT_PATH_CDC +"sink/MONGODB-CONNECTOR-SINK.json";
    public final static String MONGODB_SINK_SFTP= ROOT_PATH_SFTP +"sink/SFTP-CONNECTOR-SINK.json";
    public final static String ES_SINK= ROOT_PATH_CDC +"sink/ES-CONNECTOR-SINK.json";
    public final static String CASSANDRA_SINK= ROOT_PATH_CDC +"sink/CASSANDRA-CONNECTOR-SINK.json";
    public final static String SFTP_SINK = ROOT_PATH_SFTP+"sink/SFTP-CONNECTOR-SINK.json";

}
