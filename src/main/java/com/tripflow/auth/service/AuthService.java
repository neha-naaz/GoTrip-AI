package com.tripflow.auth.service;

import com.tripflow.agency.entity.AgencyProfile;
import com.tripflow.agency.repository.AgencyProfileRepository;
import com.tripflow.auth.dto.AuthResponse;
import com.tripflow.auth.dto.RegisterRequest;
import com.tripflow.auth.security.JwtService;
import com.tripflow.common.exception.EmailAlreadyExistsException;
import com.tripflow.common.exception.InvalidRegistrationException;
import com.tripflow.user.entity.User;
import com.tripflow.user.entity.UserRole;
import com.tripflow.user.entity.UserStatus;
import com.tripflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AgencyProfileRepository agencyProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserRole role = parseRole(request.getRole());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        if (role == UserRole.AGENCY) {
            String agencyName = StringUtils.hasText(request.getAgencyName())
                    ? request.getAgencyName().trim()
                    : request.getName().trim();

            AgencyProfile agencyProfile = new AgencyProfile();
            agencyProfile.setUserId(user.getId());
            agencyProfile.setAgencyName(agencyName);
            agencyProfileRepository.save(agencyProfile);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name());
    }

    private UserRole parseRole(String roleValue) {
        if (!StringUtils.hasText(roleValue)) {
            throw new InvalidRegistrationException("Role is required");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRegistrationException("Invalid role: " + roleValue);
        }

        if (role == UserRole.ADMIN) {
            throw new InvalidRegistrationException("ADMIN registration is not allowed");
        }

        return role;
    }
}
