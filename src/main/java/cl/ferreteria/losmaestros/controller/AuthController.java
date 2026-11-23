package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.AuthResponse;
import cl.ferreteria.losmaestros.dto.LoginRequest;
import cl.ferreteria.losmaestros.dto.UsuarioDto;
import cl.ferreteria.losmaestros.dto.UsuarioRequest;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.service.AuthService;
import cl.ferreteria.losmaestros.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro, perfil y renovación de token JWT")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales contra la BD y devuelve JWT + datos del usuario")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** Registro público: siempre crea un cliente. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registro público", description = "Crea una cuenta con rol cliente")
    public UsuarioDto registro(@RequestBody UsuarioRequest req) {
        return usuarios.registrar(req);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Usuario actual", description = "Devuelve el usuario autenticado (útil al recargar la página)")
    public UsuarioDto me(@AuthenticationPrincipal Usuario actual) {
        return UsuarioDto.de(actual);
    }

    /** Edición del perfil propio (no cambia rol ni contraseña). */
    @PutMapping("/perfil")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Actualizar perfil propio")
    public UsuarioDto actualizarPerfil(@RequestBody UsuarioRequest req, @AuthenticationPrincipal Usuario actual) {
        return usuarios.actualizarPerfil(req, actual);
    }

    /**
     * Renueva el token JWT mientras el actual siga siendo válido.
     * El frontend puede llamarlo antes de que expire (p. ej. a la mitad de la vigencia).
     */
    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Renovar token JWT", description = "Emite un nuevo token con la misma vigencia. Requiere token válido.")
    public AuthResponse refresh(@AuthenticationPrincipal Usuario actual) {
        return auth.renovar(actual);
    }
}
