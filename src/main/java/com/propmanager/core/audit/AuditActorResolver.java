package com.propmanager.core.audit;

import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditActorResolver {

    private final StaffMemberRepository staffMemberRepository;
    private static final UUID SYSTEM_ACTOR_ID = new UUID(0L, 0L);

    public UUID resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM_ACTOR_ID;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            return staffMemberRepository.findByEmail(email)
                    .map(StaffMember::getId)
                    .orElse(SYSTEM_ACTOR_ID);
        }

        return SYSTEM_ACTOR_ID;
    }
}
