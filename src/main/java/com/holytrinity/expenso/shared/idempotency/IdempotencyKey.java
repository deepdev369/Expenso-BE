package com.holytrinity.expenso.shared.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "idempotency_key", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "idempotency_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "userId", type = String.class)})
@Filter(name = "tenantFilter", condition = "user_id = :userId")
public class IdempotencyKey {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Integer responseStatus;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
