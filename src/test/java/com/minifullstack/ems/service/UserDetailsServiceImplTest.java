package com.minifullstack.ems.service;

import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetails_whenEmailFound() {
        User user = User.builder()
                .id(1L).email("admin@test.com").password("encoded")
                .role(Role.ROLE_ADMIN).firstName("Ad").lastName("Min")
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(result.getUsername()).isEqualTo("admin@test.com");
        assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenNotFound() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@test.com");
    }

    @Test
    void loadUserByUsername_returnsCorrectRole_forReviewer() {
        User reviewer = User.builder()
                .id(2L).email("rev@test.com").password("encoded")
                .role(Role.ROLE_REVIEWER).firstName("Rev").lastName("User")
                .build();
        when(userRepository.findByEmail("rev@test.com")).thenReturn(Optional.of(reviewer));

        UserDetails result = userDetailsService.loadUserByUsername("rev@test.com");

        assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_REVIEWER");
    }
}
