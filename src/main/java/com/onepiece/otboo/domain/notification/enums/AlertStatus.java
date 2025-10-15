package com.onepiece.otboo.domain.notification.enums;

/**
 * {@code AlertStatus}는 특이 기상 변화(폭우, 급강하 등)가 감지되었을 때 해당 알림의 발송 상태를 나타냅니다.
 *
 * <p>이 Enum은 날씨 데이터 수집 배치 이후, {@code WeatherAlertOutbox} 엔티티에서
 * 각 알림이 현재 어떤 상태에 있는지를 구분하기 위해 사용됩니다.</p>
 *
 * <ul>
 *   <li>{@link #PENDING} - 알림이 생성되었지만 아직 발송되지 않은 상태</li>
 *   <li>{@link #SENDING} - 알림이 사용자에게 발송 중인 상태</li>
 *   <li>{@link #SEND} - 알림이 정상적으로 사용자에게 발송된 상태</li>
 *   <li>{@link #FAILED} - 알림 발송 중 오류가 발생한 상태</li>
 * </ul>
 *
 * <p>이 상태값은 배치(Tasklet) 또는 메시지 브로커 기반 발송 로직에서
 * 재시도 및 모니터링 로직의 기준으로 사용됩니다.</p>
 */
public enum AlertStatus {
    PENDING,
    SENDING,
    SEND,
    FAILED
}
