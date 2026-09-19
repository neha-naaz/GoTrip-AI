package com.tripflow.user.dto;

import com.tripflow.agency.entity.VerificationStatus;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final UserRole role;
    private final UserStatus status;
    /** Present for AGENCY users only. */
    private final VerificationStatus verificationStatus;
    /** Present for AGENCY users only. */
    private final String agencyName;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                null,
                null
        );
    }

    public static UserResponse fromAgency(
            User user,
            VerificationStatus verificationStatus,
            String agencyName
    ) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                verificationStatus,
                agencyName
        );
    }
}
