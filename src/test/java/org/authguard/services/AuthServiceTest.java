package org.authguard.services;

import org.authguard.infrastructure.LuckPermsIntegration;
import org.authguard.models.User;
import org.authguard.repositories.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private LuckPermsIntegration luckPermsIntegration;

    private AuthService authService;
    private final UUID testUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Standard UUID
    private final String defaultRank = "default";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, sessionManager, luckPermsIntegration, defaultRank);
    }

    @Test
    void register_Success() {
        when(userRepository.findByUuid(testUuid)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(userRepository.save(any(User.class))).thenReturn(CompletableFuture.completedFuture(null));

        boolean result = authService.register(testUuid, "TestUser", "password123").join();

        assertTrue(result);
        verify(userRepository).save(any(User.class));
        verify(sessionManager).authenticate(testUuid);
        verify(luckPermsIntegration).assignRank(eq(testUuid), eq(defaultRank));
    }

    @Test
    void register_AlreadyExists() {
        User existingUser = mock(User.class);
        when(userRepository.findByUuid(testUuid))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(existingUser)));

        boolean result = authService.register(testUuid, "TestUser", "password123").join();

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(sessionManager, never()).authenticate(any());
    }

    @Test
    void login_Success() {
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn(hashedPassword);
        when(user.getRank()).thenReturn("vip");

        when(userRepository.findByUuid(testUuid)).thenReturn(CompletableFuture.completedFuture(Optional.of(user)));

        boolean result = authService.login(testUuid, password).join();

        assertTrue(result);
        verify(sessionManager).authenticate(testUuid);
        verify(luckPermsIntegration).assignRank(testUuid, "vip");
    }

    @Test
    void login_WrongPassword() {
        String password = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn(hashedPassword);

        when(userRepository.findByUuid(testUuid)).thenReturn(CompletableFuture.completedFuture(Optional.of(user)));

        boolean result = authService.login(testUuid, wrongPassword).join();

        assertFalse(result);
        verify(sessionManager, never()).authenticate(any());
    }

    @Test
    void login_NotRegistered() {
        when(userRepository.findByUuid(testUuid)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        boolean result = authService.login(testUuid, "password123").join();

        assertFalse(result);
        verify(sessionManager, never()).authenticate(any());
    }
}
