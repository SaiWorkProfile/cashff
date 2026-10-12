package com.cashff.controller;
import com.cashff.config.JwtService; import com.cashff.dto.LoginRequest; import com.cashff.repository.AdminUserRepository; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.util.Map;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final AdminUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthController(AdminUserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
 @PostMapping("/login") public Map<String,String> login(@Valid @RequestBody LoginRequest request){var user=users.findByUsername(request.username()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password"));if(!encoder.matches(request.password(),user.getPasswordHash()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");return Map.of("token",jwt.generate(user.getUsername(),user.getRole()),"username",user.getUsername(),"role",user.getRole());}
}
