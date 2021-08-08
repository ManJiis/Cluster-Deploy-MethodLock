package top.b0x0.methodlock.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.b0x0.methoddistributedlock.annotion.MethodDistributedLock;


/**
 * @author TANG
 * @since 2021-07-21
 * @since jdk1.8
 */
@Component
public class MyDemoTask {
    private static final Logger log = LoggerFactory.getLogger(MyDemoTask.class);

    @Scheduled(cron = "0/2 * * * * ?")
    @MethodDistributedLock()
    public void testTask() throws InterruptedException {
        int randomSec = ((int) (Math.random() * 9000 + 1000));
        long sleepSec = randomSec * 10L;
        System.out.println("sleep sec = " + randomSec);
        Thread.sleep(sleepSec);
        log.info("System.currentTimeMillis() = " + System.currentTimeMillis());
    }

    public static void main(String[] args) {
//        Math.random()取值范围是[0,1)
//        Math.random()*9000的取值范围是[0,9000);
//        Math.random()*9000+1000的取值范围是[1000,10000)。

    }
}
