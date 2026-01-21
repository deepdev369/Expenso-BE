package com.holytrinity.expenso.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "\"user\"")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {

        @Id
        @Column(nullable = false, updatable = false)
        @SequenceGenerator(name = "primary_sequence", sequenceName = "primary_sequence", allocationSize = 1, initialValue = 10000)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_sequence")
        private Long userId;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String userName;

        @Column
        private String phone;

        @Column(columnDefinition = "jsonb")
        @JdbcTypeCode(SqlTypes.JSON)
        private List<String> authProviders;

        @Column
        private String passwordHash;

        @Column(nullable = false)
        private Boolean emailVerified;

        @Column(nullable = false)
        private String defaultCurrency;

        @Column(nullable = false)
        private String language;

        @Column(nullable = false)
        private Boolean smsConsentGranted;

        @Column(nullable = false)
        private Boolean voiceConsentGranted;

        @Column
        private Long consentGrantedAt;

        @Column
        private Long lastLoginAt;

        @jakarta.persistence.Version
        @Column(nullable = false)
        private Long version;

        @CreatedDate
        @Column(nullable = false, updatable = false)
        private OffsetDateTime dateCreated;

        @LastModifiedDate
        @Column(nullable = false)
        private OffsetDateTime lastUpdated;

}
