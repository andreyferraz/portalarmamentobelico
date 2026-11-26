package com.armamentobelico.portalarmamentobelico.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.armamentobelico.portalarmamentobelico.model.Admin;
import com.armamentobelico.portalarmamentobelico.repository.AdminRepository;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/static/**",
                                "/uploads/**",
                                "/edicoes/**",
                                "/favicon.ico",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers("/admin", "/admin/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/?login=required")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/?loginError=true")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .addFilterAfter(new CsrfTokenInitializationFilter(), CsrfFilter.class);

        return http.build();
    }

    private static final class CsrfTokenInitializationFilter extends OncePerRequestFilter {
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String contextPath = request.getContextPath();
            String path = request.getRequestURI();
            if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
            return path.startsWith("/css/")
                    || path.startsWith("/js/")
                    || path.startsWith("/assets/")
                    || path.startsWith("/img/")
                    || path.startsWith("/static/")
                    || path.startsWith("/uploads/")
                    || path.startsWith("/webjars/")
                    || "/favicon.ico".equals(path);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public UserDetailsService userDetailsService(AdminRepository adminRepository) {
        return username -> adminRepository.findByUsername(username)
                .map(admin -> User.withUsername(admin.getUsername())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
    }

    @Bean
    public CommandLineRunner defaultAdminSeeder(AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.admin.username:admin}") String adminUsername,
            @Value("${app.security.admin.password:admin}") String adminPassword) {
        return args -> {
            if (adminRepository.findByUsername(adminUsername).isPresent()) {
                return;
            }

            Admin admin = new Admin();
            admin.setId(UUID.randomUUID());
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNew(true);
            adminRepository.save(admin);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}