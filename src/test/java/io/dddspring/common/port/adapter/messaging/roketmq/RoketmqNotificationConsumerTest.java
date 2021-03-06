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

import com.google.gson.reflect.TypeToken;
import io.dddspring.common.CommonTestCase;
import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.event.DomainEventHandle;
import io.dddspring.common.event.EventStore;
import io.dddspring.common.event.TestableDomainEvent;
import io.dddspring.common.notification.Notification;
import io.dddspring.common.notification.NotificationPublisher;
import io.dddspring.common.notification.NotificationSerializer;
import io.dddspring.common.notification.PublishedNotificationTrackerStore;
import io.dddspring.common.persistence.PersistenceManagerProvider;
import io.dddspring.common.port.adapter.notification.RoketmpNotificationPublisher;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernateEventStore;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernatePublishedNotificationTrackerStore;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

import java.lang.reflect.Type;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Disabled
public class RoketmqNotificationConsumerTest extends CommonTestCase {

    private String topic="topic-demo";
    private String namesrvAddr="172.29.36.94:9876";//"192.168.43.125:9876"
//    private String namesrvAddr="192.168.3.17:9876";//"192.168.43.125:9876"
    public RoketmqNotificationConsumerTest() {
        super();
    }
    @Test
    public void TestableTestDomainEventHandle(){
        /**
         * Consumer Group,?????????????????????????????????????????????
         */
        long num=this.eventStore().countStoredEvents();

        /**
         * ??????Consumer???????????????????????????????????????????????????????????????????????????
         * ????????????????????????????????????????????????????????????????????????
         */
        try {


            TestDomainEventHandle eventHandle1= new TestDomainEventHandle<TestableDomainEvent>(){

                @Override
                public void onEvent(TestableDomainEvent event ){

                    System.out.println("??????t1::NotificationSerializer::"
                            +"id:"+ event.id()
                            +"name:"+ event.name()
                            +"eventVersion:"+ event.eventVersion()
                            +"occurredOn:" + event.occurredOn());
                    System.out.println(event.toString());
                    assertNotNull(event);
                }

                @Override
                public void onNotification(String rs){

                    Type type = new TypeToken<Notification<TestableDomainEvent>>() {
                    }.getType();

                    Notification<TestableDomainEvent> notification = NotificationSerializer.instance().deserialize(rs, type);

                    System.out.println("??????t1::NotificationSerializer::"
                            +"id:"+ notification.event().id()
                            +"name:"+ notification.event().name()
                            +"eventVersion:"+ notification.event().eventVersion()
                            +"occurredOn:" + notification.event().occurredOn());
                    System.out.println(notification.event().toString());
                    assertNotNull(notification.event());

                }

            };


            TestDomainEventHandle eventHandle2= new TestDomainEventHandle<TestableDomainEvent>(){

                @Override
                public void onEvent(TestableDomainEvent event ){
                    System.out.println("??????t2::NotificationSerializer::"
                            +"id:"+ event.id()
                            +"name:"+ event.name()
                            +"eventVersion:"+ event.eventVersion()
                            +"occurredOn:" + event.occurredOn());
                    System.out.println(event.toString());
                    assertNotNull(event);
                }

                @Override
                public void onNotification(String rs){

                    Type type = new TypeToken<Notification<TestableDomainEvent>>() {
                    }.getType();

                    Notification<TestableDomainEvent> notification = NotificationSerializer.instance().deserialize(rs, type);

                    System.out.println("??????t2::NotificationSerializer::"
                            +"id:"+ notification.event().id()
                            +"name:"+ notification.event().name()
                            +"eventVersion:"+ notification.event().eventVersion()
                            +"occurredOn:" + notification.event().occurredOn());
                    System.out.println(notification.event().toString());
                    assertNotNull(notification.event());

                }

            };


            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo");
            //??????NameServer???????????????????????? ; ??????
            consumer.setNamesrvAddr(namesrvAddr); //??????????????????
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.subscribe(topic, "*");// ???????????????//////
            consumer.registerMessageListener(eventHandle1);
            consumer.getConsumeThreadMax();
            System.out.println("consumer.getConsumeThreadMax()"+ consumer.getConsumeThreadMax());
            System.out.println("consumer.getConsumeThreadMin()"+ consumer.getConsumeThreadMin());

            DefaultMQPushConsumer consumer2 = new DefaultMQPushConsumer("consumer_demo2");
            //??????NameServer???????????????????????? ; ??????
            consumer2.setNamesrvAddr(namesrvAddr); //??????????????????
            consumer2.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer2.subscribe(topic, "*");// ???????????????//////
            consumer2.registerMessageListener(eventHandle2);
            consumer.start();
            consumer2.start();

            Thread.sleep(10000); // ??????????????????????????????


            System.out.println(String.format("10000ms ?????????%s?????????" ,eventHandle1.recivenum()));


//            assertEquals(num, eventHandle2.recivenum());
//            assertEquals(num, eventHandle1.recivenum());

            System.out.printf("Consumer Started.%n");

        }
         catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Test
    public void TestableDomainEventHandle() throws Exception{

        /**
         * Consumer Group,?????????????????????????????????????????????
         */
        long num=this.eventStore().countStoredEvents();
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo");
        //??????NameServer???????????????????????? ; ??????
        consumer.setNamesrvAddr(namesrvAddr); //??????????????????
        /**
         * ??????Consumer???????????????????????????????????????????????????????????????????????????
         * ????????????????????????????????????????????????????????????????????????
        */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, "*");// ???????????????//////
        DomainEventMessageListenerConcurrently t= new DomainEventMessageListenerConcurrently();

            DomainEventHandle eventHandle= new DomainEventHandle(){
                @Override
                public Type type() {
                    return new TypeToken<Notification<TestableDomainEvent>>() { }.getType();
                }
                @Override
                public void onEvent(Object eventobject) {
                    Notification<TestableDomainEvent> notification=(Notification<TestableDomainEvent>)eventobject;
                    System.out.println("NotificationSerializer::"
                            +"id:"+ notification.event().id()
                            +"name:"+ notification.event().name()
                            +"eventVersion:"+ notification.event().eventVersion()
                            +"occurredOn:" + notification.event().occurredOn());
                    assertNotNull(notification);
                }
            };

            t.registerEvent(TestableDomainEvent.class,eventHandle);
            consumer.registerMessageListener(t);
            consumer.start();

            Thread.sleep(10000); // ??????????????????????????????
            System.out.println(String.format("10000ms ?????????%s?????????" ,t.reciveCount()));
            assertEquals(num, t.reciveCount());
            System.out.printf("Consumer Started.%n");
    }


    @Test
    public void TestableTestJsonReaderDomainEventHandle(){
        /**
         * Consumer Group,?????????????????????????????????????????????
         */
        long num=this.eventStore().countStoredEvents();

        /**
         * ??????Consumer???????????????????????????????????????????????????????????????????????????
         * ????????????????????????????????????????????????????????????????????????
         */
        try {

            DomainEventMessageListenerConcurrently t1= new DomainEventMessageListenerConcurrently();

            DomainEventHandle ev=new DomainEventHandle() {
                @Override
                public Type type() {
                    return new TypeToken<Notification<TestableDomainEvent>>() { }.getType();
                }
                @Override
                public void onEvent(Object eventobject) {
                    Notification<TestableDomainEvent> event=(Notification<TestableDomainEvent>)eventobject;
                    System.out.println("NotificationSerializer::"
                            +"id:"+ event.event().id()
                            +"name:"+ event.event().name()
                            +"eventVersion:"+ event.event().eventVersion()
                            +"occurredOn:" + event.event().occurredOn());
                    System.out.println(event.toString());
                    assertNotNull(event);
                }
            };


            t1.registerEvent(TestableDomainEvent.class,ev);


            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_demo");
            //??????NameServer???????????????????????? ; ??????
            consumer.setNamesrvAddr(namesrvAddr); //??????????????????
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.subscribe(topic, "*");// ???????????????//////
            consumer.registerMessageListener(t1);
            consumer.start();

            Thread.sleep(10000); // ??????????????????????????????

            System.out.println(String.format("10000ms ?????????%s?????????" ,t1.reciveCount()));

            assertEquals(num, t1.reciveCount());

            System.out.printf("Consumer Started.%n");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // always start with at least 20 events

        EventStore eventStore = this.eventStore();

        long startingDomainEventId = (new Date()).getTime();
         // ???????????????
        for (int idx = 0; idx < 5; ++idx) {
            long domainEventId = startingDomainEventId + 1;

            DomainEvent event = new TestableDomainEvent(domainEventId, "name" + domainEventId);

            eventStore.append(event);
        }

        assertNotNull(eventStore);
//??????new????????????
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
                        .setProducerGroup("demo_provider_user_common");

        assertNotNull(notificationPublisher);

        notificationPublisher.publishNotifications();

    }

    private EventStore eventStore() {
        EventStore eventStore = new HibernateEventStore(new PersistenceManagerProvider(this.session()));

        assertNotNull(eventStore);

        return eventStore;
    }

}




