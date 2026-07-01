package com.propmanager.core.security;

import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.entity.StaffStatus;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Security UserDetailsService implementation backed by the
 * StaffMember JPA entity.
 *
 * This class replaces the temporary stub bean in SecurityConfig and
 * closes the authentication loop:
 *
 *   JwtAuthenticationFilter extracts email from JWT
 *     → calls loadUserByUsername(email)
 *       → loads StaffMember from database
 *         → returns UserDetails with ROLE_{role} authority
 *           → JwtService.isTokenValid() validates token against UserDetails
 *             → SecurityContextHolder populated
 *               → TenantContext.setCurrentTenantId(orgId) called
 *
 * Cross-tenant query:
 *   findByEmail() queries across all organizations — it has no tenant_id
 *   filter. This is intentional and correct: at the moment this method
 *   is called, no tenant context has yet been established (the JWT has
 *   not been fully processed). The TenantFilterAspect is guarded against
 *   this scenario — it skips filter activation when TenantContext is null.
 *
 * Account status enforcement:
 *   If the staff member's status is DEACTIVATED, this method throws
 *   UsernameNotFoundException, which causes Spring Security to reject
 *   the authentication and return 401 Unauthorized. This ensures that
 *   deactivating a staff account takes effect on the next request even
 *   though existing JWT tokens have not expired yet.
 *
 * Authority mapping:
 *   StaffRole enum values are prefixed with "ROLE_" to satisfy Spring
 *   Security's convention for hasRole() / @PreAuthorize("hasRole(...)").
 *   Example: StaffRole.ADMINISTRATOR → GrantedAuthority("ROLE_ADMINISTRATOR")
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffMemberDetailsService implements UserDetailsService {

    private final StaffMemberRepository staffMemberRepository;

    /**
     * Loads a StaffMember by email address for JWT authentication.
     *
     * @param email the email extracted from the JWT subject claim
     * @return a populated UserDetails object for the authenticated principal
     * @throws UsernameNotFoundException if no active staff member exists
     *                                   with the supplied email address
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {

        log.debug("StaffMemberDetailsService: loading user by email [{}]", email);

        StaffMember staffMember = staffMemberRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("StaffMemberDetailsService: no staff member found for email [{}]", email);
                return new UsernameNotFoundException(
                    "No staff member found with email: " + email
                );
            });

        if (staffMember.getStatus() == StaffStatus.DEACTIVATED) {
            log.warn("StaffMemberDetailsService: login attempt by DEACTIVATED account [{}]", email);
            throw new UsernameNotFoundException(
                "Staff member account is deactivated: " + email
            );
        }

        String roleAuthority = "ROLE_" + staffMember.getRole().name();

        log.debug("StaffMemberDetailsService: authenticated [{}] with authority [{}]",
            email, roleAuthority);

        return User.builder()
            .username(staffMember.getEmail())
            .password(staffMember.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority(roleAuthority)))
            .build();
    }
}