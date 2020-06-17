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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * This algorithm creates a List of all the times at least one attendee is busy, then uses that
   * List to find all the times that everyone is available The worst-case runtime is
   * O(events*attendees)
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendeesRequired = request.getAttendees();
    Collection<String> attendeesOptional = request.getOptionalAttendees();
    Collection<String> attendeesCombined = new ArrayList<String>();
    attendeesCombined.addAll(attendeesRequired);
    attendeesCombined.addAll(attendeesOptional);

    int meetingDuration = (int) request.getDuration();
    List<TimeRange> combinedTimes = getGoodTimes(events, attendeesCombined, meetingDuration);

    if (attendeesRequired.isEmpty() || !combinedTimes.isEmpty()) {
      return combinedTimes;
    }

    // optimization algorithm
    // start with one less than max number of optional attendees, and if no times work it will try
    // one less
    int numberOptional = attendeesOptional.size() - 1;

    // in case there is only one optional attendee
    if (numberOptional < 0) {
      numberOptional = 0;
    }

    List<TimeRange> optimalTime =
        optimizeTime(numberOptional, events, attendeesRequired, attendeesOptional, meetingDuration);
    return optimalTime;
  }

  /**
   * Function that tests all possible combinations of a given number of optional attendees
   * (numOptional) and returns the times that work. If no times work, it recursively calls itself
   * with a smaller number of optional attendees until there are times that work. If times don't
   * work with any optional attendees, it will return the times for only the required attendees.
   */
  private static List<TimeRange> optimizeTime(
      int numOptional,
      Collection<Event> events,
      Collection<String> attendeesRequired,
      Collection<String> attendeesOptionalCollection,
      int meetingDuration) {

    // change optional attendee collection into list
    List<String> attendeesOptional = new ArrayList<String>(attendeesOptionalCollection);

    if (numOptional <= 0) { // no optional attendees work
      return getGoodTimes(events, attendeesRequired, meetingDuration);
    }

    // add all possible combinations of size numOptional to allOptionalCombos using helper method
    // combination()
    List<List<String>> allOptionalCombos = new LinkedList<List<String>>();
    allOptionalCombos.addAll(combination(attendeesOptional, numOptional));

    for (List<String> attendeeOptionalCombo : allOptionalCombos) {
      List<String> attendeesCombined = new ArrayList<String>();
      attendeesCombined.addAll(attendeesRequired);
      attendeesCombined.addAll(attendeeOptionalCombo);

      List<TimeRange> potentialTimes = getGoodTimes(events, attendeesCombined, meetingDuration);
      if (!potentialTimes.isEmpty()) {
        return potentialTimes;
      }
    }

    return optimizeTime(
        numOptional - 1, events, attendeesRequired, attendeesOptionalCollection, meetingDuration);
  }

  /**
   * Given a List and a size, returns a List of all the possible combinations of size (order within
   * combinations does not matter)
   */
  private static List<List<String>> combination(List<String> attendees, int size) {
    if (size == 0) {
      return Collections.singletonList(Collections.<String>emptyList());
    }

    if (attendees.isEmpty()) {
      return Collections.emptyList();
    }

    List<List<String>> combination = new LinkedList<List<String>>();

    String actual = attendees.iterator().next();

    List<String> subSet = new LinkedList<String>(attendees);
    subSet.remove(actual);

    List<List<String>> subSetCombination = combination(subSet, size - 1);

    for (List<String> set : subSetCombination) {
      List<String> newSet = new LinkedList<String>(set);
      newSet.add(0, actual);
      combination.add(newSet);
    }

    combination.addAll(combination(subSet, size));
    return combination;
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
  private static List<TimeRange> getGoodTimes(
      Collection<Event> events, Collection<String> attendees, int meetingDuration) {
    List<TimeRange> busyTimes = getBusyTimes(events, attendees);
    List<TimeRange> goodTimes = new ArrayList<TimeRange>();

    // beginning of an available period
    int goodStart = TimeRange.START_OF_DAY;

    for (TimeRange busyRange : busyTimes) {
      // end of an available period
      int goodEnd = busyRange.start();

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
