package com.board.global.security.config;

import com.board.domain.token.service.TokenService;
import com.board.global.security.filter.JwtAuthenticationFilter;
import com.board.global.security.handler.JwtAuthenticationEntryPoint;
import com.board.global.security.handler.MemberLoginFailureHandler;
import com.board.global.security.handler.MemberLoginSuccessHandler;
import com.board.global.security.handler.MemberLogoutSuccessHandler;

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

    private static final String ROLE_MEMBER = "MEMBER";

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

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
                        .loginProcessingUrl("/api/members/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(memberLoginSuccessHandler())
                        .failureHandler(memberLoginFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll(false)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/members/nickname/*",
                                "/api/members/username/*",
                                "/api/posts/*").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/members/signup").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/write").hasRole(ROLE_MEMBER)
                        .requestMatchers(HttpMethod.PUT,
                                "/api/posts/*").hasRole(ROLE_MEMBER)
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/posts/*").hasRole(ROLE_MEMBER)
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
        return new MemberLoginSuccessHandler(objectMapper, tokenService);
    }

    @Bean
    public AuthenticationFailureHandler memberLoginFailureHandler() {
        return new MemberLoginFailureHandler(objectMapper);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new MemberLogoutSuccessHandler(tokenService);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenService, authenticationEntryPoint());
    }

}
