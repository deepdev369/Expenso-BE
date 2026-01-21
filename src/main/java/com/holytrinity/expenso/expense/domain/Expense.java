package com.holytrinity.expenso.expense.domain;

import com.holytrinity.expenso.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "expense")
@EntityListeners(AuditingEntityListener.class)
@org.hibernate.annotations.SQLRestriction("deleted = false")
@Getter
@Setter
public class Expense {

        @jakarta.persistence.Version
        @Column(nullable = false)
        private Long version;

        @Column(nullable = false)
        private Boolean deleted = false;

        @Id
        @Column(nullable = false, updatable = false)
        @SequenceGenerator(name = "primary_sequence", sequenceName = "primary_sequence", allocationSize = 1, initialValue = 10000)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_sequence")
        private Long expenseId;

        @Column(nullable = false)
        private Double amount;

        @Column(nullable = false)
        private String category;

        @Column
        private String subCategory;

        @Column(columnDefinition = "jsonb")
        @JdbcTypeCode(SqlTypes.JSON)
        private List<String> tags;

        @Column
        private String paymentMode;

        @Column
        private String transactionType;

        @Column
        private String merchantName;

        @Column
        private String source;

        @Column
        private Boolean userConfirmed;

        @Column
        private String rawText;

        @Column
        private String normalizedText;

        @Column
        private String status;

        @Column
        private Long expenseDate;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @CreatedDate
        @Column(nullable = false, updatable = false)
        private OffsetDateTime dateCreated;

        @LastModifiedDate
        @Column(nullable = false)
        private OffsetDateTime lastUpdated;

}
