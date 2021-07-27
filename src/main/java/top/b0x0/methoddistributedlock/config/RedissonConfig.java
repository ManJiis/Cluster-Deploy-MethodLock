package top.b0x0.methoddistributedlock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson config
 *
 * @author TANG
 * @since 2021-07-27
 * @since JDK1.8
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .setScanInterval(2000)
                .addNodeAddress(
                        "redis://192.168.1.106:9001"
                        , "redis://192.168.1.106:9002"
                        , "redis://192.168.1.106:9003"
                        , "redis://192.168.1.106:9004"
                        , "redis://192.168.1.106:9005"
                        , "redis://192.168.1.106:9006");

//        config = Config.fromYAML(new File("config-file.yaml"));

        return Redisson.create(config);
    }
}
