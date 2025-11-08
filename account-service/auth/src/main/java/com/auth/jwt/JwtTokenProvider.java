package com.auth.jwt;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.user.domain.vo.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;
	@Value("${jwt.expiration}")
	private long accessTokenExpiration;
	@Value("${jwt.refresh-expiration}")
	private long refreshTokenExpiration;

	private Key key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String createAccessToken(String userId, List<UserRole> roles, String provider) {

		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(accessTokenExpiration);

		return Jwts.builder()
			.subject(userId)
			.claim("userId", userId)
			.claim("provider", provider)
			.claim("roles", roles)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(key)
			.compact();
	}

	public String createRefreshToken(String userId, List<UserRole> roles, String provider) {

		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(refreshTokenExpiration);

		return Jwts.builder()
			.subject(userId)
			.claim("userId", userId)
			.claim("provider", provider)
			.claim("roles", roles)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(key)
			.compact();
	}

	public String getUserIdFromToken(String token) {
		Claims claims = parseClaims(token);
		return claims.get("userId").toString();
	}

	public String getProviderFromToken(String token) {
		Claims claims = parseClaims(token);
		return claims.get("provider").toString();
	}

	public List<UserRole> getRolesFromToken(String token) {
		Claims claims = parseClaims(token);
		List<String> roles = claims.get("roles", List.class);
		return roles.stream().map(UserRole::valueOf).toList();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (SignatureException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public Instant getExpirationFromToken(String token) {
		Claims claims = parseClaims(token);
		Date expiration = claims.getExpiration();
		return expiration.toInstant();
	}
}
