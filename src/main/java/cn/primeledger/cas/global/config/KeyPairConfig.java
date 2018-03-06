package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.crypto.model.KeyPair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Su Jiulong
 * @date 2018/3/2
 */
@Configuration
public class KeyPairConfig {

    @Bean
    public KeyPair peerKeyPair(AppConfig config) {
        KeyPair keyPair = new KeyPair();
        keyPair.setPriKey(config.getPriKey());
        keyPair.setPubKey(config.getPubKey());
        return keyPair;
    }
}
