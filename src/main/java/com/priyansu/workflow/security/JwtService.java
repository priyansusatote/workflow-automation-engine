package com.priyansu.workflow.security;


import com.priyansu.workflow.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service

public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpiration;

    // SecretKey [Convert Base64 secret string into a SecretKey used for signing/verifying JWT tokens
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Generate Token
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) //jti = JWT ID [A unique identifier for each token][jti token id -unique per token) {use for logout , token blacklist , better security etc..)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now)) //Current system time (now)
                .expiration(Date.from(now.plusMillis(jwtExpiration)))
                .signWith(getSignKey())
                .compact();
    }


    //Validate & extract userDetails [If token is:expired,tampered,invalid :> It will THROW: exception
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token) //expiration auto checked by JJWT
                .getPayload();
    }

    //extract [ "<T>" Genetic Type Parameter] means <T> = I don't know the Type yet-it will be decided later
    public <T> T extractClaim(String token, Function<Claims, T> resolver) { //Function<Claims, T> = input → output(T=unknown Type)

        return resolver.apply(extractAllClaims(token)); //gives specific data from claims
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); //claims -> claims.getSubject()
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }
}
