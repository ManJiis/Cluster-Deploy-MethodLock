package top.b0x0.methodlock.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 模拟多实例,复制启动类
 * 然后VM options加上参数启动: -Dserver.port=8887
 *
 * @author ManJiis
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"top.b0x0"}) // 扫描Cluster-Deploy-MethodLock.jar包里的类
public class MethodLockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MethodLockDemoApplication.class, args);
    }

}
