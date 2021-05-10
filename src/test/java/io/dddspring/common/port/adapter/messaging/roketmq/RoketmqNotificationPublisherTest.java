//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package io.dddspring.common.port.adapter.messaging.roketmq;

import io.dddspring.common.CommonTestCase;
import io.dddspring.common.event.TestableDomainEvent;
import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.domain.model.DomainEventPublisher;
import io.dddspring.common.event.EventStore;
import io.dddspring.common.notification.NotificationPublisher;
import io.dddspring.common.notification.PublishedNotificationTrackerStore;
import io.dddspring.common.persistence.PersistenceManagerProvider;
import io.dddspring.common.port.adapter.notification.RoketmpNotificationPublisher;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernateEventStore;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernatePublishedNotificationTrackerStore;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoketmqNotificationPublisherTest extends CommonTestCase {
    private int recivenum;
    private String topic="com-weailove-service-grpc-domain-demo";
    private String namesrvAddr="172.29.36.94:9876";//"192.168.43.125:9876"
    public RoketmqNotificationPublisherTest() {
        super();
    }

    @Test
    public void testPublishNotifications() throws Exception {
        EventStore eventStore = this.eventStore();

        assertNotNull(eventStore);
//通过new方式创建
        PublishedNotificationTrackerStore publishedNotificationTrackerStore =
                new HibernatePublishedNotificationTrackerStore(
                        new PersistenceManagerProvider(this.session()),
                        topic);

        NotificationPublisher notificationPublisher =
                new RoketmpNotificationPublisher(
                        eventStore,
                        publishedNotificationTrackerStore)
                        .setExchangeName(topic)
                        .setNameServer(namesrvAddr)
                        .setProducerGroup("demo_provider_user")
                ;

        assertNotNull(notificationPublisher);

        notificationPublisher.publishNotifications();


        /**
         * Consumer Group,非常重要的概念，后续会慢慢补充
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo01");
        //指定NameServer地址，多个地址以 ; 隔开
                consumer.setNamesrvAddr(namesrvAddr); //修改为自己的

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */

            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.subscribe(topic, "*");// 指定的主题//////
            consumer.registerMessageListener(
                    new MessageListenerConcurrently(){
                        @Override
                        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                            try {
                                int msgindex=0;
                                for (MessageExt msg : msgs) {
                                    msgindex++;
                                    String rs = new String(msg.getBody(), "utf-8");
                                    System.out.println("收到消息 MessageBody: :"+msgindex+"::" + rs);//输出消息内容
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER; //稍后再试
                            }
                            recivenum = recivenum + msgs.size();
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; //消费成功 标记消息处理成功
                        }
            });

            consumer.start();
            Thread.sleep(10000); // 等一些时间来发送消息

//            assertEquals(eventStore().countStoredEvents(), recivenum);


    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        DomainEventPublisher.instance().reset();

        super.setUp();

        // always start with at least 20 events

        EventStore eventStore = this.eventStore();

        long startingDomainEventId = (new Date()).getTime();
         // 初始化数据
        for (int idx = 0; idx < 5; ++idx) {
            long domainEventId = startingDomainEventId + 1;

            DomainEvent event = new TestableDomainEvent(domainEventId, "name" + domainEventId);

            eventStore.append(event);
        }
    }

    private EventStore eventStore() {
        EventStore eventStore = new HibernateEventStore(new PersistenceManagerProvider(this.session()));

        assertNotNull(eventStore);

        return eventStore;
    }

}




