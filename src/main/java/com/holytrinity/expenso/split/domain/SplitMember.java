package com.holytrinity.expenso.split.domain;

import com.holytrinity.expenso.user.domain.AssociatedUser;
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

    @Column(nullable = false)
    private Boolean hasPaid = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
}
