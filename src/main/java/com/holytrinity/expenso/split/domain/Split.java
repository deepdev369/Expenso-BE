package com.holytrinity.expenso.split.domain;

import com.holytrinity.expenso.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

@Entity
@Table(name = "splits")
@EntityListeners(AuditingEntityListener.class)
@org.hibernate.annotations.SQLRestriction("deleted = false")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "userId", type = String.class))
@Filter(name = "tenantFilter", condition = "user_id = :userId")
@Getter
@Setter
public class Split {

    @jakarta.persistence.Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Id
    @Column(nullable = false, updatable = false)
    private String splitId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private String splitMethod; // EQUAL, CUSTOM, PERCENTAGE

    @Column(nullable = false)
    private Long splitDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creatorUser;

    @OneToMany(mappedBy = "split", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SplitMember> members;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
}
