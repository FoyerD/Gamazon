package Application;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;


import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    private final long expirationTime = 1000 * 60 * 60 * 24; // 24 hours
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);


    /***
     * Generates a JWT token for the given user ID.
     * The token will contain the user ID as the subject and will be signed with the secret key.
     * @param id The ID of the user for whom the token is generated.
     * @return The generated JWT token as a string.
     */
    public String generateToken(String id) {
        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }


    /***
     * Validates the given JWT token.
     * @param token The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /***
     * Extracts the user ID from the given JWT token.
     * @param token The JWT token from which to extract the user ID.
     * @return The user ID extracted from the token.
     */
    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /***
     * Extracts the expiration date from the given JWT token.
     * @param token The JWT token from which to extract the expiration date.
     * @return The expiration date extracted from the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }



    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
