package com.holytrinity.expenso.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_oauth_providers")
@Getter
@Setter
public class UserOAuthProvider {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String providerName; // e.g. "GOOGLE", "APPLE"

    @Column(nullable = false)
    private String providerSubjectId; // The immutable `sub` claim
}
