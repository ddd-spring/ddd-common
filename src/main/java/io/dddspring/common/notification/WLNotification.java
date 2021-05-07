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

public class WLNotification extends AssertionConcern implements Serializable {

    private static final long serialVersionUID = 1L;
    private String eventdata;
    private long notificationId;
    private Date occurredOn;
    private String typeName;
    private int version;

    public WLNotification(
            long aNotificationId,
            DomainEvent anEvent,
            String typename) {

        this();

        this.setEvent(anEvent);
        this.setNotificationId(aNotificationId);
        this.setOccurredOn(anEvent.occurredOn());
        this.setTypeName(typename);
        this.setVersion(anEvent.eventVersion());
    }

    @SuppressWarnings("unchecked")
    public String event() {
        return this.eventdata;
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
            WLNotification typedObject = (WLNotification) anObject;
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
        return "Notification [event=" + eventdata + ", notificationId=" + notificationId
                + ", occurredOn=" + occurredOn + ", typeName="
                + typeName + ", version=" + version + "]";
    }

    protected WLNotification() {
        super();
    }


    protected void setEvent(DomainEvent anEvent) {
        this.assertArgumentNotNull(anEvent, "The event is required.");

        this.eventdata = anEvent.toString();
    }

    protected void setNotificationId(long aNotificationId) {
        this.notificationId = aNotificationId;
    }

    protected void setOccurredOn(Date anOccurredOn) {
        this.occurredOn = anOccurredOn;
    }

    protected void setTypeName(String aTypeName) {
        this.assertArgumentNotEmpty(aTypeName, "The type name is required.");
        this.assertArgumentLength(aTypeName, 100, "The type name must be 100 characters or less.");

        this.typeName = aTypeName;
    }

    private void setVersion(int aVersion) {
        this.version = aVersion;
    }
}
