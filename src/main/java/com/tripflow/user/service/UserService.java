package com.tripflow.user.service;

import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.user.dto.UserResponse;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AgencyProfileRepository agencyProfileRepository;

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == UserRole.AGENCY) {
            return agencyProfileRepository.findByUserId(user.getId())
                    .map(profile -> UserResponse.fromAgency(
                            user,
                            profile.getVerificationStatus(),
                            profile.getAgencyName()
                    ))
                    .orElseGet(() -> UserResponse.from(user));
        }

        return UserResponse.from(user);
    }
}
