package com.onepiece.otboo.global.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    @Value("${spring.application.name:discodeit}")
    private String appName;

    // 각 인스턴스 고유 식별자
    @Value("${INSTANCE_ID:${HOSTNAME:${random.uuid}}}")
    private String instanceId;

    /**
     * Processing(단일 처리) 컨테이너 팩토리 - 공유 그룹
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> processingKafkaListenerContainerFactory(
        DefaultErrorHandler errorHandler) {

        String groupId = appName + ".processing.notifications";
        return buildListenerFactory(groupId, errorHandler);
    }

    /**
     * Broadcast(모든 인스턴스) 컨테이너 팩토리 - 인스턴스별 고유 그룹
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> broadcastKafkaListenerContainerFactory(
        DefaultErrorHandler errorHandler) {

        String groupId = appName + ".broadcast." + instanceId;
        return buildListenerFactory(groupId, errorHandler);
    }

    /**
     * 그룹 ID를 포함한 Consumer 프로퍼티 생성
     */
    private Map<String, Object> consumerProps(String groupId) {
        Map<String, Object> p = baseProps();
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return p;
    }

    /**
     * 공통 Consumer 기본 프로퍼티
     */
    private Map<String, Object> baseProps() {
        Map<String, Object> p = new HashMap<>();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        p.put(ConsumerConfig.CLIENT_ID_CONFIG, appName + "." + instanceId);

        // 역직렬화 오류를 안전하게 처리하기 위한 래퍼 사용
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        p.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        p.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
        return p;
    }

    /**
     * ConsumerFactory 생성
     */
    private DefaultKafkaConsumerFactory<String, String> buildConsumerFactory(String groupId) {
        Map<String, Object> props = consumerProps(groupId);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * ListenerContainerFactory 생성
     */
    private ConcurrentKafkaListenerContainerFactory<String, String> buildListenerFactory(
        String groupId, DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(buildConsumerFactory(groupId));
        f.setCommonErrorHandler(errorHandler);

        // 수동 ACK 모드 설정(리스너에서 Acknowledgment 객체로 수동 커밋)
        f.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.MANUAL);

        return f;
    }

    /**
     * 에러 핸들러 + DLT 1초 간격으로 3회 재시도 후 &lt;원본토픽&gt;.DLT 로 전송. (주의) DLT 토픽은 미리 생성되어 있어야 합니다.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = null;

        if (kafkaTemplate != null) {
            recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                // 원본 토픽과 동일 파티션의 "<topic>.DLT" 로 라우팅
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
            );
        }

        return (recoverer != null)
            ? new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L))
            : new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }
}
