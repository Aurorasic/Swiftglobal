package com.higgsblock.global.chain.app.task;

import com.higgsblock.global.chain.app.config.MariaDBMyServive;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HuangShengli
 * @date 2018-08-31
 **/
@Component
@Slf4j
public class DBMonitorTask extends BaseTask {

    @Autowired
    private MariaDBMyServive mariaDBMyServive;

    private AtomicInteger times = new AtomicInteger(0);

    private final int MAX_RETRAY = 5;

    private final String DETECT_SQL = "SELECT 1 FROM DUAL";


    /**
     * Task.
     */
    @Override
    protected void task() {
        if (mariaDBMyServive.getStatus() != MariaDBMyServive.DBStatus.READY) {
            LOGGER.info("db is starting,retry later");
            return;
        }
        if (times.get() >= MAX_RETRAY) {
            LOGGER.error("db unrecoverable,retry times has exceed MAX_RETRY");
            return;
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(mariaDBMyServive.getUrl(), mariaDBMyServive.getUserName(), mariaDBMyServive.getPassword());
            ResultSet resultSet = conn.prepareStatement(DETECT_SQL).executeQuery();
            resultSet.next();
            LOGGER.info("db monitor task successfully:{}", resultSet.getString(1));
            //clear retry times
            times.set(0);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            dealException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private void dealException(SQLException e) {
        if (!(e instanceof CommunicationsException)) {
            return;
        }
        LOGGER.info("db communication link failure,retry");
        times.incrementAndGet();
        mariaDBMyServive.start();
    }

    /**
     * Gets period ms.
     *
     * @return the period ms
     */
    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(60 * 2);
    }
}
