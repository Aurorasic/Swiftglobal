package cn.primeledger.cas.global.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * for testing
 *
 * @author baizhengwen
 * @date 2018/3/16
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    public Object test() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
