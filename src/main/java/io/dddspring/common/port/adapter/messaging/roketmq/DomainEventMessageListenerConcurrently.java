package io.dddspring.common.port.adapter.messaging.roketmq;

import io.dddspring.common.notification.DomainNotificationEvent;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class DomainEventMessageListenerConcurrently extends DomainNotificationEvent implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                String rs = new String(msg.getBody(), "utf-8");
                super.onNoticee(rs);
            }
            if(successednum<msgs.size())
            {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER; //稍后再试
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER; //稍后再试
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; //消费成功 标记消息处理成功
    }
}