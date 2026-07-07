package com.holytrinity.expenso.split.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "split_repayments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Repayment {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_member_id", nullable = false)
    private SplitMember splitParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_id", nullable = false)
    private Split split;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentMethod method;

    @Column
    private String note;

    @Column(nullable = false)
    private OffsetDateTime repaidAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
