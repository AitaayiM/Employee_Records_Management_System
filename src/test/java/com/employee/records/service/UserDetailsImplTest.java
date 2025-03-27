package com.employee.records.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.employee.records.constant.UserRole;
import com.employee.records.entity.User;

public class UserDetailsImplTest {

    @Test
    void testBuildAndGetters() {
        // Création d'un utilisateur factice
        User dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setEmail("test@example.com");
        dummyUser.setPassword("password123");
        dummyUser.setRole(UserRole.ROLE_HR); // Assurez-vous que ROLE_HR existe dans UserRole

        // Construction de UserDetailsImpl à partir du dummyUser
        UserDetailsImpl userDetails = UserDetailsImpl.build(dummyUser);

        // Vérifier les getters
        assertEquals(1L, userDetails.getId());
        assertEquals("test@example.com", userDetails.getUsername()); // getUsername() retourne l'email
        assertEquals("password123", userDetails.getPassword());

        // Vérifier les authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        // Nous attendons une seule autorité: new SimpleGrantedAuthority("ROLE_HR")
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_HR")));

        // Vérifier les méthodes de contrôle du compte
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testEquals() {
        // Création de deux instances avec le même id
        UserDetailsImpl user1 = new UserDetailsImpl(
                1L,
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );
        UserDetailsImpl user2 = new UserDetailsImpl(
                1L,
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );

        // Ces deux instances doivent être égales
        assertEquals(user1, user2);

        // Création d'une instance avec un id différent
        UserDetailsImpl user3 = new UserDetailsImpl(
                2L,
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );
        assertNotEquals(user1, user3);
    }
}

