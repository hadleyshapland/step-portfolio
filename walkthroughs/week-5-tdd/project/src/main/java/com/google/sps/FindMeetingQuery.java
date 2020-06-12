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
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * This algorithm creates a List of all the times at least one attendee is busy, then uses that
   * List to find all the times that everyone is available The worst-case runtime is
   * O(events*attendees)
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendeesRequested = request.getAttendees();

    // check for no attendees
    if (attendeesRequested.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // check duration of meeting request
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    int meetingDuration = (int) request.getDuration();
    return getGoodTimes(getBusyTimes(events, attendeesRequested), meetingDuration);
  }

  /**
   * Returns a sorted List (earliest to latest by start time) with all the times that requested
   * attendees have conflicts.
   */
  private static List<TimeRange> getBusyTimes(
      Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> busyTimes = new ArrayList<TimeRange>();

    // iterate through all events and add  time to busyTimes if attendee is in the request
    for (Event conflictEvent : events) {
      Set<String> busyAttendees = conflictEvent.getAttendees();

      // loop to see if any attendees in conflictEvent are requested attendees
      for (String attendee : busyAttendees) {
        if (attendees.contains(attendee)) {
          busyTimes.add(conflictEvent.getWhen());
          break;
        }
      }
    }

    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
    return busyTimes;
  }

  /**
   * Returns a List with all the TimeRanges between the busyTimes that are greater or equal to the
   * requested meeting duration.
   */
  private static List<TimeRange> getGoodTimes(List<TimeRange> busyTimes, int meetingDuration) {
    List<TimeRange> goodTimes = new ArrayList<TimeRange>();

    // beginning of an available period
    int goodStart = TimeRange.START_OF_DAY;

    // end of an available period
    int goodEnd = TimeRange.END_OF_DAY;

    for (TimeRange busyRange : busyTimes) {
      goodEnd = busyRange.start();

      if (goodEnd > goodStart) {
        TimeRange goodRange = TimeRange.fromStartEnd(goodStart, goodEnd, false);
        if (goodRange.duration() >= meetingDuration) {
          goodTimes.add(goodRange);
        }
      }

      // only increment goodStart if it is earlier than the next ending period - takes care of
      // nested
      // events with the following format (don't want goodStart to go back in time):
      // Events  :       |----A----|
      //                   |--B--|
      // Day     : |---------------------|
      goodStart = Math.max(goodStart, busyRange.end());
    }

    // edge case for last time chunk
    if (TimeRange.END_OF_DAY - goodStart > meetingDuration) {
      goodTimes.add(TimeRange.fromStartEnd(goodStart, TimeRange.END_OF_DAY, true));
    }

    return goodTimes;
  }
}
