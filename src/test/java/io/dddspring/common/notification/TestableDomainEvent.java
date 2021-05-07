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

import java.util.Date;

public class TestableDomainEvent implements DomainEvent {

    private int eventVersion;
    private long id;
    private String name;
    private Date occurredOn;

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getEventVersion() {
        return eventVersion;
    }
    public Date getOccurredOn() {
        return occurredOn;
    }

    public TestableDomainEvent(long anId, String aName) {
        super();

        this.setEventVersion(1);
        this.setId(anId);
        this.setName(aName);
        this.setOccurredOn(new Date());
    }

    public int eventVersion() {
        return eventVersion;
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Date occurredOn() {
        return this.occurredOn;
    }


    public void setEventVersion(int anEventVersion) {
        this.eventVersion = anEventVersion;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setOccurredOn(Date occurredOn) {
        this.occurredOn = occurredOn;
    }
}
