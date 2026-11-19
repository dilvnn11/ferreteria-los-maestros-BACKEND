package cl.ferreteria.losmaestros.config;

import cl.ferreteria.losmaestros.dto.ProductoDto;
import cl.ferreteria.losmaestros.model.Categoria;
import cl.ferreteria.losmaestros.model.Producto;
import cl.ferreteria.losmaestros.model.Rol;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.repository.CategoriaRepository;
import cl.ferreteria.losmaestros.repository.ProductoRepository;
import cl.ferreteria.losmaestros.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Carga los mismos datos iniciales del frontend, solo si las tablas están vacías. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;

    public DataInitializer(
            CategoriaRepository categorias,
            ProductoRepository productos,
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            ObjectMapper mapper) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categorias.count() == 0) {
            categorias.saveAll(List.of(
                    new Categoria("cat-construccion", "Mat. Construcción"),
                    new Categoria("cat-pinturas", "Pinturas"),
                    new Categoria("cat-fierros", "Fierros")));
            log.info("Categorías iniciales creadas.");
        }

        if (productos.count() == 0) {
            Map<String, Categoria> porId =
                    categorias.findAll().stream().collect(Collectors.toMap(Categoria::getId, Function.identity()));
            try (InputStream in = new ClassPathResource("seed/productos.json").getInputStream()) {
                List<ProductoDto> lista = mapper.readValue(in, new TypeReference<List<ProductoDto>>() {});
                for (ProductoDto d : lista) {
                    Producto p = new Producto();
                    p.setId(d.id());
                    p.setNombre(d.nombre());
                    p.setCategoria(porId.get(d.categoriaId()));
                    p.setSubcategoria(d.subcategoria());
                    p.setMarca(d.marca());
                    p.setUnidad(d.unidad());
                    p.setPrecio(d.precio());
                    p.setStock(d.stock());
                    p.setStockMinimo(d.stockMinimo());
                    p.setDescripcion(d.descripcion());
                    p.setImagen(d.imagen());
                    productos.save(p);
                }
                log.info("{} productos iniciales creados.", lista.size());
            }
        }

        if (usuarios.count() == 0) {
            usuarios.save(usuario("u-admin", "Camila", "Fuentes", "1985-04-12", "admin@duoc.cl", "Admin123",
                    "Av. Francisco de Aguirre 100, La Serena", "Coquimbo", "Femenino", Rol.ADMINISTRADOR,
                    "2026-01-10T12:00:00Z"));
            usuarios.save(usuario("u-vendedor", "Marcelo", "Rojas", "1992-09-03", "vendedor@duoc.cl", "Vendedor123",
                    "Los Carrera 450, La Serena", "Coquimbo", "Masculino", Rol.VENDEDOR, "2026-01-10T12:05:00Z"));
            usuarios.save(usuario("u-cliente", "Pedro", "Soto", "1998-02-21", "cliente@duoc.cl", "Cliente123",
                    "Calle Prat 321, Coquimbo", "Coquimbo", "Masculino", Rol.CLIENTE, "2026-01-11T09:30:00Z"));
            log.info("Usuarios demo creados (admin@duoc.cl, vendedor@duoc.cl, cliente@duoc.cl).");
        }
    }

    private Usuario usuario(String id, String nombre, String apellido, String nacimiento, String correo,
                            String password, String direccion, String region, String genero, Rol rol, String registro) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setFechaNacimiento(LocalDate.parse(nacimiento));
        u.setCorreo(correo);
        u.setPassword(encoder.encode(password));
        u.setDireccion(direccion);
        u.setRegion(region);
        u.setGenero(genero);
        u.setRol(rol);
        u.setFechaRegistro(Instant.parse(registro));
        return u;
    }
}
