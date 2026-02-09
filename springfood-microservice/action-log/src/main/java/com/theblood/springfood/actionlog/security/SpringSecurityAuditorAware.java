package com.theblood.springfood.actionlog.security;


import com.theblood.common.dto.request.UserContextHolder;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String accountId = UserContextHolder.getContext().getUserIdString();
        return Optional.ofNullable(accountId);
    }
}
