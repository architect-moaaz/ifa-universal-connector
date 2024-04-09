package org.acme.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.acme.dto.DtoHelper;
import org.acme.dto.UserConnectorConfigDto;
import org.acme.model.DbConnectionDetails;
import org.acme.repository.DbConnectionDetailsRepository;
import org.acme.resource.utili.ConnectionClass;
import org.acme.resource.utili.ConnectorEnum;
import org.acme.resource.utili.ResponseUtil;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DbConnectionDetailsService {

    private String POSTGRES_URL = "jdbc:postgresql://";
    private String MYSQL_URL = "jdbc:mysql://";
    private String DB2_URL = "jdbc:db2:";
    private String SQL_SERVER_URL = "jdbc:sqlserver://";
    private String ORACLE = "jdbc:oracle:thin:@";
    private String MONGO_URL = "rs0";
    private String URL = "http://";
    private String NAME = "name";
    private String CONFIG = "config";
    private String DOUBLE_ORDINAL = "7";
    private String MYSQL_DRIVER_CLASS = "new com.mysql.jdbc.Driver()";

    Logger logger = LoggerFactory.getLogger("universal-connector");

    @Inject
    private DbConnectionDetailsRepository dbConnectionDetailsRepository;

    @Inject
    private EncryptionService encryptionService;

    public ResponseUtil testDbConnection(UserConnectorConfigDto userConnectorConfigDto) {
        ResponseUtil response = new ResponseUtil();
        try {
            if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                Connection mysqlConnectionUrl = ConnectionClass.createMysqlConnectionUrl(userConnectorConfigDto);
                if (mysqlConnectionUrl == null) {
                    return failedResponse();
                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                Connection postgresConnectionUrl = ConnectionClass.createPostgresConnectionUrl(userConnectorConfigDto);
                if (postgresConnectionUrl == null) {
                    return failedResponse();
                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MONGODB.toString())) {
//                Connection mongodbConnectionUrl = ConnectionClass.createMONGODbConnectionUrl(userConnectorConfigDto);
//                if (mongodbConnectionUrl == null) {
//                    return failedResponse();
//                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.ORACLE.toString())) {
                Connection oracleConnectionUrl = ConnectionClass.createOracleConnectionUrl(userConnectorConfigDto);
                if (oracleConnectionUrl == null) {
                    return failedResponse();
                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.SQLSERVER.toString())) {
                Connection sqlServerConnectionUrl = ConnectionClass.createSqlServerConnectionUrl(userConnectorConfigDto);
                if (sqlServerConnectionUrl == null) {
                    return failedResponse();
                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.CASSANDRA.toString())) {
                Connection cassandraConnectionUrl = ConnectionClass.createCassandraConnectionUrl(userConnectorConfigDto);
                if (cassandraConnectionUrl == null) {
                    return failedResponse();
                }
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.DB2.toString())) {
                Connection db2ConnectionUrl = ConnectionClass.createDB2ConnectionUrl(userConnectorConfigDto);
                if (db2ConnectionUrl == null) {
                    return failedResponse();
                }
            }else if(userConnectorConfigDto.getConnector().equals(ConnectorEnum.SFTP.toString())){
                ChannelSftp channelSftp = ConnectionClass.createSftpConnection(userConnectorConfigDto);
                if (!channelSftp.isConnected()) {
                    return failedResponse();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("connection failed...due to below error \n: " + e.getMessage());
            response.setStatusCode(RestResponse.StatusCode.BAD_REQUEST);
            return response;
        }
        response.setStatusCode(RestResponse.StatusCode.OK);
        response.setMessage("Connected to db...");
        return response;
    }

    private ResponseUtil failedResponse() {
        ResponseUtil responseUtil = new ResponseUtil();
        responseUtil.setStatusCode(RestResponse.StatusCode.BAD_REQUEST);
        responseUtil.setMessage("Connection failed....");
        return responseUtil;
    }

    public ResponseUtil addDbDetailsIntoDb(UserConnectorConfigDto userConnectorConfigDto) {
        ResponseUtil responseUtil = new ResponseUtil();
//        if(12>2){
//            responseUtil.setMessage("connection failed...due to below error \n: ");
//            responseUtil.setStatusCode((RestResponse.StatusCode.BAD_REQUEST));
//            return responseUtil;
//        }
        try {
            if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                Connection mysqlConnectionUrl = ConnectionClass.createMysqlConnectionUrl(userConnectorConfigDto);
                if (mysqlConnectionUrl == null) {
                    return failedResponse();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseUtil.setMessage("connection failed...due to below error \n: " + e.getMessage());
            responseUtil.setStatusCode((RestResponse.StatusCode.BAD_REQUEST));
            return responseUtil;
        }
        DbConnectionDetails byConnectionName = dbConnectionDetailsRepository.findByConnectionName(userConnectorConfigDto.getConnectionName());
        if (byConnectionName != null && byConnectionName.getConnectionName().equals(userConnectorConfigDto.getConnectionName())) {
            responseUtil.setStatusCode(RestResponse.StatusCode.CONFLICT);
            responseUtil.setMessage("connection details with given name is already present in db..Please give different name");
            return responseUtil;
        }
//        userConnectorConfigDto.setConnectionName(userConnectorConfigDto.getUserId()+userConnectorConfigDto.getDbUser()+ UUID.randomUUID().toString());
//        userConnectorConfigDto.setConnectionName(userConnectorConfigDto.getDbUser()+"-"+ UUID.randomUUID().toString()+"-"+userConnectorConfigDto.getConnector());
        userConnectorConfigDto.setDbPassword(encryptionService.encrypt(userConnectorConfigDto.getDbPassword()));
        dbConnectionDetailsRepository.persistOrUpdate(DtoHelper.convertToDbConnectionDetails(userConnectorConfigDto));
        responseUtil.setStatusCode(RestResponse.StatusCode.OK);
        responseUtil.setMessage("added to db...");
        return responseUtil;
    }

    public ResponseUtil getConnectionByName(String connectionName) {
        ResponseUtil responseUtil = new ResponseUtil();
        DbConnectionDetails connectionDb = dbConnectionDetailsRepository.findByConnectionName(connectionName);
        connectionDb.setDbPassword(encryptionService.decrypt(connectionDb.getDbPassword()));
        if (connectionDb != null) {
            responseUtil.setData(DtoHelper.convertDbDetailsDtoToUserConnectorConfigDto(connectionDb));
            responseUtil.setStatusCode(Response.Status.OK.getStatusCode());
            return responseUtil;
        }
        responseUtil.setMessage("no connection details for the connectionName = " + connectionName);
        responseUtil.setStatusCode(RestResponse.StatusCode.NO_CONTENT);
        return responseUtil;
    }

    public ResponseUtil deleteConnection(String connectionName) {
        ResponseUtil responseUtil = new ResponseUtil();
        DbConnectionDetails dbConnectionDetailsDb = dbConnectionDetailsRepository.findByConnectionName(connectionName);
        try {
            if (dbConnectionDetailsDb == null) {
                responseUtil.setStatusCode(RestResponse.StatusCode.NO_CONTENT);
                responseUtil.setMessage("no such connection is available in database with name = " + connectionName);
                return responseUtil;
            }
            long res = dbConnectionDetailsRepository.deleteByConnectionName(connectionName);
            if (res==1) {
                responseUtil.setMessage("Connection Details removed...");
                responseUtil.setStatusCode(RestResponse.StatusCode.OK);
                return responseUtil;
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseUtil.setStatusCode(RestResponse.StatusCode.BAD_REQUEST);
            responseUtil.setMessage("delete failed because of this error : " + e.getMessage());
            return responseUtil;
        }
        responseUtil.setMessage("delete failed....");
        responseUtil.setStatusCode(RestResponse.StatusCode.BAD_REQUEST);
        return responseUtil;
    }

    public ResponseUtil updateConnectionDetails(UserConnectorConfigDto userConnectorConfigDto) {
        ResponseUtil responseUtil = new ResponseUtil();
        DbConnectionDetails dbConnectionDetails = dbConnectionDetailsRepository.findById(userConnectorConfigDto.getId());

        if (dbConnectionDetailsRepository.findByConnectionName(userConnectorConfigDto.getConnectionName()) != null) {
            responseUtil.setMessage("Connection Name "+userConnectorConfigDto.getConnectionName()+" already present. " );
            responseUtil.setStatusCode(RestResponse.StatusCode.CONFLICT);
            return responseUtil;
        }
        userConnectorConfigDto.setId(dbConnectionDetails.getId());
        dbConnectionDetails = DtoHelper.convertToDbConnectionDetailsUpdate(userConnectorConfigDto);
        try {
            if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.MYSQL.toString())) {
                ConnectionClass.createMysqlConnectionUrl(userConnectorConfigDto);
            } else if (userConnectorConfigDto.getConnector().equals(ConnectorEnum.POSTGRES.toString())) {
                ConnectionClass.createPostgresConnectionUrl(userConnectorConfigDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseUtil.setStatusCode(RestResponse.StatusCode.UNAUTHORIZED);
            responseUtil.setMessage("invalid details to connect to db...");
            return responseUtil;
        }
        dbConnectionDetails.setDbPassword(encryptionService.encrypt(dbConnectionDetails.getDbPassword()));
        dbConnectionDetailsRepository.update(dbConnectionDetails);
        responseUtil.setStatusCode(RestResponse.StatusCode.OK);
        responseUtil.setMessage("Updated connection Details...");
        responseUtil.setData(dbConnectionDetails);
        return responseUtil;
    }

    public List<UserConnectorConfigDto> getConnectionByWorkSpaceName(String workspacename) {
        List<UserConnectorConfigDto> response  = new ArrayList<>();
        List<DbConnectionDetails> response1 = dbConnectionDetailsRepository.findByWorkSpaceName(workspacename);
        if(response1!=null && !response1.isEmpty()){
            for(DbConnectionDetails dbConnectionDetails:response1){
                dbConnectionDetails.setDbPassword(encryptionService.decrypt(dbConnectionDetails.getDbPassword()));
                response.add(DtoHelper.convertDbDetailsDtoToUserConnectorConfigDto(dbConnectionDetails));
            }
            return response;
        }
        return response;
    }
}
