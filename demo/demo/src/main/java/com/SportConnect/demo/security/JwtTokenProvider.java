package com.SportConnect.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:SportConnectSecretKeyThatMustBeVeryLongAndSecure1234567890}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds:604800000}")
    private Long jwtExpirationDate;


    private Key key(){

        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication){
        String email = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    public String getEmailFromToken(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return true;

        }catch (MalformedJwtException ex){
            System.out.println("Token JWT invalid");
        }catch (ExpiredJwtException ex){
            System.out.println("Token JWT expirat");
        }catch (UnsupportedJwtException ex){
            System.out.println("Token JWT nesuportat");
        }catch (IllegalArgumentException ex){
            System.out.println("Token JWT gol");
        }
        return false;
    }
}
