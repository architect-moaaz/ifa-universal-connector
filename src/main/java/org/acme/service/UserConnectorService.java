package org.acme.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import io.smallrye.mutiny.Uni;
import org.acme.dto.DtoHelper;
import org.acme.dto.UserConnectorConfigDto;
import org.acme.model.*;
import org.acme.repository.DbConnectionDetailsRepository;
import org.acme.repository.SourceSinkConnectorConfigRepository;
import org.acme.repository.UserConnectorConfigRepository;
import org.acme.resource.utili.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.acme.resource.utili.ConnectionClass.ORACLE_URL;

@ApplicationScoped
public class UserConnectorService {


    private String POSTGRES_URL = "jdbc:postgresql://";
    private String MYSQL_URL = "jdbc:mysql://";

    private String DB2_URL = "jdbc:db2:";

    private String SQL_SERVER_URL = "jdbc:sqlserver://";

    private String ORACLE = "jdbc:oracle:thin:@";
    private String MONGO_URL_SOURCE = "rs0";
    private String MONGO_URL_SINK = "mongodb://";
    private String URL = "http://";
    private String NAME = "name";
    private String CONFIG = "config";
    private String DOUBLE_ORDINAL = "7";

    private String MYSQL_DRIVER_CLASS = "new com.mysql.jdbc.Driver()";

    Logger logger = LoggerFactory.getLogger("universal-connector");

    @Inject
    private UserConnectorConfigRepository userConnectorConfigRepository;

    @Inject
    private SourceSinkConnectorConfigRepository sourceSinkConnectorConfigRepository;

    @Inject
    private DbConnectionDetailsRepository dbConnectionDetailsRepository;

    @RestClient
    private ConnectorService connectorService;

    @RestClient
    private ModellerService modellerService;

    @Inject
    private EncryptionService encryptionService;

    private boolean sinkCreationFlag = true;
    private Map<String,HashMap<String,List<String>>> configErrors = new HashMap<>();

    private SinkConnectionDetails sinkConnectionDetails = new SinkConnectionDetails();


    public JSONObject createSourceConnectorJson(UserConnectorConfigDto userConnectorConfigDTO) {
        JSONObject response = null;
        if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
//            userConnectorConfigDTO.setHost("mysql");
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceMysqlJsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);
        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
            userConnectorConfigDTO.setHost("postgres");
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                    response = createSourcePostgresDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);
        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
            response = createSourceMongodbJsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);

        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceCassandraJsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);

        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.DB2.toString())) {
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceDB2JsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);

        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceSQLServerJsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);

        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.VITESS.toString())) {
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceVitessJsonDebezium(userConnectorConfigDTO);
            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceJsonJdbc(userConnectorConfigDTO);
        }else if(userConnectorConfigDTO.getConnector().equals(ConnectorEnum.SFTP.toString())){
            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CSV.toString())) {
                response = createSourceSftpCSV(userConnectorConfigDTO);
            }else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JSON.toString())){
                response = createSourceSftpJSON(userConnectorConfigDTO);
            }
        }
        return response;
    }

    private JSONObject createSourceSftpJSON(UserConnectorConfigDto userConnectorConfigDto) {
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_JSON_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType()+"-"+userConnectorConfigDto.getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("kafka.topic",userConnectorConfigDto.getTopics());
            configObject.put("input.path",userConnectorConfigDto.getInputPathForSftp());
            configObject.put("finished.path",userConnectorConfigDto.getFinishedPathForSftp());
            configObject.put("error.path",userConnectorConfigDto.getErrorPathForSftp());
            configObject.put("sftp.username", userConnectorConfigDto.getDbUser());
            configObject.put("sftp.password", userConnectorConfigDto.getDbPassword());
            configObject.put("sftp.host",userConnectorConfigDto.getHost());
            configObject.put("sftp.port",userConnectorConfigDto.getPort());
            configObject.put("input.file.pattern",userConnectorConfigDto.getFileName());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceSftpCSV(UserConnectorConfigDto userConnectorConfigDto) {
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_CSV_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getConnectorType()+"-"+userConnectorConfigDto.getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("kafka.topic",userConnectorConfigDto.getTopics());
            configObject.put("input.path",userConnectorConfigDto.getInputPathForSftp());
            configObject.put("finished.path",userConnectorConfigDto.getFinishedPathForSftp());
            configObject.put("error.path",userConnectorConfigDto.getErrorPathForSftp());
            configObject.put("sftp.username", userConnectorConfigDto.getDbUser());
            configObject.put("sftp.password", userConnectorConfigDto.getDbPassword());
            configObject.put("sftp.host",userConnectorConfigDto.getHost());
            configObject.put("sftp.port",userConnectorConfigDto.getPort());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceVitessJsonDebezium(UserConnectorConfigDto userConnectorConfigDTO) {
        return null;
    }

    private JSONObject createSourceSQLServerJsonDebezium(UserConnectorConfigDto userConnectorConfigDTO) {
        return null;
    }

    private JSONObject createSourceDB2JsonDebezium(UserConnectorConfigDto userConnectorConfigDTO) {
        return null;
    }

    private JSONObject createSourceCassandraJsonDebezium(UserConnectorConfigDto userConnectorConfigDTO) {
        return null;
    }

    private String createConnectionLinkPostgresJdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(POSTGRES_URL);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName);
        return connectionUrl.toString();
    }

    private JSONObject createSourceJsonJdbc(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
//            ClassLoader classLoader = getClass()
//            InputStream in = getClass().getClassLoader().getResourceAsStream(TemplatePath.MYSQL_SOURCE_JDBC);
//            InputStream resourceAsStream = classLoader.getResourceAsStream(TemplatePath.MYSQL_SOURCE_JDBC);
//            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("src/main/resources/connectors/jdbc/source/MYSQL-CONNECTOR-SOURCE.json");
            String sb = helperMethodeToGeTJson(TemplatePath.MYSQL_SOURCE_JDBC);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
//            String workFlowJson = IOUtils.toString(in, StandardCharsets.UTF_8);
//            JSONObject jsonObject = new JSONObject(workFlowJson);
//            Object obj = parser.parse(new FileReader(TemplatePath.MYSQL_SOURCE_JDBC));
//            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType()+"-"+userConnectorConfigDto.getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            if(userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                configObject.put("connection.url", createConnectionLinkMysqlJdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            }else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                configObject.put("connection.url", createConnectionLinkPostgresJdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.DB2.toString())) {
                configObject.put("connection.url", createConnectionLinkDB2Jdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
                configObject.put("connection.url", createConnectionLinkCassandraJdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ORACLE.toString())) {
                configObject.put("connection.url", createConnectionLinkOracleJdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            }else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
                configObject.put("connection.url", createConnectionLinkMongoDbJdbc(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            }
            configObject.put("connection.user", userConnectorConfigDto.getDbUser());
            configObject.put("connection.password", userConnectorConfigDto.getDbPassword());
            configObject.put("table.whitelist", addTableList(userConnectorConfigDto.getTables()));
            configObject.put("topic.prefix",userConnectorConfigDto.getTopicsPrefix());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String helperMethodeToGeTJson(String templatePath) throws IOException {

        InputStream in = getClass().getClassLoader().getResourceAsStream(templatePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while (true) {
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String createConnectionLinkMongoDbJdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(MONGO_URL_SINK);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName);
        return connectionUrl.toString();
    }

    private String createConnectionLinkOracleJdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(ORACLE_URL);
        return connectionUrl.append(ORACLE_URL).append(host).append(":").append(port).append(":").append(dbName).toString();
    }

    private String createConnectionLinkCassandraJdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(ConnectionClass.CASSANDRA_URL);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName);
        return connectionUrl.toString();
    }

    private String createConnectionLinkDB2Jdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(DB2_URL);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName);
        return connectionUrl.toString();
    }

    private String createConnectionLinkMysqlJdbc(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(MYSQL_URL);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName);
        return connectionUrl.toString();
    }

    private String addTableList(List<String> tables) {
        StringBuilder result = new StringBuilder();
        for (String table : tables) {
            result.append(table);
            result.append(",");
        }
        result.replace(result.length() - 1, result.length() - 1, "");
        return result.toString();

    }

    private JSONObject createSourceMongodbJsonDebezium(UserConnectorConfigDto userConnectorSourceConfigDTO) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MONGODB_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, userConnectorSourceConfigDTO.getUserId() + "-" + userConnectorSourceConfigDTO.getConnectorType() + "-" + userConnectorSourceConfigDTO.getDbName() + "-" + jsonObject.get("name"));
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("mongodb.hosts", createConnectionLinkMongodbSource(userConnectorSourceConfigDTO.getHost(), userConnectorSourceConfigDTO.getPort()));
//            configObject.put("mongodb.hosts","rs0/mongodb:27017");
            configObject.put("mongodb.user", userConnectorSourceConfigDTO.getDbUser());
            configObject.put("mongodb.password", userConnectorSourceConfigDTO.getDbPassword());
            configObject.put("database.include.list", userConnectorSourceConfigDTO.getDbName());
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createConnectionLinkMongodbSource(String host, String port) {
        StringBuilder connectionUrl = new StringBuilder(MONGO_URL_SOURCE);
        return connectionUrl.append("/" + host).append(":").append(port).toString();
    }

    private JSONObject createSourcePostgresDebezium(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.POSTGRES_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, userConnectorConfigDto.getUserId() + "-" + userConnectorConfigDto.getConnectorType() + "-" + userConnectorConfigDto.getDbName() + "-" + jsonObject.get("name"));
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database.port", userConnectorConfigDto.getPort());
            configObject.put("database.hostname", userConnectorConfigDto.getHost());
            configObject.put("database.user", userConnectorConfigDto.getDbUser());
            configObject.put("database.password", userConnectorConfigDto.getDbPassword());
            configObject.put("database.server.name", userConnectorConfigDto.getDbName());
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceMysqlJsonDebezium(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MYSQL_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType()+"-"+userConnectorConfigDto.getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database.port", userConnectorConfigDto.getPort());
            configObject.put("database.hostname", userConnectorConfigDto.getHost());
            configObject.put("database.user", userConnectorConfigDto.getDbUser());
            configObject.put("database.password", userConnectorConfigDto.getDbPassword());
            configObject.put("database.include.list", userConnectorConfigDto.getDbName());
        //    configObject.put("topic.prefix",userConnectorConfigDto.getTopicsPrefix());
            if (userConnectorConfigDto.getTables() != null && !userConnectorConfigDto.getTables().isEmpty()) {
                configObject.put("table.include.list", createTableListString(userConnectorConfigDto.getDbName(), userConnectorConfigDto.getTables()));
                if (userConnectorConfigDto.getColumns() != null && !userConnectorConfigDto.getColumns().isEmpty())
                    configObject.put("column.include.list", createColumnListString(userConnectorConfigDto.getDbName(), userConnectorConfigDto.getColumns()));
            } else {
                configObject.put("database.whitelist", userConnectorConfigDto.getDbName());
            }
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createColumnListString(String dbName, Map<String, List<String>> columns) {
        StringBuilder result = new StringBuilder();
        for (String table : columns.keySet()) {
            for (String column : columns.get(table)) {
                result.append(dbName).append(".").append(table).append(".").append(column).append(",");
            }
        }
        return result.toString();
    }

    private String createTableListString(String dbName, List<String> tables) {
        StringBuilder result = new StringBuilder();
        for (String table : tables) {
            result.append(dbName + "." + table);
            result.append(",");
        }
        result.replace(result.length() - 1, result.length() - 1, "");
        return result.toString();
    }


    public JSONObject createSinkConnectorJson(UserConnectorConfigDto userConnectorConfigDto) {
        JSONObject response = null;
        if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
            response = createSinkPostgresJson(userConnectorConfigDto);
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
            if(userConnectorConfigDto.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
            response = createSinkMongodbJsonDebezium(userConnectorConfigDto);
            else if(userConnectorConfigDto.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSinkMongodbJsonJdbc(userConnectorConfigDto);
            else if(userConnectorConfigDto.getConnectorFamily().equals(ConnectorEnum.SFTP.toString()))
                response = createSinkMongodbJsonJdbc(userConnectorConfigDto);
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
            response = createSinkMYSQLJson(userConnectorConfigDto);
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ELASTIC_SEARCH.toString())) {
            response = createSinkESJson(userConnectorConfigDto);
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.DB2.toString())) {
            // to be implemented
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
            // to be implemented
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ORACLE.toString())) {
            // to be implemented
        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
            // to be implemented
        }
        return response;
    }

    private JSONObject createSinkSftpJson(UserConnectorConfigDto userConnectorConfigDto) {

        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",userConnectorConfigDto.getDbName());
            if(userConnectorConfigDto.getTables()!=null && !userConnectorConfigDto.getTables().isEmpty())
                configObject.put("collection",userConnectorConfigDto.getTables().get(0));
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.uri", createConnectionLinkMongoDB(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSinkMongodbJsonJdbc(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",userConnectorConfigDto.getDbName());
            if(userConnectorConfigDto.getTables()!=null && !userConnectorConfigDto.getTables().isEmpty())
                configObject.put("collection",userConnectorConfigDto.getTables().get(0));
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.uri", createConnectionLinkMongoDB(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSinkMYSQLJson(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MYSQL_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, userConnectorConfigDto.getUserId() + "-" + userConnectorConfigDto.getDbName() + "-" + jsonObject.get("name"));
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.url", createConnectionLinkMysql(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createConnectionLinkMysql(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(MYSQL_URL);
        connectionUrl.append(host).append(":").append(port).append("/").append(dbName).append("?user:").append(dbUser).append("&").append("password:").append(dbPassword);
        return connectionUrl.toString();
    }

    private JSONObject createSinkESJson(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.ES_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, userConnectorConfigDto.getUserId() + "-" + userConnectorConfigDto.getDbName() + "-" + jsonObject.get("name"));
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.url", createConnectionLinkEs(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createConnectionLinkEs(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(URL);
        connectionUrl.append(host).append(":").append(port);
        return connectionUrl.toString();
    }

    private JSONObject createSinkMongodbJsonDebezium(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MONGODB_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",userConnectorConfigDto.getDbName());
            if(userConnectorConfigDto.getTables()!=null && !userConnectorConfigDto.getTables().isEmpty())
            configObject.put("collection",userConnectorConfigDto.getTables().get(0));
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.uri", createConnectionLinkMongoDB(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSinkPostgresJson(UserConnectorConfigDto userConnectorConfigDto) {
//        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.POSTGRES_SINK);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, userConnectorConfigDto.getUserId() + "-" + userConnectorConfigDto.getDbName() + "-" + userConnectorConfigDto.getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("pk.fields",userConnectorConfigDto.getPrimaryKeys());
            configObject.put("topics", userConnectorConfigDto.getTopics());
            configObject.put("connection.url", createConnectionLinkPostgres(userConnectorConfigDto.getHost(), userConnectorConfigDto.getPort(), userConnectorConfigDto.getDbName(), userConnectorConfigDto.getDbUser(), userConnectorConfigDto.getDbPassword()));
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createConnectionLinkMongoDB(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(MONGO_URL_SINK);
        connectionUrl.append(dbUser).append(":").append(dbPassword).append("@").append(host).append(":").append(port).append("/").append("?authSource=admin&w=1&");
        return connectionUrl.toString();
    }

    private String createConnectionLinkPostgres(String host, String port, String dbName, String dbUser, String dbPassword) {
        StringBuilder connectionUrl = new StringBuilder(POSTGRES_URL);
        connectionUrl.append(host).append(":").append(port).append("/" + dbName).append("?user=").append(dbUser + "&").append("password=").append(dbPassword);
        return connectionUrl.toString();
    }

    public List<DataModelDTO> createDataModelMapping(UserConnectorConfigDto userConnectorConfigDto) throws SQLException, ClassNotFoundException {
        JSONObject result = new JSONObject();
        List<DataModelDTO> response = null;
        userConnectorConfigDto.setDbPassword(encryptionService.decrypt(userConnectorConfigDto.getDbPassword()));
        if (userConnectorConfigDto.getConnectorType().equals(ConnectorEnum.SOURCE.toString())) {
            if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createMysqlConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createPostgresConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createMONGODbConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ORACLE.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createOracleConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createSqlServerConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.VITESS.toString())) {
                // to be implemented

            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ELASTIC_SEARCH.toString())) {
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createCassandraConnectionUrl(userConnectorConfigDto), result);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.DB2.toString())) {
                response = createDataModelJson(userConnectorConfigDto, ConnectionClass.createDB2ConnectionUrl(userConnectorConfigDto), result);
            }
        }
        return response;
    }

    private List<DataModelDTO> createDataModelJson(UserConnectorConfigDto userConnectorConfigDto, Connection con, JSONObject result) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        List<DataModelDTO> dataModelDTOS = new ArrayList<>();
        if (userConnectorConfigDto.getColumns() != null && !userConnectorConfigDto.getColumns().isEmpty()) {
            for (String table : userConnectorConfigDto.getColumns().keySet()) {
                List<DataModelProperty> dataModelProperties = new ArrayList<>();
                DataModelDTO response = new DataModelDTO();
                response.setWorkspaceName(userConnectorConfigDto.getWorkSpaceName());
                response.setMiniAppName(userConnectorConfigDto.getMiniAppName());
                response.setFileType("datamodel");
                result.put("Entity", table);
                response.setFileName(table);
                HashMap<String, String> fieldMap = new HashMap<>();

                try (ResultSet columns = metaData.getColumns(null, null, table, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String datatype = columns.getString("DATA_TYPE");
                        List<String> columnsName = userConnectorConfigDto.getColumns().get(table);
                        if (columnsName != null && !columnsName.isEmpty()) {
                            if (columnsName.contains(columnName)) {
                                DataModelProperty dataModelProperty = new DataModelProperty();
                                dataModelProperty.setName(columnName);
                                if (datatype.equals(DOUBLE_ORDINAL)) {
                                    fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                                    dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                                } else {
                                    fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype)).name());
                                    dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype)).name());
                                }
                                dataModelProperties.add(dataModelProperty);
                            }
                        }
                    }
                }
                result.put("Value", fieldMap);
                response.setDataModelProperties(dataModelProperties);
                dataModelDTOS.add(response);
            }
        }else {
            for (String table : userConnectorConfigDto.getTables()) {
                List<DataModelProperty> dataModelProperties = new ArrayList<>();
                DataModelDTO response = new DataModelDTO();
                response.setWorkspaceName(userConnectorConfigDto.getWorkSpaceName());
                response.setMiniAppName(userConnectorConfigDto.getMiniAppName());
                response.setFileType("datamodel");
                result.put("Entity", table);
                response.setFileName(table);
                HashMap<String, String> fieldMap = new HashMap<>();

                try (ResultSet columns = metaData.getColumns(null, null, table, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String datatype = columns.getString("DATA_TYPE");
                                DataModelProperty dataModelProperty = new DataModelProperty();
                                dataModelProperty.setName(columnName);
                                if (datatype.equals(DOUBLE_ORDINAL)) {
                                    fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                                    dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                                } else {
                                    fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype)).name());
                                    dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype)).name());
                                }
                                dataModelProperties.add(dataModelProperty);
                    }
                }
                result.put("Value", fieldMap);
                response.setDataModelProperties(dataModelProperties);
                dataModelDTOS.add(response);
            }
        }
        return dataModelDTOS;
    }

    public void savingConfigurationInDB(Response response, JSONObject connectorJson, UserConnectorConfigDto userConnectorConfigDto) {
        userConnectorConfigDto.setName(connectorJson.get("name").toString());
        JSONObject obj = new JSONObject((Map) response.readEntity(JSONObject.class).get("config"));
        userConnectorConfigDto.setConfiguration(obj.toString());
        userConnectorConfigDto.setDbPassword(encryptionService.encrypt(userConnectorConfigDto.getDbPassword()));
        userConnectorConfigRepository.persist(DtoHelper.convertFromUserConnectorConfigDto(userConnectorConfigDto));
    }

    public UserConnectorConfigDto findConnectorConfigById(ObjectId id) {
        UserConnectorConfig userConnectorConfigDb = userConnectorConfigRepository.findById(id);
        if (userConnectorConfigDb != null)
            return DtoHelper.convertToUserConnectorConfigDTO(userConnectorConfigDb);
        else
            return null;
    }

    public UserConnectorConfigDto findConnectorConfigByName(String name) {
        UserConnectorConfig userConnectorConfigDb = userConnectorConfigRepository.findByName(name);
        if (userConnectorConfigDb != null)
            return DtoHelper.convertToUserConnectorConfigDTO(userConnectorConfigDb);
        return null;
    }

    public long deleteConnector(String connectorName) {
        return userConnectorConfigRepository.deleteByName(connectorName);
    }

    public void updateConfig(JSONObject connectorJson, Response response, UserConnectorConfigDto userConnectorConfigDto) {
        try {
            userConnectorConfigDto.setName(connectorJson.get("name").toString());
            JSONObject obj = new JSONObject((Map) response.readEntity(JSONObject.class).get("config"));
            userConnectorConfigDto.setConfiguration(obj.toString());
            UserConnectorConfig userConnectorConfig = DtoHelper.convertFromUserConnectorConfigDto(userConnectorConfigDto);
            userConnectorConfigRepository.update(userConnectorConfig);
//            userConnectorConfigRepository.persistOrUpdate(userConnectorConfig);
            List<DataModelDTO> dataModelMapping = this.createDataModelMapping(userConnectorConfigDto);
            logger.debug("added model class into generated folder...");
            for (DataModelDTO dataModelDTO : dataModelMapping) {
                dataModelDTO.setFileName(Utility.createFileName(dataModelDTO.getFileName()));
                modellerService.addModeller(dataModelDTO).subscribe().with(
                        item -> {
                            logger.debug("added model for the object = " + dataModelDTO.getFileName());
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<UserConnectorConfigDto> findminiAppName(String workspacename) {
        List<UserConnectorConfigDto> userConnectorConfigDtos = new ArrayList<>();
        for (UserConnectorConfig userConnectorConfig : userConnectorConfigRepository.findByWorkSpaceName(workspacename)) {
            UserConnectorConfigDto userConnectorConfigDto = DtoHelper.convertToUserConnectorConfigDTO(userConnectorConfig);
            JSONObject status1 = connectorService.findStatus(userConnectorConfig.getName());
            HashMap connector = (HashMap) status1.get("connector");
            String connectorState = (String) connector.get("state");
            List<HashMap> tasks = (List<HashMap>) status1.get("tasks");
            String taskState = (String) tasks.get(0).get("state");
            userConnectorConfigDto.setConnectorStatus(connectorState);
            userConnectorConfigDto.setTasksStatus(taskState);
            userConnectorConfigDtos.add(userConnectorConfigDto);
        }
        return userConnectorConfigDtos;
    }

    public List<SourceSinkConnectorConfig> findByWorkSpaceName(String workspacename) {
        List<SourceSinkConnectorConfig> sourceSinkConnectorConfigs = new ArrayList<>();
        for (SourceSinkConnectorConfig sourceSinkConnectorConfig : sourceSinkConnectorConfigRepository.findByWorkSpaceName(workspacename)) {
            JSONObject sourceStatus = connectorService.findStatus(sourceSinkConnectorConfig.getSourceConfig().getName());
            JSONObject sinkStatus = connectorService.findStatus(sourceSinkConnectorConfig.getSourceConfig().getName());
            HashMap sourceConnector = (HashMap) sourceStatus.get("connector");
            HashMap sinkConnector = (HashMap) sinkStatus.get("connector");
            String sourceConnectorState = (String) sourceConnector.get("state");
            String sinkConnectorState = (String) sinkConnector.get("state");
            List<HashMap> sourceTasks = (List<HashMap>) sourceStatus.get("tasks");
            List<HashMap> sinkTasks = (List<HashMap>) sinkStatus.get("tasks");
            String sourceTaskState = (String) sourceTasks.get(0).get("state");
            String sinkTaskState = (String) sinkTasks.get(0).get("state");
            sourceSinkConnectorConfig.getConnectorStatus().setSourceConnectorState(sourceConnectorState);
            sourceSinkConnectorConfig.getConnectorStatus().setSourceConnectorTaskStatus(sourceTaskState);
            sourceSinkConnectorConfig.getConnectorStatus().setSinkConnectorState(sinkConnectorState);
            sourceSinkConnectorConfig.getConnectorStatus().setSinkConnectorTaskStatus(sinkTaskState);
            sourceSinkConnectorConfigs.add(sourceSinkConnectorConfig);
        }
        return sourceSinkConnectorConfigs;
    }

    public Uni<EventResponseModel> addSourceConnector(UserConnectorConfigDto userConnectorConfigDto) throws SQLException, ClassNotFoundException {
        JSONObject connectorJson = null;
        userConnectorConfigDto = helperFunctionToFillDetailsFromDbConfig(userConnectorConfigDto);
        EventResponseModel eventResponseModel = new EventResponseModel();
        connectorJson = this.createSourceConnectorJson(userConnectorConfigDto);
        logger.debug("config json = "+connectorJson.toJSONString());
        try {
            Response connectorResponse = connectorService.addConnector(connectorJson);

            if (connectorResponse.getStatus() == RestResponse.StatusCode.CREATED) {
                this.savingConfigurationInDB(connectorResponse, connectorJson, userConnectorConfigDto);
                // create data model mapping
               // userConnectorConfigDto.setHost("localhost");
                List<DataModelDTO> dataModelMapping = this.createDataModelMapping(userConnectorConfigDto);
                if(dataModelMapping!=null) {
                    logger.debug("added model class into generated folder...");
                    for (DataModelDTO dataModelDTO : dataModelMapping) {
                        dataModelDTO.setFileName(Utility.createFileName(dataModelDTO.getFileName()));
                        modellerService.addModeller(dataModelDTO).subscribe().with(
                                item -> {
                                    logger.debug("added model for the object = " + dataModelDTO.getFileName());
                                }
                        );
                    }
                }
                eventResponseModel.setMessage("added connector and created model..");
                eventResponseModel.setData(RestResponse.StatusCode.OK);
            }
        } catch (ClientWebApplicationException exception) {
            if (exception.getResponse().getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("connector with same name is present");
                eventResponseModel.setMessage("connector with same name is present");
                eventResponseModel.setData(RestResponse.StatusCode.CONFLICT);
                return Uni.createFrom().item(eventResponseModel);
            } else if (exception.getResponse().getStatus() == RestResponse.StatusCode.INTERNAL_SERVER_ERROR) {
                logger.debug("issue with connector api");
                eventResponseModel.setMessage("issue with connector api");
                eventResponseModel.setData(RestResponse.StatusCode.INTERNAL_SERVER_ERROR);
                return Uni.createFrom().item(eventResponseModel);
            }else{
                exception.printStackTrace();
                eventResponseModel.setMessage("api response error : "+exception.getMessage());
                eventResponseModel.setData(exception.getResponse().getStatus());
               return Uni.createFrom().item(eventResponseModel);
            }
        }
        return Uni.createFrom().item(eventResponseModel);
    }

    private void createTopicForSftpSource(UserConnectorConfigDto userConnectorConfigDto) {
        helper(userConnectorConfigDto);
    }

    private void helper(UserConnectorConfigDto userConnectorConfigDto) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = jsch.getSession("Akhil", "172.31.32.1", 22);
            session.setPassword("8285");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.cd("/SFTPRoot/");
            Vector<ChannelSftp.LsEntry> fileList = channel.ls(".");
            for (ChannelSftp.LsEntry entry : fileList) {
                if (!entry.getAttrs().isDir()) {
                    System.out.println(entry.getFilename());
                }
            }
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private UserConnectorConfigDto helperFunctionToFillDetailsFromDbConfig(UserConnectorConfigDto userConnectorConfigDto) {
        DbConnectionDetails byConnectionName = dbConnectionDetailsRepository.findByConnectionName(userConnectorConfigDto.getConnectionName());
        if(byConnectionName!=null){
            userConnectorConfigDto.setConnector(byConnectionName.getDb());
            userConnectorConfigDto.setDbName(byConnectionName.getDbName());
            userConnectorConfigDto.setDbUser(byConnectionName.getDbUser());
            userConnectorConfigDto.setDbPassword(encryptionService.decrypt(byConnectionName.getDbPassword()));
            userConnectorConfigDto.setHost(byConnectionName.getHost());
            userConnectorConfigDto.setPort(byConnectionName.getPort());
            userConnectorConfigDto.setMiniAppName(byConnectionName.getMiniAppName());
            userConnectorConfigDto.setWorkSpaceName(byConnectionName.getWorkSpaceName());
        }
        return userConnectorConfigDto;
    }

    public Uni<EventResponseModel> updateConnector(String name, UserConnectorConfigDto userConnectorConfigDto) {
        EventResponseModel responseModel = new EventResponseModel();
        Response response = null;
        JSONObject connectorJson = null;
        try {
            if (userConnectorConfigDto.getConnectorType().equals(ConnectorEnum.SOURCE.toString())) {
                connectorJson = this.createSourceConnectorJson(userConnectorConfigDto);
            } else if (userConnectorConfigDto.getConnectorType().equals(ConnectorEnum.SINK.toString())) {
                connectorJson = this.createSinkConnectorJson(userConnectorConfigDto);
            }
            response = connectorService.updateConnector(name,(JSONObject) connectorJson.get("config") );
            logger.debug("successfully updated connector " + response);
            if (response.getStatus() == RestResponse.StatusCode.OK) {
                this.updateConfig(connectorJson, response, userConnectorConfigDto);
                logger.debug("saved configuration into the db");
            } else if (response.getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("connector with same name is present");
                responseModel.setMessage("connector with same name is present");
                return Uni.createFrom().item(responseModel);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseModel.setMessage("some problem occurred with the given input with error : " + e.getMessage());
            return Uni.createFrom().item(responseModel);
        }
        responseModel.setMessage("connector updated....");
        return Uni.createFrom().item(responseModel);
    }

    private JSONObject convertJson(JSONObject response) {
        JSONObject result = new JSONObject();

        result.remove("name");
        return (JSONObject) result.get("config");
    }

    public ResponseUtil getStatus(String connector) {
        ResponseUtil response = new ResponseUtil();
        JSONObject result = null;
        try {
            result = connectorService.findStatus(connector);
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("some issue comes .." + e.getMessage());
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return response;
        }
        response.setData(result);
        return response;
    }

    public ResponseUtil getStatusOneClick(String connector) {
        ResponseUtil response = new ResponseUtil();
        JSONObject result = null;
        try {
            result = connectorService.findStatus(connector);
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("some issue comes .." + e.getMessage());
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return response;
        }
        response.setData(result);
        return response;
    }

    public ResponseUtil pauseConnector(String connector) {
        ResponseUtil response = new ResponseUtil();
        try {
            connectorService.pauseConnector(connector);
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("some issue came with the error : "+e.getMessage());
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return response;
        }
        response.setMessage("paused the connector...");
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    public ResponseUtil resumeConnector(String connector) {
        ResponseUtil response = new ResponseUtil();
        try {
            connectorService.resumeConnector(connector);
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("Some issue came with the error : "+e.getMessage());
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return response;
        }
        response.setMessage("resumed the connector...");
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    public ResponseUtil deleteConfig(String connectorName) {
        ResponseUtil response = new ResponseUtil();
        Response connectorResponse = null;
        try {
            UserConnectorConfigDto connectorConfigDb = this.findConnectorConfigByName(connectorName);
            if (connectorConfigDb != null) {
                connectorResponse = connectorService.deleteConnector(connectorConfigDb.getName());
                if (connectorResponse.getStatus() == 204) {
                    long result = this.deleteConnector(connectorName);
                    if (result == 1) {
                        response.setMessage("Connector deleted successfully with name  : " + connectorName);
                        response.setStatusCode(Response.Status.OK.getStatusCode());
                    }
                }
            } else {
                response.setMessage("no such connector is present in db with name  : " + connectorName);
                response.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            }

        } catch (ClientWebApplicationException clientWebApplicationException) {
            clientWebApplicationException.printStackTrace();
            if (clientWebApplicationException.getResponse().getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("re-balance is in process");
                response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
                response.setMessage("re-balance is in process can not delete this connector for now..");
            }
        }
        return response;
    }

    public ResponseUtil deleteConfigOneClick(String connectorName) {
        ResponseUtil response = new ResponseUtil();
        Response sourceConnectorResponse = null;
        Response sinkConnectorResponse = null;
        try {
            SourceSinkConnectorConfig connectorConfigDb = this.findConnectorConfigByNameOneClick(connectorName);
            if (connectorConfigDb != null) {
                sourceConnectorResponse = connectorService.deleteConnector(connectorConfigDb.getSourceConfig().getName());
                sinkConnectorResponse = connectorService.deleteConnector(connectorConfigDb.getSinkConfig().getName());
                if (sourceConnectorResponse.getStatus() == 204 && sinkConnectorResponse.getStatus() == 204) {
                    long result = this.deleteConnectorOneClick(connectorName);
                    if (result == 1) {
                        response.setMessage("Connector deleted successfully with name  : " + connectorName);
                        response.setStatusCode(Response.Status.OK.getStatusCode());
                    }
                }
            } else {
                response.setMessage("no such connector is present in db with name  : " + connectorName);
                response.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
            }

        } catch (ClientWebApplicationException clientWebApplicationException) {
            clientWebApplicationException.printStackTrace();
            if (clientWebApplicationException.getResponse().getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("re-balance is in process");
                response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
                response.setMessage("re-balance is in process can not delete this connector for now..");
            }
        }
        return response;
    }

    private long deleteConnectorOneClick(String connectorName) {
        return sourceSinkConnectorConfigRepository.deleteByName(connectorName);
    }

    private SourceSinkConnectorConfig findConnectorConfigByNameOneClick(String connectorName) {
        SourceSinkConnectorConfig sourceSinkConnectorConfig = sourceSinkConnectorConfigRepository.findByName(connectorName);
        if (sourceSinkConnectorConfig != null)
            return sourceSinkConnectorConfig;
        return null;
    }

    public ResponseUtil getConnectorByName(String name) {
        ResponseUtil response = new ResponseUtil();
        try {
            UserConnectorConfigDto connectorDb = this.findConnectorConfigByName(name);
            if (connectorDb != null) {
                response.setStatusCode(Response.Status.OK.getStatusCode());
                response.setData(connectorDb);
            } else {
                response.setMessage("No data present...");
                response.setStatusCode(Response.Status.NO_CONTENT.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            response.setMessage("Some error comes : " + e.getMessage());
        }
        return response;
    }

    public ResponseUtil getConnectorByNameOneClick(String name) {
        ResponseUtil response = new ResponseUtil();
        try {
            SourceSinkConnectorConfig sourceSinkConnectorConfig = this.findConnectorConfigByNameOneClick(name);
            if (sourceSinkConnectorConfig != null) {
                response.setStatusCode(Response.Status.OK.getStatusCode());
                response.setData(sourceSinkConnectorConfig);
            } else {
                response.setMessage("No data present...");
                response.setStatusCode(Response.Status.NO_CONTENT.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            response.setMessage("Some error comes : " + e.getMessage());
        }
        return response;
    }

    public Response getConnectorById(ObjectId id) {
        Response response = null;
        try {
            UserConnectorConfigDto connectorDb = this.findConnectorConfigById(id);
            if (connectorDb != null) {
                logger.debug("found the connector config = " + connectorDb.toString());
                response = Response.ok(connectorDb).build();
            } else {
                response = Response.ok("No Such Connector present in db with this id = " + id).build();
                logger.debug("no connector config found....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = Response.ok(e.getMessage()).build();
        }
        return response;
    }

    public Uni<EventResponseModel> addSinkConnector(UserConnectorConfigDto userConnectorConfigDto) throws SQLException, ClassNotFoundException {
        JSONObject connectorJson = null;
        EventResponseModel eventResponseModel = new EventResponseModel();
        userConnectorConfigDto = helperFunctionToFillDetailsFromDbConfig(userConnectorConfigDto);
        connectorJson = this.createSinkConnectorJson(userConnectorConfigDto);
        try {
            Response connectorResponse = connectorService.addConnector(connectorJson);
            if (connectorResponse.getStatus() == RestResponse.StatusCode.CREATED) {
                this.savingConfigurationInDB(connectorResponse, connectorJson, userConnectorConfigDto);
                eventResponseModel.setMessage("added sink connector...");
                eventResponseModel.setData(Response.Status.OK.getStatusCode());
            }
        } catch (ClientWebApplicationException clientWebApplicationException) {
            if (clientWebApplicationException.getResponse().getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("connector with same name is present");
                eventResponseModel.setMessage("connector with same name is present");
                eventResponseModel.setData(Response.Status.CONFLICT.getStatusCode());
                return Uni.createFrom().item(eventResponseModel);
            } else if (clientWebApplicationException.getResponse().getStatus() == RestResponse.StatusCode.INTERNAL_SERVER_ERROR) {
                logger.debug("issue with connector api");
                eventResponseModel.setMessage("issue with connector api "+clientWebApplicationException.getLocalizedMessage());
                eventResponseModel.setData(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                return Uni.createFrom().item(eventResponseModel);
            }
        } catch (NullPointerException nullPointerException) {
            logger.debug("issue with connector api");
            eventResponseModel.setMessage("issue with connector api with null pointer "+nullPointerException.getMessage());
            return Uni.createFrom().item(eventResponseModel);
        }
        return Uni.createFrom().item(eventResponseModel);
    }

    public List<DataModelDTO> getTableStructures(UserConnectorConfigDto userConnectorConfigDto) {
        List<DataModelDTO> result = null;
        DbConnectionDetails byConnectionName = dbConnectionDetailsRepository.findByConnectionName(userConnectorConfigDto.getConnectionName());


            try {
                if(byConnectionName!=null) {
                    byConnectionName.setDbPassword(encryptionService.decrypt(byConnectionName.getDbPassword()));
                    userConnectorConfigDto = helperConverter(userConnectorConfigDto,DtoHelper.convertDbDetailsDtoToUserConnectorConfigDto(byConnectionName));
//                    if (userConnectorConfigDto.getConnectorType().equals(ConnectorEnum.SOURCE.toString())) {
                        if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createMysqlConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createPostgresConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createMONGODbConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ORACLE.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createOracleConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createSqlServerConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.VITESS.toString())) {
                            // to be implemented

                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ELASTIC_SEARCH.toString())) {
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createCassandraConnectionUrl(userConnectorConfigDto));
                        } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.DB2.toString())) {
                            result = getTableStructure(userConnectorConfigDto, ConnectionClass.createDB2ConnectionUrl(userConnectorConfigDto));
                        }
//                    }
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                Response.ok(sqlException.getErrorCode(), "connection failed with below error : " + sqlException.getMessage()).build();
            }
            return result;
        }

    private UserConnectorConfigDto helperConverter(UserConnectorConfigDto older, UserConnectorConfigDto fromDb) {
        older.setConnector(fromDb.getConnector());
        older.setConnectionName(fromDb.getConnectionName());
        older.setId(fromDb.getId());
        older.setDbUser(fromDb.getDbUser());
        older.setDbPassword(fromDb.getDbPassword());
        older.setHost(fromDb.getHost());
        older.setPort(fromDb.getPort());
        older.setWorkSpaceName(fromDb.getWorkSpaceName());
        older.setMiniAppName(fromDb.getMiniAppName());
        older.setDbName(fromDb.getDbName());

        return older;
    }

    private List<DataModelDTO> getTableStructure(UserConnectorConfigDto userConnectorConfigDto, Connection con) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        List<DataModelDTO> dataModelDTOS = new ArrayList<>();
        if (userConnectorConfigDto.getTables() != null && !userConnectorConfigDto.getTables().isEmpty()) {
            for (String table : userConnectorConfigDto.getTables()) {
                List<DataModelProperty> dataModelProperties = new ArrayList<>();
                DataModelDTO response = new DataModelDTO();
                response.setWorkspaceName(userConnectorConfigDto.getWorkSpaceName());
                response.setMiniAppName(userConnectorConfigDto.getMiniAppName());
                response.setFileType("datamodel");
                response.setFileName(table);
                HashMap<String, String> fieldMap = new HashMap<>();

                try (ResultSet columns = metaData.getColumns(null, null, table, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String datatype = columns.getString("DATA_TYPE");
                        DataModelProperty dataModelProperty = new DataModelProperty();
                        dataModelProperty.setName(columnName);
                        if (datatype.equals(DOUBLE_ORDINAL)) {
                            fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                            dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype) - 1).name());
                        } else {
                            fieldMap.put(columnName, JDBCType.valueOf(Integer.valueOf(datatype)).name());
                            dataModelProperty.setType(JDBCType.valueOf(Integer.valueOf(datatype)).name());
                        }
                        dataModelProperties.add(dataModelProperty);
                    }
                }
                response.setDataModelProperties(dataModelProperties);
                dataModelDTOS.add(response);
            }
        }
        return dataModelDTOS;
    }

    public ResponseUtil getTopics(String connector) {
        ResponseUtil responseUtil = new ResponseUtil();
        JSONObject topics = connectorService.getTopics(connector);
        System.out.println(topics);
        responseUtil.setStatusCode(Response.Status.OK.getStatusCode());
        responseUtil.setData(topics);
        return responseUtil;
    }

    public ResponseUtil resetTopics(String connector) {
        ResponseUtil responseUtil = new ResponseUtil();
        JSONObject topics = connectorService.resetTopics(connector);
        System.out.println(topics);
        responseUtil.setStatusCode(Response.Status.OK.getStatusCode());
        responseUtil.setData(topics);
        return responseUtil;
    }

    public List<String> getTables(String connectionName) {
        List<String> tables = new ArrayList<>();
        try {
            DbConnectionDetails connectionDetailsByConnectionName = dbConnectionDetailsRepository.findByConnectionName(connectionName);
            if(connectionDetailsByConnectionName!=null) connectionDetailsByConnectionName.setDbPassword(encryptionService.decrypt(connectionDetailsByConnectionName.getDbPassword()));
            if(connectionDetailsByConnectionName!=null && connectionDetailsByConnectionName.getDb().equals(ConnectorEnum.MYSQL.toString())) {
                Connection mysqlConnectionUrl = ConnectionClass.createMysqlConnectionUrl(DtoHelper.convertDbDetailsDtoToUserConnectorConfigDto(connectionDetailsByConnectionName));
                DatabaseMetaData metaData = mysqlConnectionUrl.getMetaData();
                String[] tableTypes = {"TABLE"};
                ResultSet resultSet = metaData.getTables(null, null, null, tableTypes);

                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }else if(connectionDetailsByConnectionName!=null && connectionDetailsByConnectionName.getDb().equals(ConnectorEnum.POSTGRES.toString())){
                Connection postgresConnectionUrl = ConnectionClass.createPostgresConnectionUrl(DtoHelper.convertDbDetailsDtoToUserConnectorConfigDto(connectionDetailsByConnectionName));
                DatabaseMetaData metaData = postgresConnectionUrl.getMetaData();
                String[] tableTypes = {"TABLE"};
                ResultSet resultSet = metaData.getTables(null, null, null, tableTypes);

                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tables;
    }

    public Map<String, String> findStatus(String workspacename) {
        Map<String,String> status = new HashMap<>();
        List<UserConnectorConfigDto> userConnectorConfigDtos = new ArrayList<>();
        for (UserConnectorConfig userConnectorConfig : userConnectorConfigRepository.findByWorkSpaceName(workspacename))
            userConnectorConfigDtos.add(DtoHelper.convertToUserConnectorConfigDTO(userConnectorConfig));
        for(UserConnectorConfigDto userConnectorConfigDto:userConnectorConfigDtos){
            JSONObject status1 = connectorService.findStatus(userConnectorConfigDto.getName());
            HashMap connector = (HashMap)status1.get("connector");
            String state = (String) connector.get("state");
            status.put(userConnectorConfigDto.getName(),state);
        }
        return status;
    }

    public Uni<EventResponseModel> addConnectorOneClick(SourceSinkConnectorConfig sourceSinkConnectorConfig,SinkConnectionDetails sinkConnectionDetails) {
        configErrors.clear();
        EventResponseModel eventResponseModel = new EventResponseModel();
        this.sinkConnectionDetails = sinkConnectionDetails;
        JSONObject connectorJson = null;
        sinkCreationFlag = true;

//        if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.CDC.toString()) && topicNameValidation(sourceSinkConnectorConfig.getSourceConfig())){
//            eventResponseModel.setMessage("Please choose different Topic Prefix or server Name");
//            eventResponseModel.setStatus(409);
//            return Uni.createFrom().item(eventResponseModel);
//        }else if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.JDBC.toString()) && topicValidationJdbc(sourceSinkConnectorConfig.getSourceConfig())){
//            eventResponseModel.setMessage("Please choose different Topic Prefix or server Name");
//            eventResponseModel.setStatus(409);
//            return Uni.createFrom().item(eventResponseModel);
//        }else if(sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.SFTP.toString()) && topicValidationSftp(sourceSinkConnectorConfig.getSourceConfig())){
//            eventResponseModel.setMessage("Please choose different Topic Name for SFTP.");
//            eventResponseModel.setStatus(409);
//            return Uni.createFrom().item(eventResponseModel);
//        }
        SourceConfig sourceConfig = helperFunctionToFillDetailsFromDb(sourceSinkConnectorConfig.getSourceConfig());
        if(sourceConfig==null){
            eventResponseModel.setMessage("No Connection Details found for this connection Name = "+sourceSinkConnectorConfig.getSourceConfig().getConnectionName());
            eventResponseModel.setStatus(404);
            return Uni.createFrom().item(eventResponseModel);
        }
        sourceSinkConnectorConfig.setSourceConfig(sourceConfig);
        connectorJson = this.createSourceConnectorJsonForOneClick(sourceSinkConnectorConfig);
        logger.debug("config json = "+connectorJson.toJSONString());
        String connectorName  = (String)connectorJson.get("name");
        String topicName = sourceSinkConnectorConfig.getSourceConfig().getTopics();

        try {

            if(invalidConfiguration(connectorJson,ConnectorEnum.SOURCE.toString())){
                eventResponseModel.setMessage("invalid source configuration");
                eventResponseModel.setStatus(400);
                if(!configErrors.isEmpty()){
                    eventResponseModel.setData(configErrors);
                }
                return Uni.createFrom().item(eventResponseModel);
            }
            Response connectorResponse = connectorService.addConnector(connectorJson);

            if (connectorResponse.getStatus() == RestResponse.StatusCode.CREATED) {
                Response sinkConnectorResponse =null;
                if(connectorStates(connectorName)){
                    sourceSinkConnectorConfig.getSourceConfig().setName(connectorName);
                    if(addSinkConnectorOnOneClick(sourceSinkConnectorConfig)){
                        this.savingConfigurationInDBForOneClick(connectorResponse, connectorJson, sourceSinkConnectorConfig);
                        eventResponseModel.setMessage("added source and sink connector");
                        eventResponseModel.setData(RestResponse.StatusCode.OK);
                        eventResponseModel.setStatus(200);
                        return Uni.createFrom().item(eventResponseModel);
                    }
                }else{
                    deleteConfigOneClick(connectorName);
                    eventResponseModel.setMessage("Please try again...");
                    eventResponseModel.setStatus(503);
                    eventResponseModel.setData(RestResponse.StatusCode.INTERNAL_SERVER_ERROR);
                    return Uni.createFrom().item(eventResponseModel);
                }
            }
        } catch (ClientWebApplicationException exception) {
            deleteConfigOneClick(connectorName);
            if (exception.getResponse().getStatus() == RestResponse.StatusCode.CONFLICT) {
                logger.debug("connector with same name is present");
                eventResponseModel.setMessage("connector with same name is present");
                eventResponseModel.setData(exception.getMessage());
                eventResponseModel.setStatus(409);
                return Uni.createFrom().item(eventResponseModel);
            } else if (exception.getResponse().getStatus() == RestResponse.StatusCode.INTERNAL_SERVER_ERROR) {
                logger.debug("issue with connector api");
                eventResponseModel.setMessage("issue with connector api");
                eventResponseModel.setData(exception.getMessage());
                eventResponseModel.setStatus(503);
                return Uni.createFrom().item(eventResponseModel);
            }else{
                exception.printStackTrace();
                eventResponseModel.setMessage("api response error : "+exception.getMessage());
                eventResponseModel.setData(exception.getResponse().getStatus());
                eventResponseModel.setStatus(503);
                return Uni.createFrom().item(eventResponseModel);
            }
        }
        if(!sinkCreationFlag){
            deleteConfigOneClick(connectorName);
            try (AdminClient adminClient = getKafkaClient()) {
                DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singleton(topicName));
                deleteTopicsResult.all().get();
                System.out.println("Topic deleted successfully: " + topicName);
                eventResponseModel.setMessage("Please try again...sink creation failed please check your sink configurations.");
                eventResponseModel.setData(RestResponse.StatusCode.INTERNAL_SERVER_ERROR);
                eventResponseModel.setStatus(503);
            } catch (ExecutionException | InterruptedException e) {
                eventResponseModel.setMessage("Please try again...something went wrong");
                eventResponseModel.setData(e.getMessage());
                eventResponseModel.setStatus(503);
            }

        }
        return Uni.createFrom().item(eventResponseModel);
    }

    private boolean invalidConfiguration(JSONObject connectorJson, String connector) {
        JSONObject newJson = (JSONObject) connectorJson.get("config");
        newJson.put("name",(String)connectorJson.get("name"));
        String s = (String) newJson.get("connector.class");
        JSONObject jsonObject = connectorService.validateConnector(s, newJson);
        if(((BigDecimal) jsonObject.get("error_count")).intValue()>0){
            HashMap<String,List<String>> mapErrors = new HashMap<>();
            helperToFillErrors(jsonObject,mapErrors);
            if(connector.equals(ConnectorEnum.SOURCE.toString())){
                configErrors.put("SOURCE-CONNECTOR-ERRORS",mapErrors);
            }else{
                configErrors.put("SINK-CONNECTOR-ERRORS",mapErrors);
            }
            return true;
        }else{
            return false;
        }
    }

    private void helperToFillErrors(JSONObject jsonObject, HashMap<String, List<String>> mapErrors) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(jsonObject.toString());

            String name = jsonNode.get("name").asText();

            JsonNode configsNode = jsonNode.get("configs");
            if (configsNode != null && configsNode.isArray()) {
                for (JsonNode configNode : configsNode) {
                    JsonNode valueNode = configNode.get("value");
                    if (valueNode != null) {
                        String valueName = valueNode.get("name").asText();
                        List<String> errors = new ArrayList<>();
                        JsonNode errorsNode = valueNode.get("errors");
                        if (errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty()) {
                            for (JsonNode errorNode : errorsNode) {
                                errors.add(errorNode.asText());
                            }
                            mapErrors.put(valueName, errors);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean topicValidationJdbc(SourceConfig sourceConfig) {
        AdminClient kafkaClient = getKafkaClient();
        String topicName = sourceConfig.getTopicsPrefix()+sourceConfig.getTables().get(0);
        try {
            Set<String> strings = kafkaClient.listTopics().names().get();
            if(strings.contains(topicName)){
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            return true;
        }
        return false;
    }

    private boolean topicValidationSftp(SourceConfig sourceConfig) {
        AdminClient kafkaClient = getKafkaClient();
        String topicName = sourceConfig.getTopics();
        try {
            Set<String> strings = kafkaClient.listTopics().names().get();
            if(strings.contains(topicName)){
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            return true;
        }
        return false;
    }

//    private boolean sinkCollectionValidation(SinkConfig sinkConfig) {
//        String connectionLinkMongoDB = createConnectionLinkMongoDB(SinkConnectionDetails.Host, SinkConnectionDetails.Port, sinkConfig.getDbName(), SinkConnectionDetails.DbUser, SinkConnectionDetails.DbPassword);
//
//        try (MongoClient mongoClient = MongoClients.create(connectionLinkMongoDB)) {
//            // Connect to the MongoDB server
//            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
//
//            // Iterate over the database names
//            for (String dbName : databaseNames) {
//                if (dbName.equals(sinkConfig.getDbName())) {
//                    // The database exists
//                    return true;
//                }
//            }
//        } catch (Exception e) {
//            // Handle any exceptions
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }

    private boolean topicNameValidation(SourceConfig sourceConfig) {
        AdminClient kafkaClient = getKafkaClient();
        String topicName = extractTopicName(sourceConfig);
        String dbServerName = extractDbServerName(sourceConfig);
        try {
            Set<String> strings = kafkaClient.listTopics().names().get();
            if(strings.contains(topicName) || strings.contains(dbServerName)){
               return true;
           }
        } catch (InterruptedException | ExecutionException e) {
            return true;
        }
        return false;
    }

    private String extractDbServerName(SourceConfig sourceConfig) {
        StringBuilder dbServerName = new StringBuilder();
        dbServerName.append(sourceConfig.getTopicsPrefix()).append(".").append(sourceConfig.getDbServer());
        return dbServerName.toString();
    }

    private String extractTopicName(SourceConfig sourceConfig) {
        StringBuilder topic = new StringBuilder();
        topic.append(sourceConfig.getTopicsPrefix()).append(".").append(sourceConfig.getDbServer()).append(".").append(sourceConfig.getDbName()).append(".").append(sourceConfig.getTables().get(0));
        return topic.toString();
    }

    private AdminClient getKafkaClient(){
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, sinkConnectionDetails.getKafkaHost()+":"+sinkConnectionDetails.getKafkaPort());
        return AdminClient.create(properties);
    }


    private boolean addSinkConnectorOnOneClick(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
            Response connectorResponse = null;
            JSONObject connectorJson = null;
        String connectorName=null;
        sourceSinkConnectorConfig.setSinkConfig(helperFunctionToFillDetailsFromDbInSink(sourceSinkConnectorConfig.getSinkConfig()));
            if (sourceSinkConnectorConfig.getSinkConfig().getConnectorFamily().equals(ConnectorEnum.CDC.toString())) {
                connectorJson = createSinkMongodbJsonOnOneClickCDC(sourceSinkConnectorConfig);
                connectorName= (String) connectorJson.get("name");
            } else if (sourceSinkConnectorConfig.getSinkConfig().getConnectorFamily().equals(ConnectorEnum.JDBC.toString())) {
                connectorJson = createSinkMongodbJsonOnOneClickJDBC(sourceSinkConnectorConfig);
                connectorName= (String) connectorJson.get("name");
            } else if (sourceSinkConnectorConfig.getSinkConfig().getConnectorFamily().equals(ConnectorEnum.SFTP.toString())) {
                connectorJson = createSinkMongodbJsonOnOneClickSFTP(sourceSinkConnectorConfig);
                connectorName= (String) connectorJson.get("name");
            }
        try{
            if(invalidConfiguration(connectorJson,ConnectorEnum.SINK.toString())){
                sinkCreationFlag = false;
               return false;
            }
            connectorResponse = connectorService.addConnector(connectorJson);
            if (connectorResponse.getStatus() == RestResponse.StatusCode.CREATED) {
                if (connectorStates(connectorName)) {
                    sourceSinkConnectorConfig.getSinkConfig().setName(connectorName);
                    return true;
                } else {
                    sinkCreationFlag = false;
                    deleteConfigOneClick(connectorName);
                }
            }
        }catch (Exception e){
            sinkCreationFlag = false;
        }
        return false;
    }

    private SinkConfig helperFunctionToFillDetailsFromDbInSink(SinkConfig sinkConfig) {
        sinkConfig.setConnector(sinkConnectionDetails.getConnector());
        sinkConfig.setDbName(sinkConfig.getDbName());
        sinkConfig.setDbUser(sinkConnectionDetails.getDbUser());
        sinkConfig.setDbPassword(sinkConnectionDetails.getDbPassword());
        sinkConfig.setHost(sinkConnectionDetails.getHost());
        sinkConfig.setPort(sinkConnectionDetails.getPort());
        sinkConfig.setMiniAppName(sinkConnectionDetails.getMiniAppName());
        sinkConfig.setWorkSpaceName(sinkConnectionDetails.getWorkSpaceName());
        return sinkConfig;
    }

    private JSONObject createSinkMongodbJsonOnOneClickSFTP(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        //        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MONGODB_SINK_SFTP);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSinkConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSinkConfig().getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",createSinkDbName(sourceSinkConnectorConfig.getSinkConfig().getDbName(),sourceSinkConnectorConfig.getWorkSpaceName()));
            if(sourceSinkConnectorConfig.getSinkConfig().getTables()!=null && !sourceSinkConnectorConfig.getSinkConfig().getTables().isEmpty())
                configObject.put("collection",sourceSinkConnectorConfig.getSinkConfig().getTables().get(0).replace(".java",""));
            configObject.put("topics", sourceSinkConnectorConfig.getSourceConfig().getTopics());
            configObject.put("connection.uri", createConnectionLinkMongoDB(sinkConnectionDetails.getHost(), sinkConnectionDetails.getPort(), sinkConnectionDetails.getDbName(), sinkConnectionDetails.getDbUser(), sinkConnectionDetails.getDbPassword()));
            if(sourceSinkConnectorConfig.getTransforms()!=null && !sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames().isEmpty()){
                configObject.put("transforms", "RenameField");
                configObject.put("transforms.RenameField.type","org.apache.kafka.connect.transforms.ReplaceField$Value");
                configObject.put("transforms.RenameField.renames",helperToCreateRenameString(sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames()));
            }
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String helperToCreateRenameString(HashMap<String, String> renameFieldsNames) {
        StringBuilder renameFieldsString = new StringBuilder();
        for (Map.Entry<String, String> entry : renameFieldsNames.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            renameFieldsString.append(key).append(":").append(value).append(",");
        }

        // Remove the trailing comma
        if (renameFieldsString.length() > 0) {
            renameFieldsString.deleteCharAt(renameFieldsString.length() - 1);
        }
        return renameFieldsString.toString();
    }

    private JSONObject createSinkMongodbJsonOnOneClickJDBC(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        //        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MONGODB_SINK_JDBC);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSinkConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSinkConfig().getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",createSinkDbName(sourceSinkConnectorConfig.getSinkConfig().getDbName(),sourceSinkConnectorConfig.getWorkSpaceName()));
            if(sourceSinkConnectorConfig.getSinkConfig().getTables()!=null && !sourceSinkConnectorConfig.getSinkConfig().getTables().isEmpty())
                configObject.put("collection",sourceSinkConnectorConfig.getSinkConfig().getTables().get(0).replace(".java",""));
            configObject.put("topics",sourceSinkConnectorConfig.getSourceConfig().getTopicsPrefix()+sourceSinkConnectorConfig.getSourceConfig().getTables().get(0));
            configObject.put("connection.uri", createConnectionLinkMongoDB(sinkConnectionDetails.getHost(), sinkConnectionDetails.getPort(),sinkConnectionDetails.getDbName(), sinkConnectionDetails.getDbUser(), sinkConnectionDetails.getDbPassword()));
            if(sourceSinkConnectorConfig.getTransforms()!=null && !sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames().isEmpty()){
                configObject.put("transforms", "RenameField");
                configObject.put("transforms.RenameField.type","org.apache.kafka.connect.transforms.ReplaceField$Value");
                configObject.put("transforms.RenameField.renames",helperToCreateRenameString(sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames()));
            }
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createJdbcSinkTopic(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        StringBuilder stringBuilder = new StringBuilder();
       return stringBuilder.append(sourceSinkConnectorConfig.getSinkConfig().getTopics()).append(sourceSinkConnectorConfig.getSourceConfig().getTables().get(0)).toString();
    }

    private JSONObject createSinkMongodbJsonOnOneClickCDC(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        //        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MONGODB_SINK_CDC);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSinkConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSinkConfig().getConnectorType());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database",createSinkDbName(sourceSinkConnectorConfig.getSinkConfig().getDbName(),sourceSinkConnectorConfig.getWorkSpaceName()));
            if(sourceSinkConnectorConfig.getSinkConfig().getTables()!=null && !sourceSinkConnectorConfig.getSinkConfig().getTables().isEmpty())
                configObject.put("collection",sourceSinkConnectorConfig.getSinkConfig().getTables().get(0).replace(".java",""));
            configObject.put("topics", extractTopicName(sourceSinkConnectorConfig.getSourceConfig()));
            configObject.put("connection.uri", createConnectionLinkMongoDB(sinkConnectionDetails.getHost(), sinkConnectionDetails.getPort(), sinkConnectionDetails.getDbName(), sinkConnectionDetails.getDbUser(), sinkConnectionDetails.getDbPassword()));
            if(sourceSinkConnectorConfig.getTransforms()!=null && !sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames().isEmpty()){
                configObject.put("transforms", "renameField");
                configObject.put("transforms.renameField.type","com.example.transforms.FieldRenameTransformation");
                configObject.put("transforms.renameField.renames",helperToCreateRenameString(sourceSinkConnectorConfig.getTransforms().getRenameFieldsNames()));
            }
            System.out.println(jsonObject.toJSONString());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createSinkDbName(String dbName,String workspace) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(workspace).append("-").append(dbName).toString();
    }

    private boolean connectorStates(String connectorName) {
        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject status1 = connectorService.findStatus(connectorName);
        HashMap connector = (HashMap) status1.get("connector");
        String connectorState = (String) connector.get("state");
        List<HashMap> tasks = (List<HashMap>) status1.get("tasks");
        if(tasks.isEmpty()){
            return false;
        }
        String taskState = (String) tasks.get(0).get("state");
        if(taskState.equals("RUNNING") && connectorState.equals("RUNNING")){
            return true;
        }
        return false;
    }

    private void savingConfigurationInDBForOneClick(Response connectorResponse, JSONObject connectorJson, SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        sourceSinkConnectorConfig.setName(UUID.randomUUID().toString()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnector()+"-"+sourceSinkConnectorConfig.getWorkSpaceName());
        sourceSinkConnectorConfig.getSourceConfig().setDbPassword(encryptionService.encrypt(sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
        sourceSinkConnectorConfigRepository.persist(sourceSinkConnectorConfig);
    }

    private JSONObject createSourceConnectorJsonForOneClick(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        JSONObject response = null;
        if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.MYSQL.toString())) {
            if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
                response = createSourceMysqlJsonCDC(sourceSinkConnectorConfig);
            else if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
                response = createSourceMysqlJsonJdbc(sourceSinkConnectorConfig);
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
//            userConnectorConfigDTO.setHost("postgres");
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourcePostgresDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourceMongodbJsonDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
//
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourceCassandraJsonDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
//
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.DB2.toString())) {
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourceDB2JsonDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
//
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourceSQLServerJsonDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
//
//        } else if (userConnectorConfigDTO.getConnector().equals(ConnectorEnum.VITESS.toString())) {
//            if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.CDC.toString()))
//                response = createSourceVitessJsonDebezium(userConnectorConfigDTO);
//            else if(userConnectorConfigDTO.getConnectorFamily().equals(ConnectorEnum.JDBC.toString()))
//                response = createSourceJsonJdbc(userConnectorConfigDTO);
        }else if(sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.SFTP.toString())){
            if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.CSV.toString())) {
                response = createSourceSftpCSVForSingleClick(sourceSinkConnectorConfig);
            }else if(sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily().equals(ConnectorEnum.JSON.toString())){
                response = createSourceSftpJSONForSingleClick(sourceSinkConnectorConfig);
            }
        }
        return response;
    }

    private JSONObject createSourceSftpJSONForSingleClick(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_JSON_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            sourceSinkConnectorConfig.getSourceConfig().setTopicsPrefix(UUID.randomUUID().toString());
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSourceConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSourceConfig().getConnectorType()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("kafka.topic",sourceSinkConnectorConfig.getSourceConfig().getTopics());
            configObject.put("input.path",sourceSinkConnectorConfig.getSourceConfig().getInputPathForSftp());
            configObject.put("finished.path",sourceSinkConnectorConfig.getSourceConfig().getFinishedPathForSftp());
            configObject.put("error.path",sourceSinkConnectorConfig.getSourceConfig().getErrorPathForSftp());
            configObject.put("sftp.username", sourceSinkConnectorConfig.getSourceConfig().getDbUser());
            configObject.put("sftp.password", sourceSinkConnectorConfig.getSourceConfig().getDbPassword());
            configObject.put("sftp.host",sourceSinkConnectorConfig.getSourceConfig().getHost());
            configObject.put("sftp.port",sourceSinkConnectorConfig.getSourceConfig().getPort());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceSftpCSVForSingleClick(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        JSONObject response = null;
        try {
            sourceSinkConnectorConfig.getSourceConfig().setTopics(UUID.randomUUID().toString());
            String sb = helperMethodeToGeTJson(TemplatePath.SFTP_CSV_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            sourceSinkConnectorConfig.getSourceConfig().setTopicsPrefix(UUID.randomUUID().toString());
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSourceConfig().getConnectorType()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("kafka.topic",sourceSinkConnectorConfig.getSourceConfig().getTopics());
            configObject.put("input.path",sourceSinkConnectorConfig.getSourceConfig().getInputPathForSftp());
            configObject.put("finished.path",sourceSinkConnectorConfig.getSourceConfig().getFinishedPathForSftp());
            configObject.put("error.path",sourceSinkConnectorConfig.getSourceConfig().getErrorPathForSftp());
            configObject.put("sftp.username", sourceSinkConnectorConfig.getSourceConfig().getDbUser());
            configObject.put("sftp.password", sourceSinkConnectorConfig.getSourceConfig().getDbPassword());
            configObject.put("sftp.host",sourceSinkConnectorConfig.getSourceConfig().getHost());
            configObject.put("sftp.port",sourceSinkConnectorConfig.getSourceConfig().getPort());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceMysqlJsonJdbc(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        JSONObject response = null;
        try {
            sourceSinkConnectorConfig.getSourceConfig().setTopicsPrefix(UUID.randomUUID().toString());
            String sb = helperMethodeToGeTJson(TemplatePath.MYSQL_SOURCE_JDBC);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSourceConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSourceConfig().getConnectorType()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            if(sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                configObject.put("connection.url", createConnectionLinkMysqlJdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            }else if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                configObject.put("connection.url", createConnectionLinkPostgresJdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            } else if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.DB2.toString())) {
                configObject.put("connection.url", createConnectionLinkDB2Jdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            } else if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
                configObject.put("connection.url", createConnectionLinkCassandraJdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            } else if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.ORACLE.toString())) {
                configObject.put("connection.url", createConnectionLinkOracleJdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            }else if (sourceSinkConnectorConfig.getSourceConfig().getConnector().equals(ConnectorEnum.MONGODB.toString())) {
                configObject.put("connection.url", createConnectionLinkMongoDbJdbc(sourceSinkConnectorConfig.getSourceConfig().getHost(), sourceSinkConnectorConfig.getSourceConfig().getPort(), sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getDbUser(), sourceSinkConnectorConfig.getSourceConfig().getDbPassword()));
            }
            configObject.put("connection.user", sourceSinkConnectorConfig.getSourceConfig().getDbUser());
            configObject.put("connection.password", sourceSinkConnectorConfig.getSourceConfig().getDbPassword());
            configObject.put("table.whitelist", addTableList(sourceSinkConnectorConfig.getSourceConfig().getTables()));
            configObject.put("topic.prefix",sourceSinkConnectorConfig.getSourceConfig().getTopicsPrefix());
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private JSONObject createSourceMysqlJsonCDC(SourceSinkConnectorConfig sourceSinkConnectorConfig) {
        JSONObject response = null;
        try {
            String sb = helperMethodeToGeTJson(TemplatePath.MYSQL_SOURCE);
            String jsonString = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            jsonObject.put(NAME, UUID.randomUUID().toString() + "-" + sourceSinkConnectorConfig.getSourceConfig().getDbName() + "-" + sourceSinkConnectorConfig.getSourceConfig().getConnectorType()+"-"+sourceSinkConnectorConfig.getSourceConfig().getConnectorFamily());
            JSONObject configObject = (JSONObject) jsonObject.get(CONFIG);
            configObject.put("database.port", sourceSinkConnectorConfig.getSourceConfig().getPort());
            configObject.put("database.hostname", sourceSinkConnectorConfig.getSourceConfig().getHost());
            configObject.put("database.user", sourceSinkConnectorConfig.getSourceConfig().getDbUser());
            configObject.put("database.password", sourceSinkConnectorConfig.getSourceConfig().getDbPassword());
            configObject.put("database.include.list", sourceSinkConnectorConfig.getSourceConfig().getDbName());
            configObject.put("database.server.name",sourceSinkConnectorConfig.getSourceConfig().getDbServer());
            configObject.put("topic.prefix",sourceSinkConnectorConfig.getSourceConfig().getTopicsPrefix());
            configObject.put("transforms.addTopicPrefix.replacement",createReplacementTopicPrefix(sourceSinkConnectorConfig.getSourceConfig()));
            if (sourceSinkConnectorConfig.getSourceConfig().getTables() != null && !sourceSinkConnectorConfig.getSourceConfig().getTables().isEmpty()) {
                configObject.put("table.include.list", createTableListString(sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getTables()));
                if (sourceSinkConnectorConfig.getSourceConfig().getColumns() != null && !sourceSinkConnectorConfig.getSourceConfig().getColumns().isEmpty())
                    configObject.put("column.include.list", createColumnListString(sourceSinkConnectorConfig.getSourceConfig().getDbName(), sourceSinkConnectorConfig.getSourceConfig().getColumns()));
            } else {
                configObject.put("database.whitelist", sourceSinkConnectorConfig.getSourceConfig().getDbName());
            }
            response = jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String createReplacementTopicPrefix(SourceConfig sourceConfig) {
        StringBuilder prefixTipic = new StringBuilder();
        prefixTipic.append(sourceConfig.getTopicsPrefix()).append(".").append("$1");
        return prefixTipic.toString();
    }

    private void createDbServerName(SourceConfig sourceConfig) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sourceConfig.getDbName());
        stringBuilder.append(UUID.randomUUID().toString());
        sourceConfig.setDbServer(stringBuilder.toString());
    }

    private SourceConfig helperFunctionToFillDetailsFromDb(SourceConfig sourceConfig) {
        DbConnectionDetails byConnectionName = dbConnectionDetailsRepository.findByConnectionName(sourceConfig.getConnectionName());
        if(byConnectionName!=null){
            sourceConfig.setConnector(byConnectionName.getDb());
            sourceConfig.setDbName(byConnectionName.getDbName());
            sourceConfig.setDbUser(byConnectionName.getDbUser());
            sourceConfig.setDbPassword(encryptionService.decrypt(byConnectionName.getDbPassword()));
            sourceConfig.setHost(byConnectionName.getHost());
            sourceConfig.setPort(byConnectionName.getPort());
            sourceConfig.setMiniAppName(byConnectionName.getMiniAppName());
            sourceConfig.setWorkSpaceName(byConnectionName.getWorkSpaceName());
        }else{
            return null;
        }
        return sourceConfig;
    }
}
