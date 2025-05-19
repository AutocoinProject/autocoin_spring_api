package com.autocoin.user.oauth;

import com.autocoin.user.domain.Role;
import com.autocoin.user.domain.User;
import com.autocoin.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        // OAuth2 서비스 ID (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // OAuth2 로그인 진행 시 키가 되는 필드 값 (PK)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        
        // OAuth2UserService를 통해 가져온 데이터를 담을 클래스
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId, userNameAttributeName, oAuth2User.getAttributes());
        
        User user = saveOrUpdate(attributes);
        
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey(),
                user.getEmail(),
                user.getId(),
                user.getProvider());
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());
        
        User user;
        if (userOptional.isPresent()) {
            // 기존 사용자가 있으면 정보 업데이트
            user = userOptional.get();
            // 이미 다른 OAuth 제공자로 가입한 경우 추가 처리가 필요할 수 있음
            return user;
        } else {
            // 신규 사용자면 회원가입 처리
            user = User.builder()
                    .email(attributes.getEmail())
                    .username(attributes.getName())
                    .password("") // OAuth 사용자는 비밀번호가 없음
                    .role(Role.ROLE_USER)
                    .provider(attributes.getProvider())
                    .providerId(attributes.getAttributes().get(attributes.getNameAttributeKey()).toString())
                    .build();
            
            return userRepository.save(user);
        }
    }
}
