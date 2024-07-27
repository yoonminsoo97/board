package com.board.global.security.config;

import com.board.domain.token.service.TokenService;
import com.board.global.security.filter.JwtAuthenticationFilter;
import com.board.global.security.handler.JwtAuthenticationEntryPoint;
import com.board.global.security.handler.MemberLoginFailureHandler;
import com.board.global.security.handler.MemberLoginSuccessHandler;
import com.board.global.security.handler.MemberLogoutSuccessHandler;
import com.board.global.security.support.JwtManager;
import com.board.global.security.support.RequestURI;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String LOGIN_USERNAME_PARAM = "username";
    private static final String LOGIN_PASSWORD_PARAM = "password";
    private static final String ROLE_MEMBER = "MEMBER";

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final JwtManager jwtManager;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .formLogin(form -> form
                        .loginProcessingUrl(RequestURI.MEMBER_LOGIN.pattern())
                        .usernameParameter(LOGIN_USERNAME_PARAM)
                        .passwordParameter(LOGIN_PASSWORD_PARAM)
                        .successHandler(memberLoginSuccessHandler())
                        .failureHandler(memberLoginFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl(RequestURI.MEMBER_LOGOUT.pattern())
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll(false)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                RequestURI.REST_API_DOCS.pattern(),
                                RequestURI.MEMBER_NICKNAME_EXISTS.pattern(),
                                RequestURI.MEMBER_USERNAME_EXISTS.pattern(),
                                RequestURI.POST_DETAIL.pattern(),
                                RequestURI.POST_LIST.pattern(),
                                RequestURI.POST_SERACH_LIST.pattern(),
                                RequestURI.COMMENT_LIST.pattern()).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                RequestURI.MEMBER_SIGNUP.pattern(),
                                RequestURI.MEMBER_LOGIN.pattern(),
                                RequestURI.REISSUE_ACCESS_TOKEN.pattern()).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                RequestURI.MEMBER_PROFILE.pattern(),
                                RequestURI.MEMBER_POST_LIST.pattern(),
                                RequestURI.MEMBER_COMMENT_LIST.pattern()).hasRole(ROLE_MEMBER)
                        .requestMatchers(HttpMethod.POST,
                                RequestURI.POST_WRITE.pattern(),
                                RequestURI.COMMENT_WRITE.pattern(),
                                RequestURI.COMMENT_REPLRY_WRITE.pattern(),
                                RequestURI.MEMBER_LOGOUT.pattern()).hasRole(ROLE_MEMBER)
                        .requestMatchers(HttpMethod.PUT,
                                RequestURI.MEMBER_NICKNAME_CHANGE.pattern(),
                                RequestURI.MEMBER_PASSWORD_CHANGE.pattern(),
                                RequestURI.POST_MODIFY.pattern(),
                                RequestURI.COMMENT_MODIFY.pattern()).hasRole(ROLE_MEMBER)
                        .requestMatchers(HttpMethod.DELETE,
                                RequestURI.POST_DELETE.pattern(),
                                RequestURI.COMMENT_DELETE.pattern()).hasRole(ROLE_MEMBER)
                        .anyRequest().denyAll()
                );
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler memberLoginSuccessHandler() {
        return new MemberLoginSuccessHandler(objectMapper, tokenService, jwtManager);
    }

    @Bean
    public AuthenticationFailureHandler memberLoginFailureHandler() {
        return new MemberLoginFailureHandler(objectMapper);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new MemberLogoutSuccessHandler(objectMapper, tokenService);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(authenticationEntryPoint(), jwtManager);
    }

}
