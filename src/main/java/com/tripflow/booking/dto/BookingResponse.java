package com.tripflow.booking.dto;

import com.tripflow.booking.entity.Booking;
import com.tripflow.booking.entity.BookingStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private final Long id;
    private final Long tripId;
    private final BookingStatus status;
    private final BigDecimal amountDue;

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getTripId(),
                booking.getStatus(),
                booking.getAmountDue()
        );
    }
}
