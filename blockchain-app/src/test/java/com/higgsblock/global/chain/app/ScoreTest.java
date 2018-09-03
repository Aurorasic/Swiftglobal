package com.higgsblock.global.chain.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yangyi
 * @deta 2018/5/9
 * @description
 */
@Slf4j
public class ScoreTest extends BaseTest {

    @Autowired
    private ConcurrentMap<String, Map> minerScoreMaps;

//    @Test
//    public void test() {
//        Map map = minerScoreMaps.get(ScoreManager.ALL_SCORE_KEY);
//        LOGGER.info("map is {}", map);
//    }

}
