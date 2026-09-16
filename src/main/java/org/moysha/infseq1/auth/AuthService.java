package org.moysha.infseq1.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();
    private final AppUserRepository userRepository;
    private final AuthTokenRepository tokenRepository;

    public AuthService(AppUserRepository userRepository, AuthTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Optional<String> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !MessageDigest.isEqual(user.getPasswordHash(), hash(password, user.getSalt()))) {
            return Optional.empty();
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        tokenRepository.save(new AuthToken(token, user));
        return Optional.of(token);
    }

    @Transactional(readOnly = true)
    public Optional<String> findUsername(String token) {
        return tokenRepository.findUsernameByToken(token);
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
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        userRepository.save(new AppUser(username, salt, hash(password, salt)));
    }

    private byte[] hash(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Cannot hash password", exception);
        } finally {
            spec.clearPassword();
        }
    }

}
