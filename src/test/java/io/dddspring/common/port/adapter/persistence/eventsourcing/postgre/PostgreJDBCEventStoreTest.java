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

package io.dddspring.common.port.adapter.persistence.eventsourcing.postgre;

import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.domain.model.DomainEventPublisher;
import io.dddspring.common.event.TestableDomainEvent;
import io.dddspring.common.event.sourcing.*;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//import static junit.framework.TestCase.*;

import static org.junit.jupiter.api.Assertions.*;

public class PostgreJDBCEventStoreTest {

    private ApplicationContext applicationContext;
    private EventStore eventStore;

    public PostgreJDBCEventStoreTest() {
        super();
    }
    @Test
    public void testConnectAndClose() throws Exception {
        assertNotNull(eventStore);
    }

    @Test
    public void testAppend() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 2; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(2, eventStream.version());
        assertEquals(2, eventStream.events().size());

        for (int idx = 1; idx <= 2; ++idx) {
            DomainEvent domainEvent = eventStream.events().get(idx - 1);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }
    }

    @Test
    public void testAppendWrongVersion() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(10, eventStream.version());
        assertEquals(10, eventStream.events().size());

        events.clear();
        events.add(new TestableDomainEvent(11, "Name: " + 11));

        for (int idx = 0; idx < 3; ++idx) {
            try {
                this.eventStore.appendWith(eventId.withStreamVersion(8 + idx), events);

                fail("Should have thrown an exception.");

            } catch (EventStoreAppendException e) {
                // good
            }
        }

        // this should succeed

        this.eventStore.appendWith(eventId.withStreamVersion(11), events);
    }

    @Test
    public void testEventsSince() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        List<DispatchableDomainEvent> loggedEvents =
                this.eventStore.eventsSince(this.greatestEventId() - 8L);

        assertEquals(8, loggedEvents.size());
    }

    @Test
    public void testEventStreamSince() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        for (int idx = 10; idx >= 1; --idx) {
            EventStream eventStream = this.eventStore.eventStreamSince(eventId.withStreamVersion(idx));

            assertEquals(10, eventStream.version());
            assertEquals(10 - idx + 1, eventStream.events().size());

            DomainEvent domainEvent = eventStream.events().get(0);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }

        try {
            this.eventStore.eventStreamSince(eventId.withStreamVersion(11));

            fail("Should have thrown an exception.");

        } catch (EventStoreException e) {
            // good
        }
    }

    @Test
    public void testFullEventStreamForStreamName() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 3; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(3, eventStream.version());
        assertEquals(3, eventStream.events().size());

        events.clear();
        events.add(new TestableDomainEvent(4, "Name: " + 4));

        this.eventStore.appendWith(eventId.withStreamVersion(4), events);

        eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(4, eventStream.version());
        assertEquals(4, eventStream.events().size());

        for (int idx = 1; idx <= 4; ++idx) {
            DomainEvent domainEvent = eventStream.events().get(idx - 1);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }
    }

    @BeforeEach
    public void setUp() throws Exception {


        DomainEventPublisher.instance().reset();

        applicationContext = new ClassPathXmlApplicationContext("applicationContext-common.xml");

        this.eventStore = PostgreJDBCEventStore.instance();
        this.eventStore.purge();
    }

    @AfterEach
    public void tearDown() throws Exception {
//        this.eventStore.purge();
        this.eventStore.close();

    }

    private long greatestEventId() {
        long greatestEventId = 0;

        DataSource dataSource = (DataSource) applicationContext.getBean("eventStoreDataSource");
        Connection connection = null;
        ResultSet result = null;

        try {
            connection = dataSource.getConnection();

            PreparedStatement statement =
                    dataSource
                        .getConnection()
                        .prepareStatement("SELECT MAX(event_id) from common.wl_es_event_store");

            result = statement.executeQuery();

            if (result.next()) {
                greatestEventId = result.getLong(1);
            }

        } catch (Throwable t) {

        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Throwable t) {
                    // ignore
                }
            }
            try {
                connection.close();
            } catch (Throwable t) {
                // ignore
            }
        }

        return greatestEventId;
    }
}
