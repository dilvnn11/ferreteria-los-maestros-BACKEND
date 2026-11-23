package cl.ferreteria.losmaestros.security;

import cl.ferreteria.losmaestros.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutosExpiracion;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-minutes}") long minutosExpiracion) {
        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosExpiracion = minutosExpiracion;
    }

    public long getMinutosExpiracion() {
        return minutosExpiracion;
    }

    /** El token solo identifica al usuario; el rol se lee siempre de la base de datos. */
    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el id del usuario o lanza JwtException si el token no es válido o expiró. */
    public String extraerUsuarioId(String token) {
        return parseClaims(token).getSubject();
    }

    public Instant extraerExpiracion(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean esValido(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
