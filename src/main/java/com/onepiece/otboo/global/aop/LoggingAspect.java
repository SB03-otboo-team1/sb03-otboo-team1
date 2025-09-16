package com.onepiece.otboo.global.aop;

import java.util.Arrays;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;


/**
 * AOP 기반 계층별 로깅 Aspect
 * <p>
 * 컨트롤러/서비스/리포지토리 계층의 주요 메서드 실행 전후, 예외 발생 시 구조화 로그를 남긴다. requestId, 계층, 클래스, 메서드, 실행 시간, 예외 정보 등을
 * 포함한다.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {


    /**
     * 컨트롤러 계층의 모든 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* com.onepiece.otboo..controller..*(..))")
    public void controllerLayer() {
    }


    /**
     * 서비스 계층의 모든 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* com.onepiece.otboo..service..*(..))")
    public void serviceLayer() {
    }


    /**
     * 리포지토리 계층의 모든 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* com.onepiece.otboo..repository..*(..))")
    public void repositoryLayer() {
    }


    /**
     * 계층별 메서드 실행 시간 및 성능 경고
     */
    @Around("controllerLayer() || serviceLayer() || repositoryLayer()")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        String layer = resolveLayer(pjp);
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        String requestId = ensureRequestId();
        long startTime = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.debug("[{}] {}#{} 실행 시간: {}ms (requestId: {})", layer, className, methodName,
                executionTime, requestId);
            if (executionTime > 1000) {
                log.warn(
                    "[ExecutionTime] [{}] {}#{} 실행 시간이 {}ms로 느립니다. 성능 최적화가 필요합니다. (requestId: {})",
                    layer, className, methodName, executionTime, requestId);
            }
            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.error("[{}] {}#{} 실행 실패 - 실행 시간: {}ms (requestId: {})", layer, className,
                methodName, executionTime, requestId, throwable);
            throw throwable;
        }
    }

    /**
     * 메서드 실행 전에 로그를 기록한다. (매개변수)
     */
    @Before("controllerLayer() || serviceLayer() || repositoryLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String layer = resolveLayer(joinPoint);
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String requestId = ensureRequestId();
        log.debug("==> [{}] {}#{} 실행 시작 - requestId: {}, 매개변수: {}", layer, className, methodName,
            requestId, Arrays.toString(args));
    }

    /**
     * 메서드 정상 실행 후에 로그를 기록한다. (반환값)
     */
    @AfterReturning(pointcut = "controllerLayer() || serviceLayer() || repositoryLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String layer = resolveLayer(joinPoint);
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String requestId = ensureRequestId();
        log.debug("<== [{}] {}#{} 실행 완료 - requestId: {}, 반환값: {}", layer, className, methodName,
            requestId, result);
    }

    /**
     * 메서드 실행 중 예외 발생 시 로그를 기록한다.
     */
    @AfterThrowing(pointcut = "controllerLayer() || serviceLayer() || repositoryLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String layer = resolveLayer(joinPoint);
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String requestId = ensureRequestId();
        log.error("!!! [{}] {}#{} 실행 중 예외 발생 - requestId: {}, 예외: {}, 메시지: {}", layer, className,
            methodName, requestId, exception.getClass().getSimpleName(), exception.getMessage());
    }

    /**
     * 클래스명에 따라 계층(controller/service/repository) 문자열 반환
     * <p>
     * JoinPoint: 메서드 실행 정보만 제공, 실행 제어 불가. (@Before, @AfterReturning, @AfterThrowing)
     * ProceedingJoinPoint: JoinPoint 확장, proceed()로 실제 메서드 실행 제어 가능. (@Around)
     */
    private String resolveLayer(ProceedingJoinPoint pjp) {
        String typeName = pjp.getSignature().getDeclaringTypeName();
        if (typeName.contains(".controller.")) {
            return "controller";
        }
        if (typeName.contains(".service.")) {
            return "service";
        }
        if (typeName.contains(".repository.")) {
            return "repository";
        }
        return "unknown";
    }

    private String resolveLayer(JoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();
        if (typeName.contains(".controller.")) {
            return "controller";
        }
        if (typeName.contains(".service.")) {
            return "service";
        }
        if (typeName.contains(".repository.")) {
            return "repository";
        }
        return "unknown";
    }

    /**
     * MDC에 requestId가 없으면 새로 생성하여 저장, 반환
     */
    private String ensureRequestId() {
        String requestId = MDC.get("requestId");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);
        }
        return requestId;
    }
}

