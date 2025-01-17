package com.syam.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("sky")
                .password(passwordEncoder.encode("sky18"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                    .requestMatchers( "/home", "/contact", "/search", "/list").permitAll() // Publicly accessible pages
                // Protect saveStudent endpoint
                .requestMatchers("/students/saveStudent").authenticated()
                // Any other requests remain accessible
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/students/saveStudent", true) // Redirect to /welcome after successful login
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home") // Redirect to /home after logout
                .permitAll()
            );

        return http.build();
    }
}
