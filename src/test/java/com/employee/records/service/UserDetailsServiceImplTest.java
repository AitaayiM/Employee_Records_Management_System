package com.employee.records.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.employee.records.entity.User;
import com.employee.records.repository.UserRepository;
import com.employee.records.constant.UserRole;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private User dummyUser;

    @BeforeEach
    void setUp() {
        // Création d'un utilisateur factice pour les tests
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setEmail("test@example.com");
        dummyUser.setPassword("password123");
        dummyUser.setRole(UserRole.ROLE_HR);
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Simule que le repository renvoie l'utilisateur factice
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(dummyUser));

        // Appel de la méthode loadUserByUsername
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername("test@example.com");

        // Vérifie que l'objet retourné n'est pas null et que le username (ici l'email) correspond
        assertNotNull(userDetails);
        assertEquals(dummyUser.getEmail(), userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Simule que le repository ne trouve pas l'utilisateur
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Vérifie qu'une UsernameNotFoundException est levée si l'utilisateur n'est pas trouvé
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsServiceImpl.loadUserByUsername("notfound@example.com");
        });
    }
}

