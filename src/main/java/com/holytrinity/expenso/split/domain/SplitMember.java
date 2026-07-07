package com.holytrinity.expenso.split.domain;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "split_members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SplitMember {

    @Id
    @Column(nullable = false, updatable = false)
    private String splitMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_id", nullable = false)
    private Split split;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associated_user_id", nullable = false)
    private AssociatedUser associatedUser;

    @Column(nullable = false)
    private Double amountOwed;

    @Column
    private String rationale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    @Column(nullable = false)
    private Double amountSettled = 0.0;

    @Column(nullable = false)
    private Double amountRemaining;

    @OneToMany(mappedBy = "splitParticipant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repayment> repayments = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
}
