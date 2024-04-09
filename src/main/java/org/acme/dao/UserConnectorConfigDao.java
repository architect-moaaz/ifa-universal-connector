//package org.acme.dao;
//
//import com.datastax.oss.driver.api.core.PagingIterable;
//import com.datastax.oss.driver.api.mapper.annotations.Dao;
//import com.datastax.oss.driver.api.mapper.annotations.Select;
//import com.datastax.oss.driver.api.mapper.annotations.Update;
//import org.acme.model.UserConnectorConfig;
//
//@Dao
//public interface UserConnectorConfigDao {
//
//    @Update
//    void update(UserConnectorConfig userConnectorConfig);
//
//    @Select
//    PagingIterable<UserConnectorConfig> findAll();
//
//    @Select
//    UserConnectorConfig findOne(String name);
//}
