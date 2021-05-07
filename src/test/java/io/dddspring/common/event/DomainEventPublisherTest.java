package io.dddspring.common.event;

import io.dddspring.common.domain.model.DomainEventPublisher;
import io.dddspring.common.domain.model.DomainEventSubscriber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DomainEventPublisherTest {

    private boolean anotherEventHandled;
    private boolean eventHandled;

    @Test
    public void testDomainEventPublisherPublish() throws Exception {

        DomainEventPublisher.instance().reset();

        DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<TestableDomainEvent>(){
            @Override
            public void handleEvent(TestableDomainEvent aDomainEvent) {
                assertEquals(100L, aDomainEvent.id());
                assertEquals("test", aDomainEvent.name());
                eventHandled = true;
            }
            @Override
            public Class<TestableDomainEvent> subscribedToEventType() {
                return TestableDomainEvent.class;
            }
        });

        assertEquals(false,this.eventHandled);

        DomainEventPublisher.instance().publish(new TestableDomainEvent(100L, "test"));

        assertEquals(true,this.eventHandled);
    }
    @Test
    public void testDomainEventPublisherBlocked() throws Exception {
        DomainEventPublisher.instance().reset();

        DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<TestableDomainEvent>(){
            @Override
            public void handleEvent(TestableDomainEvent aDomainEvent) {
                assertEquals(100L, aDomainEvent.id());
                assertEquals("test", aDomainEvent.name());
                eventHandled = true;
                // attempt nested publish, which should not work
                DomainEventPublisher.instance().publish(new AnotherTestableDomainEvent(1000.0));
            }
            @Override
            public Class<TestableDomainEvent> subscribedToEventType() {
                return TestableDomainEvent.class;
            }
        });

        DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<AnotherTestableDomainEvent>(){
            @Override
            public void handleEvent(AnotherTestableDomainEvent aDomainEvent) {
                // should never be reached due to blocked publisher
                assertEquals(1000.0, aDomainEvent.value());
                anotherEventHandled = true;
            }
            @Override
            public Class<AnotherTestableDomainEvent> subscribedToEventType() {
                return AnotherTestableDomainEvent.class;
            }
        });

        assertFalse(this.eventHandled);
        assertFalse(this.anotherEventHandled);

        DomainEventPublisher.instance().publish(new TestableDomainEvent(100L, "test"));

        assertTrue(this.eventHandled);
        assertFalse(this.anotherEventHandled);
    }
}