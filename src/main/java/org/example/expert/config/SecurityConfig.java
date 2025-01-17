package org.example.expert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final JwtUtil jwtUtil;

	@Bean
	public JwtFilter jwtFilter() {
		return new JwtFilter(jwtUtil);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.addFilterBefore(jwtFilter(), SecurityContextHolderAwareRequestFilter.class) // jwt 필터 다음 적용하겟다
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**").permitAll() // /auth로 시작하는건 다 허용
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/users/**").hasRole("USER")
				.anyRequest().authenticated()
			)
			.build();
	}
}
