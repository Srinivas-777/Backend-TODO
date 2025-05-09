package com.example.ToDo.JWT_PACKAGE;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;

@Service
public class JwtTokenProvider {

    private final SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    public String generateToken(Authentication auth) {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 86400000)) // 1 day expiration
                .claim("email", auth.getName())
                .signWith(key)
                .compact();
    }

    public String getEmailFromJwtToken(String jwt) {
        jwt = jwt.substring(7); // Remove "Bearer " prefix

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt)
                .getBody();

        return String.valueOf(claims.get("email"));
    }

    public boolean validateToken(String jwt) {
        try {
            // Parse the token to check its validity
            Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
            return true; // Token is valid
        } catch (Exception e) {
            // Log the error if needed
            return false; // Token is invalid
        }
    }

    public String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        StringBuilder auths = new StringBuilder();
        for (GrantedAuthority authority : collection) {
            auths.append(authority.getAuthority()).append(",");
        }
        return auths.toString();
    }
}