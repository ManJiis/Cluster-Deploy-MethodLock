package top.b0x0.methoddistributedlock.aspect;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.system.SystemUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.b0x0.methoddistributedlock.annotion.MethodDistributedLock;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * 防止定时任务重复执行切面
 *
 * @author TANG
 * @date 2021-04-27
 */
@Component
@Aspect
public class MethodLockAspect {
    private static final Logger log = LoggerFactory.getLogger(MethodLockAspect.class);

    @Pointcut("@annotation(top.b0x0.methoddistributedlock.annotion.MethodDistributedLock)")
    public void methodLockPointCut() {
    }

    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory("lock-pool-", false);
        EXECUTOR_SERVICE = new ThreadPoolExecutor(
                3,
                20,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                namedThreadFactory);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("methodLockPointCut()")
    public Object doAroundMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        MethodDistributedLock taskLock = method.getDeclaredAnnotation(MethodDistributedLock.class);

        if (taskLock == null) {
            return proceedingJoinPoint.proceed();
        }

        String resource = taskLock.resource();

        long expirationTime = taskLock.expirationTime();
        TimeUnit expirationTimeUnit = taskLock.timeUnit();

        String lockKey = "";
        if (StringUtils.hasText(resource)) {
            lockKey = "method_distributedLock:" + resource;
        } else {
            lockKey = "method_distributedLock:" + method.getDeclaringClass().getName() + ":" + method.getName();
        }

        // setIfAbsent 在redis cluster模式下master宕机 数据还没同步到slave 会重复获取锁
//        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "lock", expirationTime, expirationTimeUnit);

        // 使用redisson
        RLock redissonLock = redissonClient.getLock(lockKey);

        try {
            // 设置锁的有效时间
            redissonLock.lock(expirationTime, expirationTimeUnit);
            // 检查当前线程是否获得此锁
            boolean locked = redissonLock.isHeldByCurrentThread();
            log.info("-------------------------------------------------------------------------------");
            log.info("实例IP: [{}] 获取锁结果: [{}] ,lockKey: [{}] , 设置超时时间为：{} 秒", getServerIp(), locked, lockKey, expirationTime);
            // 获得锁
            if (Boolean.TRUE.equals(locked)) {
                Future<Object> future = EXECUTOR_SERVICE.submit(() -> {
                    // 执行原方法
                    Object obj = null;
                    try {
                        // 执行原方法
                        obj = proceedingJoinPoint.proceed();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                    return obj;
                });
                // 超时处理
                return future.get(expirationTime, expirationTimeUnit);
            } else {
                log.info("任务已经被其它实例执行，本实例跳过执行：{}", lockKey);
                return null;
            }
        } catch (TimeoutException ex) {
            log.error("{} 运行超时.......", lockKey, ex);
            return null;
        } catch (InterruptedException | ExecutionException e) {
            log.error("获取锁异常: {}", e.getMessage());
            return null;
        } finally {

            // 释放锁
//            stringRedisTemplate.delete(lockKey);

            redissonLock.unlock();

            log.info("释放锁: {}", lockKey);
        }
    }


    /**
     * 获取服务器IP地址
     *
     * @return /
     */
    private static String getServerIp() {
        return SystemUtil.getHostInfo().getAddress();
    }
}
