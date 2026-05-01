package com.holytrinity.expenso.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserContext userContext;

    @Before("execution(* com.holytrinity.expenso..*Repository.*(..))")
    public void enableTenantFilter() {
        String userId = userContext.getCurrentUserId();
        if (userId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("userId", userId);
        }
    }
}
