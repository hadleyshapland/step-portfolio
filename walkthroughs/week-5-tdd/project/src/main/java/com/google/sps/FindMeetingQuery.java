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
  // This algorithm creates a List of all the times at least one attendee is busy, then uses that
  // List to find all the times that everyone is available
  // The worst-case runtime is O(n^2)
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

    // list to hold all the available times (everything not in badTimes)
    List<TimeRange> goodTimes = getGoodTimes(badTimes, meetingDuration);
    return goodTimes;
  }

  /** Returns a List with all the times that requested attendees have conflicts. */
  private List<TimeRange> getBadTimes(
      Collection<Event> events, Collection<String> attendeesRequested) {
    List<TimeRange> badTimes = new ArrayList<TimeRange>();

    // iterate through all events and add  time to badTimes if attendee is in attendeesRequested
    for (Event conflictEvent : events) {
      Set<String> busyAttendees = conflictEvent.getAttendees();

      // loop to see if any attendees in conflictEvent are in attendeesRequested
      Boolean conflict = false;
      Iterator<String> eventIter = busyAttendees.iterator();
      while (!conflict && eventIter.hasNext()) {
        if (attendeesRequested.contains(eventIter.next())) {
          conflict = true;
        }
      }
      if (conflict == true) { // there was an attendee with a conflict, so add bad time to List
        badTimes.add(conflictEvent.getWhen());
      }
    }

    // sort badTimes
    Collections.sort(badTimes, TimeRange.ORDER_BY_START);
    return badTimes;
  }

  /**
   * Returns a List with all the TimeRanges between the badTimes that are greater or equal to the
   * requested meeting duration.
   */
  private List<TimeRange> getGoodTimes(List<TimeRange> badTimes, int meetingDuration) {
    List<TimeRange> goodTimes = new ArrayList<TimeRange>();

    // beginning of an available period
    int goodStart = TimeRange.START_OF_DAY;

    // end of an available period
    int goodEnd = TimeRange.END_OF_DAY;

    for (TimeRange badRange : badTimes) {
      goodEnd = badRange.start();

      if (goodEnd > goodStart) {
        TimeRange goodRange = TimeRange.fromStartEnd(goodStart, goodEnd, false);
        if (goodRange.duration() >= meetingDuration) {
          goodTimes.add(goodRange);
        }
      }

      // only increment goodStart if it is before the next ending period - takes care of nested
      // events
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
