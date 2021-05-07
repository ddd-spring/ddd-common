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

package io.dddspring.common.notification;

import io.dddspring.common.domain.model.DomainEvent;
import io.dddspring.common.AssertionConcern;

import java.io.Serializable;
import java.util.Date;

public class  Notification<T extends DomainEvent> extends AssertionConcern implements Serializable {
//    public static Type getPtype(){
//        final Type ptype = getClass().getGenericSuperclass();
//    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public T getEvent() {
        return event;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public Date getOccurredOn() {
        return occurredOn;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getVersion() {
        return version;
    }

    private static final long serialVersionUID = 1L;
    private T event;
    private long notificationId;
    private Date occurredOn;
    private String typeName;
    private int version;

    public Notification(
            long aNotificationId,
            T anEvent) {

        this();

        this.setEvent(anEvent);
        this.setNotificationId(aNotificationId);
        this.setOccurredOn(anEvent.occurredOn());
        this.setTypeName(anEvent.getClass().getName());
        this.setVersion(anEvent.eventVersion());
    }

    @SuppressWarnings("unchecked")
    public T event() {
        return (T) this.event;
    }

    public long notificationId() {
        return this.notificationId;
    }

    public Date occurredOn() {
        return this.occurredOn;
    }

    public String typeName() {
        return this.typeName;
    }

    public int version() {
        return version;
    }

    @Override
    public boolean equals(Object anObject) {
        boolean equalObjects = false;

        if (anObject != null && this.getClass() == anObject.getClass()) {
            Notification typedObject = (Notification) anObject;
            equalObjects = this.notificationId() == typedObject.notificationId();
        }

        return equalObjects;
    }

    @Override
    public int hashCode() {
        int hashCodeValue =
            + (3017 * 197)
            + (int) this.notificationId();

        return hashCodeValue;
    }

    @Override
    public String toString() {
        return "Notification [event=" + event + ", notificationId=" + notificationId
                + ", occurredOn=" + occurredOn + ", typeName="
                + typeName + ", version=" + version + "]";
    }

    protected Notification() {
        super();
    }

    public void setEvent(T anEvent) {
        this.assertArgumentNotNull(anEvent, "The event is required.");

        this.event = anEvent;
    }

    public void setNotificationId(long aNotificationId) {
        this.notificationId = aNotificationId;
    }

    public void setOccurredOn(Date anOccurredOn) {
        this.occurredOn = anOccurredOn;
    }

    public void setTypeName(String aTypeName) {
        this.assertArgumentNotEmpty(aTypeName, "The type name is required.");
        this.assertArgumentLength(aTypeName, 100, "The type name must be 100 characters or less.");

        this.typeName = aTypeName;
    }

    public void setVersion(int aVersion) {
        this.version = aVersion;
    }
}
