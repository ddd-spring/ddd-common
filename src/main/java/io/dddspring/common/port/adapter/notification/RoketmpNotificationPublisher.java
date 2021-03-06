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
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.ArrayList;
import java.util.List;
public class RoketmpNotificationPublisher implements NotificationPublisher {

    private EventStore eventStore;
    private String exchangeName;
    private DefaultMQProducer producer;


    private String nameServer;
    private String producerGroup;
    private PublishedNotificationTrackerStore publishedNotificationTrackerStore;

    public RoketmpNotificationPublisher setNameServer(String nameServer) {
        this.nameServer = nameServer;
        return this;
    }

    public RoketmpNotificationPublisher setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }
    public RoketmpNotificationPublisher setExchangeName(String anExchangeName) {
        this.exchangeName = anExchangeName;
        return this;
    }
    public RoketmpNotificationPublisher(
            EventStore anEventStore,
            PublishedNotificationTrackerStore aPublishedNotificationTrackerStore) {

        super();


        this.setEventStore(anEventStore);
//        this.setExchangeName((String) aMessagingLocator);
        this.setPublishedNotificationTrackerStore(aPublishedNotificationTrackerStore);

    }

    private  DefaultMQProducer producter(){
        if(producer==null){
            producer = new DefaultMQProducer(producerGroup);////producerGroup
            //??????NameServer??????
            producer.setNamesrvAddr(nameServer); //??????????????????
            producer.setVipChannelEnabled(false);

            try {
                producer.start();
            }
            catch (MQClientException e){
                e.printStackTrace();
            }
            finally {
                return producer;
            }

        }
        return producer;
    }


    @Override
    public void publishNotifications(){
        PublishedNotificationTracker publishedNotificationTracker =
                this.publishedNotificationTrackerStore().publishedNotificationTracker();//??????????????????????????????????????????????????????

        List<Notification> notifications =
            this.listUnpublishedNotifications(
                    publishedNotificationTracker.mostRecentPublishedNotificationId());
if(notifications.size()>0)
        System.out.println("RoketmpNotificationPublisher.publishNotifications::"+notifications.size());
            for (Notification notification : notifications) {
                this.publish(notification, producter());
            }

            this.publishedNotificationTrackerStore()
                .trackMostRecentPublishedNotification(
                    publishedNotificationTracker,
                    notifications);

    }

    @Override
    public boolean internalOnlyTestConfirmation() {
        throw new UnsupportedOperationException("Not supported by production implementation. ????????????????????????");
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



    private List<Notification> listUnpublishedNotifications(
            long aMostRecentPublishedMessageId) {
        List<StoredEvent> storedEvents =
            this.eventStore().allStoredEventsSince(aMostRecentPublishedMessageId);//???eventstore???????????????????????????????????????

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
            DefaultMQProducer roketProducer) {

        String notification =
            NotificationSerializer
                .instance()
                .serialize(aNotification);

        try {
            /**
             * Producer????????????????????????????????????start?????????????????????????????????
             * ????????????????????????????????????????????????????????????start??????
             */

//????????????
            Message msg = new Message(this.exchangeName() /* Topic */,
                    aNotification.typeName()
                    /* Tag */,
                    (notification).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
//??????????????????
            System.out.printf("RoketmpNotificationPublisher.publish::%s\n", notification);
            SendResult sendResult = roketProducer.send(msg);
            System.out.printf("RoketmpNotificationPublisher.sendResult::%s%n\n", sendResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
//            roketProducer.shutdown();
        }

    }

    private PublishedNotificationTrackerStore publishedNotificationTrackerStore() {
        return publishedNotificationTrackerStore;
    }

    private void setPublishedNotificationTrackerStore(PublishedNotificationTrackerStore publishedNotificationTrackerStore) {
        this.publishedNotificationTrackerStore = publishedNotificationTrackerStore;
    }
}
