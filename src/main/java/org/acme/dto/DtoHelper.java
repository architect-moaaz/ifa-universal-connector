package org.acme.dto;

import org.acme.model.DbConnectionDetails;
import org.acme.model.UserConnectorConfig;

public class DtoHelper {

    public static UserConnectorConfig convertFromUserConnectorConfigDto(UserConnectorConfigDto userConnectorConfigDto) {
        UserConnectorConfig userConnectorConfig = new UserConnectorConfig();
        if (userConnectorConfigDto.getConnector() != null && !userConnectorConfigDto.getConnector().isEmpty())
            userConnectorConfig.setConnector(userConnectorConfigDto.getConnector());
        if (userConnectorConfigDto.getDbUser() != null && !userConnectorConfigDto.getDbUser().isEmpty())
            userConnectorConfig.setDbUser(userConnectorConfigDto.getDbUser());
        if (userConnectorConfigDto.getUserId() != null && !userConnectorConfigDto.getUserId().isEmpty())
            userConnectorConfig.setUserId(userConnectorConfigDto.getUserId());
        if (userConnectorConfigDto.getId() != null)
            userConnectorConfig.setId(userConnectorConfigDto.getId());
        if (userConnectorConfigDto.getTopics() != null && !userConnectorConfigDto.getTopics().isEmpty())
            userConnectorConfig.setTopics(userConnectorConfigDto.getTopics());
        if (userConnectorConfigDto.getPort() != null && !userConnectorConfigDto.getPort().isEmpty())
            userConnectorConfig.setPort(userConnectorConfigDto.getPort());
        if (userConnectorConfigDto.getHost() != null && !userConnectorConfigDto.getHost().isEmpty())
            userConnectorConfig.setHost(userConnectorConfigDto.getHost());
        if (userConnectorConfigDto.getDbName() != null && !userConnectorConfigDto.getDbName().isEmpty())
            userConnectorConfig.setDbName(userConnectorConfigDto.getDbName());
        if (userConnectorConfigDto.getAppId() != null && !userConnectorConfigDto.getAppId().isEmpty())
            userConnectorConfig.setAppId(userConnectorConfigDto.getAppId());
        if (userConnectorConfigDto.getConnectorType() != null && !userConnectorConfigDto.getConnectorType().isEmpty())
            userConnectorConfig.setConnectorType(userConnectorConfigDto.getConnectorType());
        if (userConnectorConfigDto.getWorkSpaceId() != null && !userConnectorConfigDto.getWorkSpaceId().isEmpty())
            userConnectorConfig.setWorkSpaceId(userConnectorConfigDto.getWorkSpaceId());
        if (userConnectorConfigDto.getDbPassword() != null && !userConnectorConfigDto.getDbPassword().isEmpty())
            userConnectorConfig.setDbPassword(userConnectorConfigDto.getDbPassword());
        if (userConnectorConfigDto.getName() != null && !userConnectorConfigDto.getName().isEmpty())
            userConnectorConfig.setName(userConnectorConfigDto.getName());
        if (userConnectorConfigDto.getConfiguration() != null && !userConnectorConfigDto.getConfiguration().isEmpty())
            userConnectorConfig.setConfiguration(userConnectorConfigDto.getConfiguration());
        if (userConnectorConfigDto.getTables() != null && !userConnectorConfigDto.getTables().isEmpty())
            userConnectorConfig.setTables(userConnectorConfigDto.getTables());
        if (userConnectorConfigDto.getColumns() != null && !userConnectorConfigDto.getColumns().isEmpty())
            userConnectorConfig.setColumns(userConnectorConfigDto.getColumns());
        if (userConnectorConfigDto.getWorkSpaceName() != null && !userConnectorConfigDto.getWorkSpaceName().isEmpty())
            userConnectorConfig.setWorkSpaceName(userConnectorConfigDto.getWorkSpaceName());
        if (userConnectorConfigDto.getMiniAppName() != null && !userConnectorConfigDto.getMiniAppName().isEmpty())
            userConnectorConfig.setMiniAppName(userConnectorConfigDto.getMiniAppName());
        if(userConnectorConfigDto.getPrimaryKeys()!=null && !userConnectorConfigDto.getPrimaryKeys().isEmpty())
            userConnectorConfig.setPrimaryKeys(userConnectorConfigDto.getPrimaryKeys());
        if(userConnectorConfigDto.getConnectorFamily()!=null && !userConnectorConfigDto.getConnectorFamily().isEmpty())
            userConnectorConfig.setConnectorFamily(userConnectorConfigDto.getConnectorFamily());
        if(userConnectorConfigDto.getTopicsPrefix()!=null && !userConnectorConfigDto.getTopicsPrefix().isEmpty())
            userConnectorConfig.setTopicsPrefix(userConnectorConfigDto.getTopicsPrefix());
        return userConnectorConfig;
    }

    public static UserConnectorConfigDto convertToUserConnectorConfigDTO(UserConnectorConfig userConnectorConfig) {
        UserConnectorConfigDto userConnectorConfigDto = new UserConnectorConfigDto();
        if (userConnectorConfig != null && userConnectorConfig.getId() != null)
            userConnectorConfigDto.setId(userConnectorConfig.getId());
        if (userConnectorConfig.getConnector() != null && !userConnectorConfig.getConnector().isEmpty())
            userConnectorConfigDto.setConnector(userConnectorConfig.getConnector());
        if (userConnectorConfig.getDbUser() != null && !userConnectorConfig.getDbUser().isEmpty())
            userConnectorConfigDto.setDbUser(userConnectorConfig.getDbUser());
        if (userConnectorConfig.getUserId() != null && !userConnectorConfig.getUserId().isEmpty())
            userConnectorConfigDto.setUserId(userConnectorConfig.getUserId());
        if (userConnectorConfig.getTopics() != null && !userConnectorConfig.getTopics().isEmpty())
            userConnectorConfigDto.setTopics(userConnectorConfig.getTopics());
        if (userConnectorConfig.getPort() != null && !userConnectorConfig.getPort().isEmpty())
            userConnectorConfigDto.setPort(userConnectorConfig.getPort());
        if (userConnectorConfig.getHost() != null && !userConnectorConfig.getHost().isEmpty())
            userConnectorConfigDto.setHost(userConnectorConfig.getHost());
        if (userConnectorConfig.getDbName() != null && !userConnectorConfig.getDbName().isEmpty())
            userConnectorConfigDto.setDbName(userConnectorConfig.getDbName());
        if (userConnectorConfig.getAppId() != null && !userConnectorConfig.getAppId().isEmpty())
            userConnectorConfigDto.setAppId(userConnectorConfig.getAppId());
        if (userConnectorConfig.getConnectorType() != null && !userConnectorConfig.getConnectorType().isEmpty())
            userConnectorConfigDto.setConnectorType(userConnectorConfig.getConnectorType());
        if (userConnectorConfig.getWorkSpaceId() != null && !userConnectorConfig.getWorkSpaceId().isEmpty())
            userConnectorConfigDto.setWorkSpaceId(userConnectorConfig.getWorkSpaceId());
        if (userConnectorConfig.getDbPassword() != null && !userConnectorConfig.getDbPassword().isEmpty())
            userConnectorConfigDto.setDbPassword(userConnectorConfig.getDbPassword());
        if (userConnectorConfig.getName() != null && !userConnectorConfig.getName().isEmpty())
            userConnectorConfigDto.setName(userConnectorConfig.getName());
        if (userConnectorConfig.getConfiguration() != null && !userConnectorConfig.getConfiguration().isEmpty())
            userConnectorConfigDto.setConfiguration(userConnectorConfig.getConfiguration());
        if (userConnectorConfig.getTables() != null && !userConnectorConfig.getTables().isEmpty())
            userConnectorConfigDto.setTables(userConnectorConfig.getTables());
        if (userConnectorConfig.getColumns() != null && !userConnectorConfig.getColumns().isEmpty())
            userConnectorConfigDto.setColumns(userConnectorConfig.getColumns());
        if (userConnectorConfig.getWorkSpaceName() != null && !userConnectorConfig.getWorkSpaceName().isEmpty())
            userConnectorConfigDto.setWorkSpaceName(userConnectorConfig.getWorkSpaceName());
        if (userConnectorConfig.getMiniAppName() != null && !userConnectorConfig.getMiniAppName().isEmpty())
            userConnectorConfigDto.setMiniAppName(userConnectorConfig.getMiniAppName());
        if(userConnectorConfig.getPrimaryKeys()!=null && !userConnectorConfig.getPrimaryKeys().isEmpty())
            userConnectorConfigDto.setPrimaryKeys(userConnectorConfig.getPrimaryKeys());
        if(userConnectorConfig.getConnectorFamily()!=null && !userConnectorConfig.getConnectorFamily().isEmpty())
            userConnectorConfigDto.setConnectorFamily(userConnectorConfig.getConnectorFamily());
        if(userConnectorConfig.getTopicsPrefix()!=null && !userConnectorConfig.getTopicsPrefix().isEmpty())
            userConnectorConfigDto.setTopicsPrefix(userConnectorConfig.getTopicsPrefix());
        return userConnectorConfigDto;
    }

    public static DbConnectionDetails convertToDbConnectionDetails(UserConnectorConfigDto userConnectorConfigDto) {
        DbConnectionDetails dbConnectionDetails = new DbConnectionDetails();
        if (userConnectorConfigDto != null) {
            if (!userConnectorConfigDto.getConnector().isEmpty()) {
                dbConnectionDetails.setDb(userConnectorConfigDto.getConnector());
            }
            if (!userConnectorConfigDto.getDbUser().isEmpty())
                dbConnectionDetails.setDbUser(userConnectorConfigDto.getDbUser());
            if (!userConnectorConfigDto.getDbPassword().isEmpty())
                dbConnectionDetails.setDbPassword(userConnectorConfigDto.getDbPassword());
            if (!userConnectorConfigDto.getDbName().isEmpty()) {
                dbConnectionDetails.setDbName(userConnectorConfigDto.getDbName());
            }
            if (!userConnectorConfigDto.getHost().isEmpty())
                dbConnectionDetails.setHost(userConnectorConfigDto.getHost());
            if (!userConnectorConfigDto.getPort().isEmpty())
                dbConnectionDetails.setPort(userConnectorConfigDto.getPort());
            if (!userConnectorConfigDto.getWorkSpaceName().isEmpty())
                dbConnectionDetails.setWorkSpaceName(userConnectorConfigDto.getWorkSpaceName());
//            if (!userConnectorConfigDto.getMiniAppName().isEmpty()) {
//                dbConnectionDetails.setMiniAppName(userConnectorConfigDto.getMiniAppName());
//            }
            if (!userConnectorConfigDto.getConnectionName().isEmpty())
                dbConnectionDetails.setConnectionName(userConnectorConfigDto.getConnectionName());
            if (userConnectorConfigDto.getId() != null)
                dbConnectionDetails.setId(userConnectorConfigDto.getId());
        }
        return dbConnectionDetails;
    }

    public static UserConnectorConfigDto convertDbDetailsDtoToUserConnectorConfigDto(DbConnectionDetails connectionDb) {
        UserConnectorConfigDto userConnectorConfigDto = new UserConnectorConfigDto();
        if (connectionDb.getId() != null)
            userConnectorConfigDto.setId(connectionDb.getId());
        if (!connectionDb.getConnectionName().isEmpty())
            userConnectorConfigDto.setConnectionName(connectionDb.getConnectionName());
        if (!connectionDb.getDb().isEmpty())
            userConnectorConfigDto.setConnector(connectionDb.getDb());
        if (!connectionDb.getHost().isEmpty())
            userConnectorConfigDto.setHost(connectionDb.getHost());
        if (!connectionDb.getDbPassword().isEmpty())
            userConnectorConfigDto.setDbPassword(connectionDb.getDbPassword());
        if (connectionDb.getMiniAppName()!=null && !connectionDb.getMiniAppName().isEmpty())
            userConnectorConfigDto.setMiniAppName(connectionDb.getMiniAppName());
        if (!connectionDb.getWorkSpaceName().isEmpty())
            userConnectorConfigDto.setWorkSpaceName(connectionDb.getWorkSpaceName());
        if (!connectionDb.getDbUser().isEmpty())
            userConnectorConfigDto.setDbUser(connectionDb.getDbUser());
        if (!connectionDb.getPort().isEmpty())
            userConnectorConfigDto.setPort(connectionDb.getPort());
        if (!connectionDb.getDbName().isEmpty())
            userConnectorConfigDto.setDbName(connectionDb.getDbName());
        return userConnectorConfigDto;
    }

    public static DbConnectionDetails convertToDbConnectionDetailsUpdate(UserConnectorConfigDto userConnectorConfigDto) {
        DbConnectionDetails dbConnectionDetails = new DbConnectionDetails();
        dbConnectionDetails.setId(userConnectorConfigDto.getId());
        dbConnectionDetails.setConnectionName(userConnectorConfigDto.getConnectionName());
        dbConnectionDetails.setDbName(userConnectorConfigDto.getDbName());
        dbConnectionDetails.setDb(userConnectorConfigDto.getConnector());
        dbConnectionDetails.setDbUser(userConnectorConfigDto.getDbUser());
        dbConnectionDetails.setHost(userConnectorConfigDto.getHost());
        dbConnectionDetails.setPort(userConnectorConfigDto.getPort());
        dbConnectionDetails.setMiniAppName(userConnectorConfigDto.getMiniAppName());
        dbConnectionDetails.setWorkSpaceName(userConnectorConfigDto.getWorkSpaceName());
        dbConnectionDetails.setDbPassword(userConnectorConfigDto.getDbPassword());
        return dbConnectionDetails;
    }
}
