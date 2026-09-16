package org.moysha.infseq1.auth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    @Id
    @Column(length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    protected AuthToken() {
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JPA association must reference its entity")
    public AuthToken(String token, AppUser user) {
        this.token = token;
        this.user = user;
    }
}
