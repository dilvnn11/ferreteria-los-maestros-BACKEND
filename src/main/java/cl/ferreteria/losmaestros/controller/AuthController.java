package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.AuthResponse;
import cl.ferreteria.losmaestros.dto.LoginRequest;
import cl.ferreteria.losmaestros.dto.UsuarioDto;
import cl.ferreteria.losmaestros.dto.UsuarioRequest;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.service.AuthService;
import cl.ferreteria.losmaestros.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** Registro público: siempre crea un cliente. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@RequestBody UsuarioRequest req) {
        return usuarios.registrar(req);
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal Usuario actual) {
        return UsuarioDto.de(actual);
    }

    /** Edición del perfil propio (no cambia rol ni contraseña). */
    @PutMapping("/perfil")
    public UsuarioDto actualizarPerfil(@RequestBody UsuarioRequest req, @AuthenticationPrincipal Usuario actual) {
        return usuarios.actualizarPerfil(req, actual);
    }
}
