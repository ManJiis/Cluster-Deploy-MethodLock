package top.b0x0.methoddistributedlock.annotion;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 多实例部署项目
 * 防止定时任务重复执行
 *
 * @author TANG
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodDistributedLock {

    /**
     * 要锁的资源,注意唯一性
     *
     * @return /
     */
    String resource() default "";

    /**
     * 锁过期时间 默认5分钟
     *
     * @return /
     */
    long expirationTime() default 5 * 60L;

    /**
     * 时间单位 默认秒
     *
     * @return /
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}