package io.dddspring.common.port.adapter.messaging.roketmq;

import com.google.gson.internal.$Gson$Types;
import io.dddspring.common.domain.model.DomainEvent;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class TestDomainEventHandle<TT extends DomainEvent> implements MessageListenerConcurrently {

    private int recivenum;
    public TestDomainEventHandle(){
        recivenum=0;
    }

    public int recivenum(){return recivenum;}
    public void resetRecivenum(){
        recivenum=0;
    }


    public void onEvent(TT enevt){}
    public void onEvent(String enevt){}
    public void onNotification(String rs){}


    static Type getSuperclassTypeParameter(Class<?> subclass)
    {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);


    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            int msgindex=0;
            for (MessageExt msg : msgs) {
                msgindex++;
                String rs = new String(msg.getBody(), "utf-8");
                onNotification(rs);

//              这个泛型使用是错误的报错
//                Type type = new TypeToken<Notification<TT>>() {
//                }.getType();
//
//                Notification<TT> notification = NotificationSerializer.instance().deserialize(rs, type);
//                onEvent(notification.event());

            }
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER; //稍后再试
        }
        recivenum = recivenum + msgs.size();


        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; //消费成功 标记消息处理成功
    }
}