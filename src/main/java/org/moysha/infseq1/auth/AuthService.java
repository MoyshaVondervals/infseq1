package org.moysha.infseq1.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AppUserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository userRepository,
            AuthTokenRepository tokenRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public Optional<String> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, encodedPassword(user))) {
            return Optional.empty();
        }

        String token = jwtService.createToken(username);
        tokenRepository.save(new AuthToken(token, user));
        return Optional.of(token);
    }

    @Transactional(readOnly = true)
    public Optional<String> findUsername(String token) {
        return jwtService.validateAndGetSubject(token)
                .flatMap(subject -> tokenRepository.findUsernameByToken(token)
                        .filter(subject::equals));
    }

    @Transactional
    public boolean logout(String token) {
        if (!tokenRepository.existsById(token)) {
            return false;
        }
        tokenRepository.deleteById(token);
        return true;
    }

    @Transactional
    public void createUserIfMissing(String username, String password) {
        Optional<AppUser> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            if (!encodedPassword(user).startsWith("$2")) {
                user.setPasswordHash(passwordEncoder.encode(password).getBytes(StandardCharsets.UTF_8));
                userRepository.save(user);
            }
            return;
        }
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] passwordHash = passwordEncoder.encode(password).getBytes(StandardCharsets.UTF_8);
        userRepository.save(new AppUser(username, salt, passwordHash));
    }

    private String encodedPassword(AppUser user) {
        return new String(user.getPasswordHash(), StandardCharsets.UTF_8);
    }
}
