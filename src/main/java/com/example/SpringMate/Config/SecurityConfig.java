package com.example.SpringMate.Config;

import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Filter.AuthenticationFilter;
import com.example.SpringMate.Auth.Filter.SessionAuthenticationFilter;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Shared.Roles;
import com.example.SpringMate.User.Service.UserDetailServiceImpl;
import com.example.SpringMate.Shared.Urls;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailServiceImpl userDetailService;
    private final SessionManagementHelper sessionManagementHelper;
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCacheService authCacheService;
    private final AuthHelper authHelper;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1)
    SecurityFilterChain prometheusChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(Urls.Actuator.PROMETHEUS)
                .authenticationManager(prometheusAuthManager())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole(Roles.PROMETHEUS)
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }



    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authFilter = new AuthenticationFilter
                (appAuthenticationManager(http),
                        sessionManagementHelper,
                        jwtTokenProvider,
                        authCacheService,
                        authHelper,
                        objectMapper);
        authFilter.setFilterProcessesUrl(Urls.Auth.BASE + Urls.Auth.LOGIN_WITH_PASS);

        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(Urls.Security.PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers(Urls.Actuator.BASE + "/**").hasRole(Roles.SUPER_ADMIN)
                                .anyRequest().authenticated()
                )
                .addFilterBefore(sessionAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .anonymous(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager appAuthenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;

    }

    @Bean
    public AuthenticationManager prometheusAuthManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());

        ProviderManager manager = new ProviderManager(provider);
        manager.setEraseCredentialsAfterAuthentication(false);
        return manager;
    }



    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                    ROLE_SUPER_ADMIN > ROLE_ADMIN
                    ROLE_ADMIN > ROLE_USER
                """);

    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        AppProperties.Cors corsConfig = appProperties.getCors();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsConfig.getAllowedOrigins());
        config.setAllowedMethods(corsConfig.getAllowedMethods());
        config.setAllowedHeaders(corsConfig.getAllowedHeaders());
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        config.setMaxAge(corsConfig.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
