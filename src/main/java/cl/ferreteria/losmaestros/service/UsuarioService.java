package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.UsuarioDto;
import cl.ferreteria.losmaestros.dto.UsuarioRequest;
import cl.ferreteria.losmaestros.exception.ApiException;
import cl.ferreteria.losmaestros.exception.ValidacionException;
import cl.ferreteria.losmaestros.model.Rol;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.repository.CompraRepository;
import cl.ferreteria.losmaestros.repository.UsuarioRepository;
import cl.ferreteria.losmaestros.util.Validaciones;
import cl.ferreteria.losmaestros.util.Validaciones.ModoUsuario;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final String CORREO_REPETIDO = "Ya existe una cuenta con ese correo electrónico.";

    private final UsuarioRepository usuarios;
    private final CompraRepository compras;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, CompraRepository compras, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.compras = compras;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }

    /** Registro público: siempre crea una cuenta de cliente. */
    @Transactional
    public UsuarioDto registrar(UsuarioRequest r) {
        Validaciones.usuario(r, ModoUsuario.REGISTRO);
        return UsuarioDto.de(usuarios.save(construir(r, Rol.CLIENTE)));
    }

    /** Administrador: cualquier rol. Vendedor: solo clientes. */
    @Transactional
    public UsuarioDto crear(UsuarioRequest r, Usuario actual) {
        Validaciones.usuario(r, ModoUsuario.CREAR);
        boolean permitido = actual.getRol() == Rol.ADMINISTRADOR
                || (actual.getRol() == Rol.VENDEDOR && r.rol() == Rol.CLIENTE);
        if (!permitido) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permisos para crear usuarios con ese rol.");
        }
        return UsuarioDto.de(usuarios.save(construir(r, r.rol())));
    }

    @Transactional
    public UsuarioDto actualizar(String id, UsuarioRequest r, Usuario actual) {
        if (actual.getRol() != Rol.ADMINISTRADOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar usuarios.");
        }
        Validaciones.usuario(r, ModoUsuario.EDITAR);
        Usuario u = buscar(id);
        verificarCorreoLibre(r.correo(), id);
        if (id.equals(actual.getId()) && r.rol() != u.getRol()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
        }
        copiarDatos(u, r);
        u.setRol(r.rol());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPassword(encoder.encode(r.password()));
        }
        return UsuarioDto.de(usuarios.save(u));
    }

    /** Edición del perfil propio: no toca el rol ni la contraseña. */
    @Transactional
    public UsuarioDto actualizarPerfil(UsuarioRequest r, Usuario actual) {
        Validaciones.usuario(r, ModoUsuario.PERFIL);
        Usuario u = buscar(actual.getId());
        verificarCorreoLibre(r.correo(), u.getId());
        copiarDatos(u, r);
        return UsuarioDto.de(usuarios.save(u));
    }

    @Transactional
    public void eliminar(String id, Usuario actual) {
        if (actual.getRol() != Rol.ADMINISTRADOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar usuarios.");
        }
        if (id.equals(actual.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        }
        Usuario u = buscar(id);
        compras.desvincularUsuario(u.getId());
        usuarios.delete(u);
    }

    @Transactional
    public UsuarioDto desbloquear(String id) {
        Usuario u = buscar(id);
        u.setIntentosFallidos(0);
        return UsuarioDto.de(usuarios.save(u));
    }

    // ------------------------------------------------------------------ helpers

    private Usuario buscar(String id) {
        return usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El usuario ya no existe."));
    }

    private void verificarCorreoLibre(String correo, String idExcluido) {
        String limpio = Validaciones.normalizarCorreo(correo);
        boolean enUso = idExcluido == null
                ? usuarios.existsByCorreo(limpio)
                : usuarios.existsByCorreoAndIdNot(limpio, idExcluido);
        if (enUso) throw new ValidacionException(Map.of("correo", CORREO_REPETIDO));
    }

    private Usuario construir(UsuarioRequest r, Rol rol) {
        verificarCorreoLibre(r.correo(), null);
        Usuario u = new Usuario();
        u.setId("u-" + UUID.randomUUID().toString().substring(0, 8));
        copiarDatos(u, r);
        u.setPassword(encoder.encode(r.password()));
        u.setRol(rol);
        u.setFechaRegistro(Instant.now());
        return u;
    }

    private void copiarDatos(Usuario u, UsuarioRequest r) {
        u.setNombre(r.nombre().trim());
        u.setApellido(r.apellido().trim());
        u.setFechaNacimiento(LocalDate.parse(r.fechaNacimiento().trim()));
        u.setCorreo(Validaciones.normalizarCorreo(r.correo()));
        u.setDireccion(r.direccion().trim());
        u.setRegion(r.region());
        u.setGenero(r.genero());
    }
}
