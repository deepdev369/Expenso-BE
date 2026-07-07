package com.holytrinity.expenso.split.domain;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import com.holytrinity.expenso.user.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "splits")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted = false")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "userId", type = String.class))
@Filter(name = "tenantFilter", condition = "user_id = :userId")
@Getter
@Setter
public class Split {

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Id
    @Column(nullable = false, updatable = false)
    private String splitId;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitMethod splitMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_id", nullable = false)
    private AssociatedUser paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    @Column(nullable = false)
    private OffsetDateTime splitDate; // Changed from Long to OffsetDateTime per spec 'Date'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creatorUser;

    @OneToMany(mappedBy = "split", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SplitMember> members;

    @OneToMany(mappedBy = "split", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
}
