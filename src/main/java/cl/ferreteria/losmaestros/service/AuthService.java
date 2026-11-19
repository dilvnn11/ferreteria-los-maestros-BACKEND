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

        if (u.getIntentosFallidos() > 0) {
            u.setIntentosFallidos(0);
            usuarios.save(u);
        }
        return new AuthResponse(jwt.generar(u), UsuarioDto.de(u));
    }
}
