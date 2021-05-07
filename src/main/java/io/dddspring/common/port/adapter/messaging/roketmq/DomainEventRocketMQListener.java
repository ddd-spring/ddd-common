package io.dddspring.common.port.adapter.messaging.roketmq;


import io.dddspring.common.notification.DomainNotificationEvent;
import org.apache.rocketmq.spring.core.RocketMQListener;


public class DomainEventRocketMQListener extends DomainNotificationEvent implements RocketMQListener<String> {
    @Override
    public void onMessage(String rs) {
       super.onNoticee(rs);
    }
}