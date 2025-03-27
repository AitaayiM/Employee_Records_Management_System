package com.employee.records.service;

import com.employee.records.constant.UserRole;
import com.employee.records.entity.User;
import com.employee.records.payload.request.LoginRequest;
import com.employee.records.payload.request.SignupRequest;
import com.employee.records.payload.response.JwtResponse;
import com.employee.records.repository.UserRepository;
import com.employee.records.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActivateUser() {
        User user = new User("testuser@example.com", "password", UserRole.ROLE_HR);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.activateUser(1L);

        assertTrue(user.getActive());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAuthenticateUser() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        User user = new User("testuser@example.com", "password", UserRole.ROLE_HR);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mocked-jwt-token");

        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        assertNotNull(jwtResponse);
        assertEquals("mocked-jwt-token", jwtResponse.getAccessToken());
        assertEquals("testuser@example.com", jwtResponse.getEmail());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
    }

    @Test
    void testIsEmailTaken() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(authService.isEmailTaken("test@example.com"));
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void testRegisterUser() {
        SignupRequest signUpRequest = new SignupRequest("testuser@example.com", "password", UserRole.ROLE_HR);

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(new User("testuser@example.com", "encoded-password", UserRole.ROLE_HR));

        authService.registerUser(signUpRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }
}
