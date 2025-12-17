package autoservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                                // Публичные эндпоинты (доступны всем)
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()

                                // Мастера
                                .requestMatchers(HttpMethod.GET, "/api/masters/**").hasRole("MASTER")
                                .requestMatchers(HttpMethod.POST, "/api/masters").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/masters/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/masters/**").hasRole("ADMIN")

                                // Рабочие места
                                .requestMatchers(HttpMethod.GET, "/api/workshop-places/**").hasRole("MASTER")
                                .requestMatchers(HttpMethod.POST, "/api/workshop-places").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/workshop-places/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/workshop-places/**").hasRole("ADMIN")

                                // Заказы
                                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasRole("MASTER")
                                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("MANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasRole("MANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("master")
                .password(passwordEncoder.encode("password"))
                .roles("MASTER")
                .build();

        UserDetails manager = User.builder()
                .username("manager")
                .password(passwordEncoder.encode("manager123"))
                .roles("MANAGER", "MASTER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN", "MANAGER", "MASTER")
                .build();

        return new InMemoryUserDetailsManager(user, manager, admin);
    }
}