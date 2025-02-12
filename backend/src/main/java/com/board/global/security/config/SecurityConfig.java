package com.board.global.security.config;

import com.board.domain.token.service.TokenService;
import com.board.global.security.filter.LoginAuthenticationFilter;
import com.board.global.security.filter.TokenAuthenticationFilter;
import com.board.global.security.handler.MemberLoginFailureHandler;
import com.board.global.security.handler.MemberLoginSuccessHandler;
import com.board.global.security.handler.MemberLogoutSuccessHandler;
import com.board.global.security.handler.TokenAuthenticationEntryPoint;
import com.board.global.security.service.MemberUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberUserDetailsService memberUserDetailsService;
    private final TokenService tokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(memberLogoutSuccessHandler())
                        .permitAll(false)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(tokenAuthenticationFilter(), LogoutFilter.class)
                .addFilterAt(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts/*",
                                "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/members/signup").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/write").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/posts/*").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/posts/*").hasRole("MEMBER")
                        .anyRequest().denyAll()
                );
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter() {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
        loginAuthenticationFilter.setAuthenticationSuccessHandler(memberLoginSuccessHandler());
        loginAuthenticationFilter.setAuthenticationFailureHandler(memberLoginFailureHandler());
        loginAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return loginAuthenticationFilter;
    }

    @Bean
    public AuthenticationSuccessHandler memberLoginSuccessHandler() {
        return new MemberLoginSuccessHandler(tokenService);
    }

    @Bean
    public AuthenticationFailureHandler memberLoginFailureHandler() {
        return new MemberLoginFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler memberLogoutSuccessHandler() {
        return new MemberLogoutSuccessHandler(tokenService);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenService, authenticationEntryPoint());
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new TokenAuthenticationEntryPoint();
    }

}
