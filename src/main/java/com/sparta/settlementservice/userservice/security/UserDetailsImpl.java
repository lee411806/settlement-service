package com.sparta.settlementservice.userservice.security;



import com.sparta.settlementservice.userservice.entity.User;
import com.sparta.settlementservice.userservice.entity.UserRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

//spring security lib를 가져오면 UserDetails interface를 사용할 수 있고
// 거기서 오버라이딩하여 메서드들 가져옴
public class UserDetailsImpl implements UserDetails {

    private final User user;

    //UserDetailsServiceImpl에서 받아온 유저 생성자로 필드에 넣어줌
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    //유저도 필요할 때 가져올 수 있도록 getUser 만들어줌
    public User getUser() {
        return user;
    }

    //패스워드
    @Override
    public String getPassword() {
        return user.getPassword();
    }


    //유저네임도 가져올 수 있음
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    //나중에 권한 설정할 때 접근 불가 페이지 만들 때 사용가능
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRoleEnum role = user.getRole();
        String authority = role.getAuthority();

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }
    // 계정이 만료 되지는 않았는지
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨 있지는 않은지
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 계정의 인증 정보가 만료되지는 않았는지
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화 상태인지
    @Override
    public boolean isEnabled() {
        return true;
    }
}