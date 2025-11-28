package com.example.SpringMate.Config;

import com.example.SpringMate.Auth.Helper.SessionHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Service.UserDetailServiceImpl;
import com.example.SpringMate.Shared.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailServiceImpl userDetailService;
    private final SessionRepository sessionRepository;
    private final SessionHelper sessionHelper;
    private final SessionManagementHelper sessionManagementHelper;


    @Autowired
    public SecurityConfig(UserDetailServiceImpl userDetailService,
                          SessionManagementHelper sessionManagementHelper,
                          SessionHelper sessionHelper,
                          SessionRepository sessionRepository) {
        this.userDetailService = userDetailService;
        this.sessionRepository = sessionRepository;
        this.sessionManagementHelper = sessionManagementHelper;
        this.sessionHelper = sessionHelper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authFilter = new AuthenticationFilter(authenticationManager(http),sessionManagementHelper);
        authFilter.setFilterProcessesUrl(Urls.Auth.AUTH_BASE + Urls.Auth.LOGIN_WITH_PASS);

        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(Urls.PUBLIC_ENDPOINTS).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new SessionAuthenticationFilter(sessionRepository, sessionHelper),
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
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(Constants.Origin.DEV));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
