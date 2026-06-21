package com.minifullstack.ems.config;

import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.security.JwtAuthFilter;
import com.minifullstack.ems.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String EMPLOYEES_ALL      = "/api/employees/**";
    private static final String EMPLOYEES_SINGLE   = "/api/employees/*";
    private static final String STUDENTS_ALL        = "/api/students/**";
    private static final String STUDENTS_SINGLE     = "/api/students/*";
    private static final String USERS_ALL           = "/api/users/**";

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public — auth, files, dropdown data
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                         "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/api/enums/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/statuses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/countries/**").permitAll()

                        // Public — profile images (served as <img src> without auth headers)
                        .requestMatchers(HttpMethod.GET, EMPLOYEES_SINGLE + "/profile-image").permitAll()
                        .requestMatchers(HttpMethod.GET, STUDENTS_SINGLE  + "/profile-image").permitAll()

                        // Specific workflow endpoints — must come BEFORE the broad POST /employees/** rule
                        .requestMatchers(HttpMethod.GET,  "/api/employees/pending-review").hasAuthority(Role.ROLE_REVIEWER.name())
                        .requestMatchers(HttpMethod.GET,  "/api/employees/pending-approval").hasAuthority(Role.ROLE_APPROVER.name())
                        .requestMatchers(HttpMethod.POST, EMPLOYEES_SINGLE + "/review").hasAnyAuthority(Role.ROLE_REVIEWER.name(), Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST, EMPLOYEES_SINGLE + "/approve").hasAnyAuthority(Role.ROLE_APPROVER.name(), Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,  EMPLOYEES_SINGLE + "/assign-workflow").hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST, EMPLOYEES_SINGLE + "/submit").hasAuthority(Role.ROLE_ADMIN.name())

                        // Admin-only — create, update, delete employees (broad rules after specific ones)
                        .requestMatchers(HttpMethod.POST,   "/api/employees").hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST,   EMPLOYEES_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,    EMPLOYEES_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, EMPLOYEES_ALL).hasAuthority(Role.ROLE_ADMIN.name())

                        // Authenticated users — read employees
                        .requestMatchers(HttpMethod.GET, EMPLOYEES_ALL).authenticated()

                        // Admin-only — create, update, delete students
                        .requestMatchers(HttpMethod.POST,   "/api/students").hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST,   STUDENTS_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,    STUDENTS_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, STUDENTS_ALL).hasAuthority(Role.ROLE_ADMIN.name())

                        // Authenticated users — read students
                        .requestMatchers(HttpMethod.GET, STUDENTS_ALL).authenticated()

                        // Authenticated users — sim dataset (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/sim-employees/**").authenticated()

                        // Admin-only — user management
                        .requestMatchers(HttpMethod.GET,    USERS_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST,   "/api/users").hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,    USERS_ALL).hasAuthority(Role.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, USERS_ALL).hasAuthority(Role.ROLE_ADMIN.name())

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Allows the React / Angular frontend to call this API from localhost:3000 / 4200
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
