package com.holytrinity.expenso.plan.domain;

import com.holytrinity.expenso.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "goals")
@EntityListeners(AuditingEntityListener.class)
@org.hibernate.annotations.SQLRestriction("deleted = false")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "userId", type = String.class))
@Filter(name = "tenantFilter", condition = "user_id = :userId")
@Getter
@Setter
public class Goal {

    @jakarta.persistence.Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Id
    @Column(nullable = false, updatable = false)
    private String goalId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double currentAmount;

    @Column(nullable = false)
    private Double targetAmount;

    @Column(nullable = false)
    private String projectedDate;

    @Column(nullable = false)
    private String colorHex;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @Column(columnDefinition = "TEXT")
    private String aiSuggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long goalDateCreated;

    @Column(nullable = false)
    private Long goalLastUpdated;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
}
