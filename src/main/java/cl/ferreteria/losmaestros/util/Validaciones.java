package cl.ferreteria.losmaestros.util;

import cl.ferreteria.losmaestros.dto.ContactoRequest;
import cl.ferreteria.losmaestros.dto.ProductoRequest;
import cl.ferreteria.losmaestros.dto.UsuarioRequest;
import cl.ferreteria.losmaestros.exception.ValidacionException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Mismas reglas y mensajes que frontend/src/utils/validaciones.ts,
 * porque el backend nunca debe confiar en que el navegador ya validó.
 */
public final class Validaciones {

    public enum ModoUsuario { REGISTRO, CREAR, EDITAR, PERFIL }

    public static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static final String DOMINIO_PERMITIDO = "@duoc.cl";
    public static final int EDAD_MINIMA = 14;
    private static final Pattern CODIGO_PRODUCTO = Pattern.compile("^[A-Za-z0-9-]{3,12}$");

    public static final Set<String> GENEROS = Set.of("Masculino", "Femenino", "Otro", "Prefiero no decir");

    public static final Set<String> REGIONES = Set.of(
            "Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama", "Coquimbo", "Valparaíso",
            "Metropolitana", "O'Higgins", "Maule", "Ñuble", "Biobío", "La Araucanía", "Los Ríos",
            "Los Lagos", "Aysén", "Magallanes");

    private Validaciones() {}

    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Mínimo 8 caracteres, 1 mayúscula y 1 número. Devuelve el mensaje de error o null. */
    public static String validarPassword(String pass) {
        if (pass.length() < 8) return "La contraseña debe tener al menos 8 caracteres.";
        if (pass.chars().noneMatch(Character::isUpperCase)) return "La contraseña debe contener al menos una mayúscula.";
        if (pass.chars().noneMatch(c -> c >= '0' && c <= '9')) return "La contraseña debe contener al menos un número.";
        if (pass.length() > 72) return "La contraseña no puede superar los 72 caracteres.";
        return null;
    }

    public static String validarCorreo(String correo) {
        String limpio = normalizarCorreo(correo);
        if (limpio.isEmpty()) return "El correo electrónico es obligatorio.";
        if (!EMAIL.matcher(limpio).matches()) return "El formato del correo no es válido.";
        if (!limpio.endsWith(DOMINIO_PERMITIDO)) return "Solo se permiten correos del dominio " + DOMINIO_PERMITIDO + ".";
        if (limpio.length() > 120) return "El correo no puede superar los 120 caracteres.";
        return null;
    }

    public static void lanzarSiHayErrores(Map<String, String> errores) {
        if (!errores.isEmpty()) throw new ValidacionException(errores);
    }

    // ---------------------------------------------------------------- usuarios

    public static void usuario(UsuarioRequest r, ModoUsuario modo) {
        Map<String, String> e = new LinkedHashMap<>();

        if (vacio(r.nombre())) e.put("nombre", "El nombre es obligatorio.");
        else if (r.nombre().trim().length() > 60) e.put("nombre", "El nombre no puede superar los 60 caracteres.");

        if (vacio(r.apellido())) e.put("apellido", "El apellido es obligatorio.");
        else if (r.apellido().trim().length() > 60) e.put("apellido", "El apellido no puede superar los 60 caracteres.");

        if (vacio(r.fechaNacimiento())) {
            e.put("fechaNacimiento", "La fecha de nacimiento es obligatoria.");
        } else {
            try {
                LocalDate nacimiento = LocalDate.parse(r.fechaNacimiento().trim());
                LocalDate hoy = LocalDate.now();
                if (nacimiento.isAfter(hoy)) {
                    e.put("fechaNacimiento", "Ingresa una fecha de nacimiento válida.");
                } else if (Period.between(nacimiento, hoy).getYears() < EDAD_MINIMA) {
                    e.put("fechaNacimiento", "Debes tener al menos " + EDAD_MINIMA + " años.");
                }
            } catch (DateTimeParseException ex) {
                e.put("fechaNacimiento", "Ingresa una fecha de nacimiento válida.");
            }
        }

        String errorCorreo = validarCorreo(r.correo());
        if (errorCorreo != null) e.put("correo", errorCorreo);

        // Contraseña: obligatoria al registrar/crear; opcional al editar; no aplica en el perfil
        if (modo != ModoUsuario.PERFIL) {
            boolean obligatoria = modo != ModoUsuario.EDITAR;
            if (vacio(r.password())) {
                if (obligatoria) e.put("password", "La contraseña es obligatoria.");
            } else {
                String errorPass = validarPassword(r.password());
                if (errorPass != null) e.put("password", errorPass);
            }
        }

        if (vacio(r.direccion())) e.put("direccion", "La dirección es obligatoria.");
        else if (r.direccion().trim().length() > 200) e.put("direccion", "La dirección no puede superar los 200 caracteres.");

        if (vacio(r.region())) e.put("region", "Debes seleccionar una región.");
        else if (!REGIONES.contains(r.region())) e.put("region", "La región seleccionada no es válida.");

        if (vacio(r.genero())) e.put("genero", "Debes seleccionar un género.");
        else if (!GENEROS.contains(r.genero())) e.put("genero", "El género seleccionado no es válido.");

        if ((modo == ModoUsuario.CREAR || modo == ModoUsuario.EDITAR) && r.rol() == null) {
            e.put("rol", "Debes seleccionar un rol válido.");
        }

        lanzarSiHayErrores(e);
    }

    // ---------------------------------------------------------------- productos

    public static void producto(ProductoRequest r, boolean crear) {
        Map<String, String> e = new LinkedHashMap<>();

        if (crear) {
            String codigo = r.id() == null ? "" : r.id().trim();
            if (codigo.isEmpty()) e.put("id", "El código es obligatorio.");
            else if (!CODIGO_PRODUCTO.matcher(codigo).matches())
                e.put("id", "Usa de 3 a 12 letras, números o guiones (sin espacios).");
        }

        if (vacio(r.nombre()) || r.nombre().trim().length() < 3)
            e.put("nombre", "El nombre debe tener al menos 3 caracteres.");
        else if (r.nombre().trim().length() > 150) e.put("nombre", "El nombre no puede superar los 150 caracteres.");

        if (vacio(r.categoriaId())) e.put("categoriaId", "Debes seleccionar una categoría.");

        if (vacio(r.subcategoria())) e.put("subcategoria", "La subcategoría es obligatoria.");
        else if (r.subcategoria().trim().length() > 60) e.put("subcategoria", "Máximo 60 caracteres.");

        if (vacio(r.marca())) e.put("marca", "La marca es obligatoria.");
        else if (r.marca().trim().length() > 60) e.put("marca", "Máximo 60 caracteres.");

        if (vacio(r.unidad())) e.put("unidad", "La unidad es obligatoria.");
        else if (r.unidad().trim().length() > 40) e.put("unidad", "Máximo 40 caracteres.");

        if (r.precio() == null || r.precio() <= 0) e.put("precio", "El precio debe ser un entero mayor a 0.");
        if (r.stock() == null || r.stock() < 0) e.put("stock", "El stock debe ser un entero igual o mayor a 0.");
        if (r.stockMinimo() == null || r.stockMinimo() < 0)
            e.put("stockMinimo", "El stock mínimo debe ser un entero igual o mayor a 0.");

        if (vacio(r.descripcion()) || r.descripcion().trim().length() < 10)
            e.put("descripcion", "La descripción debe tener al menos 10 caracteres.");
        else if (r.descripcion().trim().length() > 500) e.put("descripcion", "La descripción no puede superar los 500 caracteres.");

        if (r.imagen() != null && r.imagen().trim().length() > 255)
            e.put("imagen", "La imagen no puede superar los 255 caracteres.");

        lanzarSiHayErrores(e);
    }

    // ---------------------------------------------------------------- categorías y contacto

    /** Devuelve el mensaje de error o null (la unicidad se verifica en el servicio). */
    public static String nombreCategoria(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.length() < 3) return "El nombre debe tener al menos 3 caracteres.";
        if (limpio.length() > 40) return "El nombre no puede superar los 40 caracteres.";
        return null;
    }

    public static void contacto(ContactoRequest r) {
        Map<String, String> e = new LinkedHashMap<>();
        if (vacio(r.nombre())) e.put("nombre", "El nombre es obligatorio.");
        else if (r.nombre().trim().length() > 100) e.put("nombre", "El nombre no puede superar los 100 caracteres.");

        if (vacio(r.correo())) e.put("correo", "El correo es obligatorio.");
        else if (!EMAIL.matcher(r.correo().trim()).matches()) e.put("correo", "Ingresa un correo electrónico válido.");
        else if (r.correo().trim().length() > 120) e.put("correo", "El correo no puede superar los 120 caracteres.");

        if (vacio(r.mensaje())) e.put("mensaje", "El mensaje es obligatorio.");
        else if (r.mensaje().trim().length() > 2000) e.put("mensaje", "El mensaje no puede superar los 2000 caracteres.");

        lanzarSiHayErrores(e);
    }
}
