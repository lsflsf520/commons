package com.ujigu.shardjdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;

public class ShardJdbcTest {

	public static void main(String[] args) throws SQLException {
		Map<String, DataSource> dataSourceMap = new HashMap<>();
	    
		org.apache.tomcat.jdbc.pool.DataSource dataSource1 = new org.apache.tomcat.jdbc.pool.DataSource();
	    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
	    dataSource1.setUrl("jdbc:mysql://localhost:3306/demo_ds_0");
	    dataSource1.setUsername("root");
	    dataSource1.setPassword("root");
	    dataSourceMap.put("ds_0", dataSource1);
	    
	    org.apache.tomcat.jdbc.pool.DataSource dataSource2 = new org.apache.tomcat.jdbc.pool.DataSource();
	    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
	    dataSource2.setUrl("jdbc:mysql://localhost:3306/demo_ds_1");
	    dataSource2.setUsername("root");
	    dataSource2.setPassword("root");
	    dataSourceMap.put("ds_1", dataSource2);
	    
	    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
	    orderTableRuleConfig.setLogicTable("t_order");
	    orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
	    
	    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
	    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
	    
	    TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
	    orderItemTableRuleConfig.setLogicTable("t_order_item");
	    orderItemTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_item_${[0, 1]}");
	    
	    orderItemTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
	    orderItemTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_item_${order_id % 2}"));
	    
	    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
	    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
	    shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
	    
	    // config order_item table rule...
	    
	    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new HashMap<String, Object>(), new Properties());

	    Connection conn = dataSource.getConnection();
	    PreparedStatement preparedStatement = conn.prepareStatement("insert into t_order(order_id, user_id, price, status, create_time, last_uptime) values(?, ?, ?, 'NORMAL', now(), now())");
	    preparedStatement.setInt(1, 307);
        preparedStatement.setInt(2, 224);
        preparedStatement.setInt(3, 3000);
	    
        preparedStatement.execute();
	    
	    /*String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, 10);
        preparedStatement.setInt(2, 1001);
        try (ResultSet rs = preparedStatement.executeQuery()) {
            while(rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
            }
        }*/
	}

}
