package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.AuthResponse;
import cl.ferreteria.losmaestros.dto.LoginRequest;
import cl.ferreteria.losmaestros.dto.UsuarioDto;
import cl.ferreteria.losmaestros.exception.ApiException;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.repository.UsuarioRepository;
import cl.ferreteria.losmaestros.security.JwtService;
import cl.ferreteria.losmaestros.util.Validaciones;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /**
     * Sin @Transactional a propósito: el contador de intentos debe guardarse
     * aunque después se lance la excepción de "credenciales incorrectas".
     */
    public AuthResponse login(LoginRequest req) {
        String correo = Validaciones.normalizarCorreo(req.correo());
        if (correo.isEmpty() || req.password() == null || req.password().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes completar correo y contraseña.");
        }

        Usuario u = usuarios.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas."));

        if (u.getIntentosFallidos() >= UsuarioDto.MAX_INTENTOS) {
            throw new ApiException(
                    HttpStatus.LOCKED,
                    "Esta cuenta está bloqueada por demasiados intentos fallidos. Contacta al administrador.");
        }

        if (!encoder.matches(req.password(), u.getPassword())) {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarios.save(u);
            int restantes = UsuarioDto.MAX_INTENTOS - u.getIntentosFallidos();
            throw new ApiException(
                    restantes <= 0 ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED,
                    restantes <= 0
                            ? "Cuenta bloqueada por 3 intentos fallidos consecutivos."
                            : "Credenciales incorrectas. Te quedan " + restantes + " intento(s).");
        }

        u.setIntentosFallidos(0);
        usuarios.save(u);
        return emitirToken(u);
    }

    /**
     * Renueva el JWT de un usuario autenticado (el token actual debe seguir siendo válido).
     * El rol se lee siempre de la BD, no del token.
     */
    public AuthResponse renovar(Usuario actual) {
        if (actual == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        Usuario u = usuarios.findById(actual.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado."));
        if (u.getIntentosFallidos() >= UsuarioDto.MAX_INTENTOS) {
            throw new ApiException(HttpStatus.LOCKED, "Esta cuenta está bloqueada.");
        }
        return emitirToken(u);
    }

    private AuthResponse emitirToken(Usuario u) {
        return new AuthResponse(jwt.generar(u), jwt.getMinutosExpiracion(), UsuarioDto.de(u));
    }
}
