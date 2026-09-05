package com.tripflow.group.exception;

public class TripGroupNotFoundException extends RuntimeException {

  public TripGroupNotFoundException(Long tripId) {
    super("Group not found for trip: " + tripId);
  }
}
