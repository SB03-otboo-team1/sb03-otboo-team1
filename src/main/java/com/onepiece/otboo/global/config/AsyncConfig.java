package com.onepiece.otboo.global.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig implements AsyncConfigurer {

    @Value("${async.executors.default.core-size}")
    private int defaultCoreSize;
    @Value("${async.executors.default.max-size}")
    private int defaultMaxSize;
    @Value("${async.executors.default.queue-capacity}")
    private int defaultQueueCapacity;
    @Value("${async.executors.default.keep-alive}")
    private int defaultKeepAlive;

    private static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 20;
    private static final boolean DEFAULT_WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = true;

    private static final String MAIL_THREAD_PREFIX = "mail-exec";

    @Bean(name = "mailTaskExecutor")
    public ThreadPoolTaskExecutor mailTaskExecutor(
        @Value("${async.executors.mail-content.core-size}") int core,
        @Value("${async.executors.mail-content.max-size}") int max,
        @Value("${async.executors.mail-content.queue-capacity}") int queue,
        @Value("${async.executors.mail-content.keep-alive}") int keepAlive
    ) {
        return buildExecutor(core, max, queue, keepAlive, MAIL_THREAD_PREFIX);
    }

    /**
     * ThreadPoolTaskExecutor 공통 빌더
     *
     * @param core      항상 유지되는 최소 스레드 수
     * @param max       최대 스레드 수
     * @param queue     작업을 대기시킬 큐의 크기 -> corePoolSize 만큼 스레드가 모두 사용 중일 때 새로운 작업들이 대기
     * @param keepAlive 유휴 스레드의 생존 시간
     * @param prefix    생성되는 스레드의 이름 접두사
     * @return ThreadPoolTaskExecutor 객체
     */
    private ThreadPoolTaskExecutor buildExecutor(int core, int max, int queue, int keepAlive,
        String prefix) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setKeepAliveSeconds(keepAlive);
        executor.setThreadNamePrefix(prefix + "-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(
            DEFAULT_WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN);
        executor.setAwaitTerminationSeconds(DEFAULT_AWAIT_TERMINATION_SECONDS);
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();

        return executor;
    }

    /**
     * Spring @Async 비동기 메서드에서 uncaught(try-catch로 처리되지 않은) 예외가 발생할 경우 자동으로 호출되는 공통 예외 핸들러입니다.
     *
     * <p>비동기 작업에서 예외가 발생해도 호출자에게 예외가 전달되지 않으므로,
     * 이 핸들러에서 예외 정보를 로깅하여 추적할 수 있도록 합니다.</p>
     *
     * @return AsyncUncaughtExceptionHandler 예외 로깅 핸들러
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> log.error(
            "비동기 작업 처리 중 예외가 발생했습니다. method={}, params={}",
            method.getName(),
            params,
            throwable
        );
    }

    /**
     * 비동기 실행 시 MDC 및 SecurityContext를 스레드풀 작업에 안전하게 전달/복원하는 TaskDecorator 구현체입니다.
     * <p>
     * Spring의 ThreadPoolTaskExecutor에서 setTaskDecorator로 등록하여 사용하며, 각 비동기 작업 실행 전후로 로깅 컨텍스트(MDC)와
     * 인증 컨텍스트(SecurityContext)를 복사/복원합니다.
     * </p>
     */
    static class ContextCopyingDecorator implements TaskDecorator {

        /**
         * 현재 스레드의 MDC와 SecurityContext를 캡처하여, 비동기 실행 스레드에 동일하게 적용합니다.
         *
         * @param runnable 원본 비동기 작업
         * @return 컨텍스트가 복원된 Runnable
         */
        @Override
        public @NonNull Runnable decorate(@NonNull Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            SecurityContext context = SecurityContextHolder.getContext();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    SecurityContextHolder.setContext(context);
                    runnable.run();
                } finally {
                    MDC.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        }
    }
}
