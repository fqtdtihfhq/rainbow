package cloud.tengyee.concurrent.lock.annotations;

import java.lang.annotation.*;

/**
 * 锁标记
 * @author Mr.赵
 * created on 2020/12/9
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TyLock {
    String key() default "";//锁分组名称
}
