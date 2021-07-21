package top.b0x0.methoddistributedlock.task;

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
public class MyTask {
    private static final Logger log = LoggerFactory.getLogger(MyTask.class);

    @Scheduled(cron = "0/1 * * * * ?")
    @MethodDistributedLock()
    public void testTask() throws InterruptedException {
        Thread.sleep(30000);
        log.info("System.currentTimeMillis() = " + System.currentTimeMillis());
    }
}
