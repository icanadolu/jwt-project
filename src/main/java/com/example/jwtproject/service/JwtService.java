package com.example.jwtproject.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.expiration}")
	private long jwtExpiration;

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles",
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
		 return Jwts.builder()
		            .setClaims(claims)
		            .setSubject(userDetails.getUsername())
		            .setIssuedAt(new Date(System.currentTimeMillis()))
		            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
		            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
		            .compact();
	}
	
	
	  public boolean isTokenValid(String token, UserDetails userDetails) { final
	  String username = extractUsername(token);
	  return username.equals(userDetails.getUsername())&&!isTokenExpired(token);
	  }
	 
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	private boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(new Date());
	}
	
	private <T> T extractClaim(String token,Function<Claims,T> claimResolver) {
		final Claims claims = extractAllClaim(token);
		return claimResolver.apply(claims);
	}
	
	private Claims extractAllClaim(String token) {
		  return Jwts.parser()                // YENİ: parserBuilder() yerine
		        .verifyWith(getSignInKey())         // YENİ: setSigningKey() yerine
		        .build()
		        .parseSignedClaims(token)           // YENİ: parseClaimsJws() yerine
		        .getPayload();   
	}
	
	private SecretKey getSignInKey() {
		byte[] keyBytes	= Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
