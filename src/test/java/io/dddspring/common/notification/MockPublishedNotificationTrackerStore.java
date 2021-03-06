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

import io.dddspring.common.notification.Notification;
import io.dddspring.common.notification.PublishedNotificationTracker;
import io.dddspring.common.notification.PublishedNotificationTrackerStore;

import java.util.List;

public class MockPublishedNotificationTrackerStore
    implements PublishedNotificationTrackerStore {

    public MockPublishedNotificationTrackerStore() {
        super();
    }

    @Override
    public PublishedNotificationTracker publishedNotificationTracker() {
        return new PublishedNotificationTracker("mock");
    }

    @Override
    public PublishedNotificationTracker publishedNotificationTracker(String aTypeName) {
        return new PublishedNotificationTracker("mock");
    }

    @Override
    public void trackMostRecentPublishedNotification(
            PublishedNotificationTracker aPublishedNotificationTracker,
            List<Notification> aNotifications) {
        // no-op
    }

    @Override
    public String typeName() {
        return "mock";
    }
}
