package cloud.tengyee.concurrent.lock.aspects;

import cloud.tengyee.concurrent.lock.LockGroup;
import cloud.tengyee.concurrent.lock.annotations.TyLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

/**
 * @author Mr.赵
 * created on 2020/12/9
 */
@Aspect //该注解标示该类为切面类
public class TyLockAspect {

    private final ConcurrentMap<String, LockGroup> lockGroupMap = new ConcurrentHashMap<String, LockGroup>(16);

    private LockGroup getGroup(String groupName){
        lockGroupMap.putIfAbsent(groupName,new LockGroup());
        return lockGroupMap.get(groupName);
    }

    @Around("@annotation(lock)")
    public Object aroundLock(ProceedingJoinPoint point, TyLock lock) throws Throwable {
        String key=lock.key();
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression =
                parser.parseExpression(key);
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args=point.getArgs();
        for(int i=0;i<args.length;i++){
            context.setVariable("arg"+i,args[i]);
        }
        String realKey=expression.getValue(context).toString();

        LockGroup group=getGroup(point.getTarget().getClass().getTypeName());
        Lock rlock=group.getLock(realKey);
        rlock.lock();
        try {
            return point.proceed();
        } finally {
            rlock.unlock();
            group.destoryLock(realKey);
        }
    }
}
