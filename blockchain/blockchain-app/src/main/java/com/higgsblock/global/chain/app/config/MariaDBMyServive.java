package com.higgsblock.global.chain.app.config;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author HuangShengli
 * @date 2018-08-27
 **/
@Slf4j
public class MariaDBMyServive extends MariaDB4jSpringService {

    private String createSql;
    private String initSql;
    private String userName;
    private String password;
    private String dbName;
    private String url;

    public MariaDBMyServive(String createSql, String initSql, String userName, String password, String dbName, String url) {
        this.createSql = createSql;
        this.initSql = initSql;
        this.userName = userName;
        this.password = password;
        this.dbName = dbName;
        this.url = url;
    }

    @Override
    public void start() {
        LOGGER.info("mariaDB instance start");
        super.start();
        initDB();
    }

    private void initDB() {
        LOGGER.info("mariaDB init start");
        Connection conn = null;
        try {
            String sql = String.format("SELECT count(1) AS num FROM information_schema.columns WHERE table_schema = '%s'", dbName);
            conn = DriverManager.getConnection(url, userName, password);
            ResultSet resultSet = conn.prepareStatement(sql).executeQuery();
            resultSet.next();
            int count = resultSet.getInt("num");
            if (count <= 0) {
                db.source(createSql, userName, password, dbName);
                LOGGER.info("mariaDB create table successfully");
                db.source(initSql, userName, password, dbName);
                LOGGER.info("mariaDB init data successfully");
            }
        } catch (ManagedProcessException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException("mariadb init error");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
