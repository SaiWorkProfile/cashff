package com.cashff.config;
import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import javax.crypto.SecretKey; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key; private final long expiryMinutes;
 public JwtService(@Value("${app.jwt-secret}") String secret,@Value("${app.jwt-expiration-minutes:480}") long expiryMinutes){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiryMinutes=expiryMinutes;}
 public String generate(String username,String role){Instant now=Instant.now();return Jwts.builder().subject(username).claim("role",role).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expiryMinutes*60))).signWith(key).compact();}
 public String username(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public boolean valid(String token){try{return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration().after(new Date());}catch(Exception ex){return false;}}
}
