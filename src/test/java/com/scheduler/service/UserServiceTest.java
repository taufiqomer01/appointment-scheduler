package com.scheduler.service;

import com.scheduler.dto.RegistrationRequest;
import com.scheduler.dto.UserDTO;
import com.scheduler.entity.User;
import com.scheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private RegistrationRequest registrationRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest(
                "John Doe",
                "johndoe",
                "john@example.com",
                "1234567890",
                "password123"
        );
        
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPhone("1234567890");
        user.setPasswordHash("hashedPassword");
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);
    }
    
    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserDTO result = userService.registerUser(registrationRequest);
        
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("johndoe", result.getUsername());
        assertEquals(User.Role.USER, result.getRole());
        
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_UsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testAuthenticate_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        
        User result = userService.authenticate("johndoe", "password123");
        
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals(User.Status.ACTIVE, result.getStatus());
    }
}
