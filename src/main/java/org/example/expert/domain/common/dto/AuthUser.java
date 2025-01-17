package org.example.expert.domain.common.dto;

import java.util.Collection;
import java.util.List;

import lombok.Getter;

import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String nickname;
    private final UserRole userRole;

    public AuthUser(Long id, String email, String nickname, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.userRole = userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() ->"ROLE_" + this.userRole.name());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() { // subject에 해당하는 부분이 들어가는거 같긴 한데.. id는 getId로 조회하면 됨.. 그래도 일단 해두자
        return this.id.toString();
    }
}
