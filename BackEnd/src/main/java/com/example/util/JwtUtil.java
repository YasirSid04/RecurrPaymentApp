package com.example.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    private String SECRET_KEY = "mySheepIsGoodButImVeryGoodHahaIJustWantToMakeItLong";

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);  //we are getting Username ,from claims by passing token,in string format.
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){ //user info    T :return can be any type
        final Claims claims = extractAllClaims(token);//getting user info
        return claimsResolver.apply(claims);//input type claim   return type can be anything
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
                                //email, yasir@gmail.com
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder().setClaims(claims).setSubject(subject)   //Claims holds only non confedential info of the user
                .setIssuedAt(new Date(System.currentTimeMillis()))   //last token created time
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*10))  //expiry of Token
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact(); //SECRET KEY encoded with Hashing algo HS256
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
