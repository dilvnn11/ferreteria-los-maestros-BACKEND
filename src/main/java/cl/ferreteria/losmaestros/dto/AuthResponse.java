package cl.ferreteria.losmaestros.dto;

/** Respuesta de login y de renovación de token. */
public record AuthResponse(String token, long expiresInMinutes, UsuarioDto usuario) {}
