// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  // TODO: explain how algorithm works & big O complexity
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendeesRequested = request.getAttendees();
    int meetingDuration = (int) request.getDuration();

    // check for no attendees
    if (attendeesRequested.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // check duration of meeting request
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    List<TimeRange> badTimes = getBadTimes(events, attendeesRequested);

    // list to hold all the good times (everything not in badTimes)
    List<TimeRange> goodTimes = getGoodTimes(badTimes, meetingDuration);

    // Collections.sort(goodTimes, TimeRange.ORDER_BY_START);
    return goodTimes;
  }

  private List<TimeRange> getBadTimes(
      Collection<Event> events, Collection<String> attendeesRequested) {
    List<TimeRange> badTimes = new ArrayList<TimeRange>();

    // loop through all events and add  time to badTimes if attendee is in attendeesRequested
    for (Event conflictEvent : events) {
      Set<String> busyAttendees = conflictEvent.getAttendees();
      // while loop to see if any attendees in set are in attendeesRequested
      // if there is overlap, add time to badTimes and stop iterating
      Boolean conflict = false;
      Iterator<String> eventIter = busyAttendees.iterator();
      while (!conflict && eventIter.hasNext()) {
        if (attendeesRequested.contains(eventIter.next())) {
          conflict = true;
        }
      }
      if (conflict == true) {
        badTimes.add(conflictEvent.getWhen());
      }
    }

    // sort badTimes
    Collections.sort(badTimes, TimeRange.ORDER_BY_START);

    return badTimes;
  }

  private List<TimeRange> getGoodTimes(List<TimeRange> badTimes, int meetingDuration) {

    List<TimeRange> goodTimes = new ArrayList<TimeRange>();

    int goodStart = TimeRange.START_OF_DAY;
    int goodEnd = TimeRange.END_OF_DAY;

    for (TimeRange badRange : badTimes) {
      goodEnd = badRange.start();

      if (goodEnd > goodStart) {
        TimeRange goodRange = TimeRange.fromStartEnd(goodStart, goodEnd, false);
        if (goodRange.duration() >= meetingDuration) { // minimum meeting time is 30 min
          goodTimes.add(goodRange);
        }
      }

      // make sure nested events are handled correctly
      if (goodStart < (goodEnd + badRange.duration())) {
        goodStart = goodEnd + badRange.duration();
      }
    }

    // edge case for last time chunk
    if (goodStart < TimeRange.END_OF_DAY) {
      TimeRange goodRange = TimeRange.fromStartEnd(goodStart, TimeRange.END_OF_DAY, true);
      if (goodRange.duration() >= meetingDuration) {
        goodTimes.add(goodRange);
      }
    }

    return goodTimes;
  }
}
