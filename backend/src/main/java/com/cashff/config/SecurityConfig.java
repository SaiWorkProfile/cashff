package com.cashff.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.web.servlet.FilterRegistrationBean; import org.springframework.context.annotation.*; import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; import org.springframework.web.cors.CorsConfiguration; import org.springframework.web.cors.CorsConfigurationSource; import org.springframework.web.cors.UrlBasedCorsConfigurationSource; import java.util.List;
@Configuration
public class SecurityConfig {
 private final JwtAuthFilter jwtAuthFilter; public SecurityConfig(JwtAuthFilter jwtAuthFilter){this.jwtAuthFilter=jwtAuthFilter;}
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean FilterRegistrationBean<JwtAuthFilter> disableContainerRegistration(JwtAuthFilter filter){FilterRegistrationBean<JwtAuthFilter> registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
 @Bean SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
  http.csrf(c->c.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/api/auth/login","/api/public/**","/uploads/**","/error").permitAll().requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401,"Authentication required")))
   .addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origin}") String origin){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setAllowCredentials(false);UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
}
