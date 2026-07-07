package com.holytrinity.expenso.split.domain;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import com.holytrinity.expenso.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "split_groups")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted = false")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "userId", type = String.class))
@Filter(name = "tenantFilter", condition = "user_id = :userId")
@Getter
@Setter
public class Group {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "associated_user_id")
    )
    private List<AssociatedUser> members;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Owner

    @Column(nullable = false)
    private Boolean deleted = false;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
