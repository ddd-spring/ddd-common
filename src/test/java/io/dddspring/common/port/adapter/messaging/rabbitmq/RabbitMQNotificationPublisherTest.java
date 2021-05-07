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

package io.dddspring.common.port.adapter.messaging.rabbitmq;

import io.dddspring.common.CommonTestCase;
import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.domain.model.DomainEventPublisher;
import io.dddspring.common.event.EventStore;
import io.dddspring.common.event.TestableDomainEvent;
import io.dddspring.common.notification.NotificationPublisher;
import io.dddspring.common.notification.PublishedNotificationTrackerStore;
import io.dddspring.common.persistence.PersistenceManagerProvider;
import io.dddspring.common.port.adapter.notification.RabbitMQNotificationPublisher;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernateEventStore;
import io.dddspring.common.port.adapter.persistence.hibernate.HibernatePublishedNotificationTrackerStore;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

//import static junit.framework.TestCase.*;
@Disabled
public class RabbitMQNotificationPublisherTest extends CommonTestCase {

    public RabbitMQNotificationPublisherTest() {
        super();
    }

    @Test
    public void testPublishNotifications() throws Exception {
        EventStore eventStore = this.eventStore();

        assertNotNull(eventStore);

        PublishedNotificationTrackerStore publishedNotificationTrackerStore =
                new HibernatePublishedNotificationTrackerStore(
                        new PersistenceManagerProvider(this.session()),
                        "unit.test");

        NotificationPublisher notificationPublisher =
                new RabbitMQNotificationPublisher(
                        eventStore,
                        publishedNotificationTrackerStore,
                        "unit.test");

        assertNotNull(notificationPublisher);

        notificationPublisher.publishNotifications();

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
                new RabbitMQNotificationPublisher(
                        eventStore,
                        publishedNotificationTrackerStore2,
                        "unit.test"); // exchangename

        assertNotNull(notificationPublisher2);
        notificationPublisher2.publishNotifications();

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
