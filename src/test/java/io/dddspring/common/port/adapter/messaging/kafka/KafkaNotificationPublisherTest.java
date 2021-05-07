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

package io.dddspring.common.port.adapter.messaging.kafka;

import com.google.gson.reflect.TypeToken;
import io.dddspring.common.CommonTestCase;
import io.dddspring.common.event.TestableDomainEvent;
import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.event.EventStore;
import io.dddspring.common.notification.*;
import io.dddspring.common.persistence.PersistenceManagerProvider;
import io.dddspring.common.port.adapter.notification.KafkaNotificationPublisher;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernateEventStore;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernatePublishedNotificationTrackerStore;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
//import org.junit.Test;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Disabled
public class KafkaNotificationPublisherTest extends CommonTestCase {

    public KafkaNotificationPublisherTest() {
        super();
    }

    @Test
    public void testPublishNotifications() throws Exception {
        EventStore eventStore = this.eventStore();

        assertNotNull(eventStore);

        PublishedNotificationTrackerStore publishedNotificationTrackerStore =
                new HibernatePublishedNotificationTrackerStore(
                        new PersistenceManagerProvider(this.session()),
                        "topic-demo");

        NotificationPublisher notificationPublisher =
                new KafkaNotificationPublisher(
                        eventStore,
                        publishedNotificationTrackerStore,
                        "topic-demo");

        assertNotNull(notificationPublisher);

        notificationPublisher.publishNotifications();
        chkPublishNotifications("topic-demo",5);

    }

    @Test
    public void testPublishNotifications2() throws Exception {

        EventStore eventStore = this.eventStore();

        assertNotNull(eventStore);

        PublishedNotificationTrackerStore publishedNotificationTrackerStore2
                =(PublishedNotificationTrackerStore)this.applicationContext.getBean("publishedNotificationTrackerStore");

//        PublishedNotificationTrackerStore publishedNotificationTrackerStore2 =
//                new HibernatePublishedNotificationTrackerStore("unit.test");

        NotificationPublisher notificationPublisher2 =
                new KafkaNotificationPublisher(
                        eventStore,
                        publishedNotificationTrackerStore2,
                        "unit.test"); // exchangename

        assertNotNull(notificationPublisher2);
        notificationPublisher2.publishNotifications();

    }

    private void chkPublishNotifications(String tiopc,int num){


        Properties pc = new Properties();
//        pc.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.29.36.94:9092");
        pc.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.43.125:9092");
        pc.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pc.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        pc.put(ConsumerConfig.GROUP_ID_CONFIG, "duanjt_test");
        //在没有offset的情况下采取的拉取策略
        pc.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        pc.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");


        KafkaConsumer kafkaConsumer = new KafkaConsumer<String, String>(pc);
        kafkaConsumer.subscribe(Collections.singletonList(tiopc));// 订阅消息

        boolean nextloop=true;
        Integer loopi=0;
        while (nextloop && loopi<20)
        {   loopi++;
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
            if(records.count()>0){
                nextloop=false;
                for (ConsumerRecord<String, String> record : records) {

                    String rs=record.value();

                    System.out.println(String.format("消息接收"+loopi.toString()+"::topic:%s,offset:%d,消息:%s", //
                            record.topic(), record.offset(), rs));


                    NotificationReader reader = new NotificationReader(record.value());
//                    assertEquals("" + domainEvent.eventVersion(), reader.eventStringValue("eventVersion"));
                    System.out.println("NotificationReader::"+reader.eventStringValue("eventVersion")
                            +reader.eventStringValue("id")
                            +reader.eventStringValue("name")+reader.eventStringValue("occurredOn")
                    );
                    assertNotNull(reader);


                    //这个是成功的
                    Type type = new TypeToken<Notification<io.dddspring.common.event.TestableDomainEvent>>(){}.getType();
                    Notification<io.dddspring.common.event.TestableDomainEvent> notification= NotificationSerializer.instance().deserialize(rs,type);
                    io.dddspring.common.event.TestableDomainEvent event= notification.event();
                    System.out.println("NotificationSerializer"+event.id()+event.name()+event.eventVersion()+event.occurredOn());

                    assertNotNull(event);




                }
                kafkaConsumer.commitSync();
                assertEquals(num, records.count());
            }
        }

    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
//        DomainEventPublisher.instance().reset();

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
