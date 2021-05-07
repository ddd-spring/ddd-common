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

package io.dddspring.common.port.adapter.notification;

import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.event.EventStore;
import io.dddspring.common.event.StoredEvent;
import io.dddspring.common.notification.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaNotificationPublisher implements NotificationPublisher {

    private EventStore eventStore;
    private String exchangeName;
    private KafkaProducer kafkaProducer;
    private PublishedNotificationTrackerStore publishedNotificationTrackerStore;


    public KafkaNotificationPublisher(
            EventStore anEventStore,
            PublishedNotificationTrackerStore aPublishedNotificationTrackerStore,
            Object aMessagingLocator) {

        super();



        this.setEventStore(anEventStore);
        this.setExchangeName((String) aMessagingLocator);
        this.setPublishedNotificationTrackerStore(aPublishedNotificationTrackerStore);


        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.29.36.94:9092");//kafka地址，多个地址用逗号分割
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        this.kafkaProducer = new KafkaProducer<>(p);

    }

    @Override
    public void publishNotifications() {
        PublishedNotificationTracker publishedNotificationTracker =
                this.publishedNotificationTrackerStore().publishedNotificationTracker();//通过类型获取该类型所有需要发布的消息

        List<Notification> notifications =
            this.listUnpublishedNotifications(
                    publishedNotificationTracker.mostRecentPublishedNotificationId());

        try {
            for (Notification notification : notifications) {
                this.publish(notification, kafkaProducer);
            }

            this.publishedNotificationTrackerStore()
                .trackMostRecentPublishedNotification(
                    publishedNotificationTracker,
                    notifications);
        } finally {
            kafkaProducer.close();
        }
    }

    @Override
    public boolean internalOnlyTestConfirmation() {
        throw new UnsupportedOperationException("Not supported by production implementation. 不可用于生产环境");
    }

    private EventStore eventStore() {
        return this.eventStore;
    }

    private void setEventStore(EventStore anEventStore) {
        this.eventStore = anEventStore;
    }

    private String exchangeName() {
        return this.exchangeName;
    }

    private void setExchangeName(String anExchangeName) {
        this.exchangeName = anExchangeName;
    }

    private List<Notification> listUnpublishedNotifications(
            long aMostRecentPublishedMessageId) {
        List<StoredEvent> storedEvents =
            this.eventStore().allStoredEventsSince(aMostRecentPublishedMessageId);//从eventstore中获取跟踪器设置的指定条数

        List<Notification> notifications =
            this.notificationsFrom(storedEvents);

        return notifications;
    }

    private List<Notification> notificationsFrom(List<StoredEvent> aStoredEvents) {
        List<Notification> notifications =
            new ArrayList<Notification>(aStoredEvents.size());

        for (StoredEvent storedEvent : aStoredEvents) {
            DomainEvent domainEvent = storedEvent.toDomainEvent();

            Notification notification =
                new Notification(storedEvent.eventId(), domainEvent);

            notifications.add(notification);
        }

        return notifications;
    }

    private void publish(
            Notification aNotification,
            KafkaProducer kafkaProducer) {
        String notification =
            NotificationSerializer
                .instance()
                .serialize(aNotification);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(this.exchangeName(), notification);
        kafkaProducer.send(record);
    }

    private PublishedNotificationTrackerStore publishedNotificationTrackerStore() {
        return publishedNotificationTrackerStore;
    }

    private void setPublishedNotificationTrackerStore(PublishedNotificationTrackerStore publishedNotificationTrackerStore) {
        this.publishedNotificationTrackerStore = publishedNotificationTrackerStore;
    }
}
