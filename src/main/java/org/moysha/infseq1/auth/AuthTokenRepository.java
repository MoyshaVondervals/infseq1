package org.moysha.infseq1.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    @Query("select token.user.username from AuthToken token where token.token = :token")
    Optional<String> findUsernameByToken(@Param("token") String token);
}
