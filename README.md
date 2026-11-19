# Ferretería Los Maestros · Backend (Spring Boot + MariaDB/XAMPP)

API REST para el frontend `ferreteria_los_maestros` (React + TypeScript).
Replica las reglas de negocio que hoy viven en los contextos del frontend (usuarios, roles,
productos, categorías, carrito/stock, bloqueo por 3 intentos) y las guarda en MySQL/MariaDB.

**Stack:** Java 17+ · Spring Boot 3.5 · Spring Security + JWT · Spring Data JPA · MariaDB (XAMPP) · Maven

---

## 1. Requisitos

- **JDK 17 o superior** (IntelliJ puede descargarlo: *File → Project Structure → SDK → Download JDK*).
- **IntelliJ IDEA** (Community sirve).
- **XAMPP** con los módulos **Apache** (opcional) y **MySQL** (obligatorio).

## 2. Base de datos con XAMPP

1. Abre el *XAMPP Control Panel* y pulsa **Start** en **MySQL**.
2. Entra a <http://localhost/phpmyadmin> → pestaña **SQL** → pega y ejecuta `sql/01_crear_base_de_datos.sql`
   (crea `ferreteria_los_maestros` con codificación `utf8mb4`).
3. No hay que crear tablas: Hibernate las crea al arrancar (`ddl-auto=update`) y
   `DataInitializer` inserta categorías, 27 productos y las 3 cuentas demo si las tablas están vacías.

> Si MySQL de XAMPP usa otro puerto (p. ej. 3307) o tiene contraseña para `root`,
> edita `src/main/resources/application.properties` (`spring.datasource.*`).

## 3. Abrir y ejecutar en IntelliJ

1. **File → Open…** y elige la carpeta del backend (la que contiene `pom.xml`). Acepta *Trust Project*.
2. Espera a que Maven descargue las dependencias (barra inferior).
3. Verifica el JDK: *File → Project Structure → Project SDK* = 17 o superior.
4. Abre `FerreteriaApplication.java` y pulsa el triángulo verde ▶ junto a `main`.
5. Deberías ver en consola `Started FerreteriaApplication` y `Usuarios demo creados`.
6. Prueba: <http://localhost:8080/api/productos> (debe devolver el JSON de 27 productos).

## 4. Cuentas demo (las mismas del frontend)

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | Admin123 |
| Vendedor | vendedor@duoc.cl | Vendedor123 |
| Cliente | cliente@duoc.cl | Cliente123 |

Las contraseñas se guardan con **BCrypt** (en phpMyAdmin verás hashes, no texto).

## 5. Endpoints

Autenticación: enviar `Authorization: Bearer <token>` (el token lo entrega `/api/auth/login`).
Errores siempre con la forma `{ "error": "mensaje", "campos": { "correo": "..." } }` (`campos` solo en validaciones).

| Método | Ruta | Quién | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | `{correo, password}` → `{token, usuario}`. 3 fallos bloquean la cuenta (HTTP 423) |
| POST | `/api/auth/registro` | público | Crea siempre un **cliente** |
| GET | `/api/auth/me` | sesión | Usuario actual |
| PUT | `/api/auth/perfil` | sesión | Edita perfil propio (sin rol ni contraseña) |
| GET | `/api/productos` | público | Filtros: `?texto=&categoriaId=&precioMax=` |
| GET | `/api/productos/{id}` | público | Detalle |
| POST | `/api/productos` | admin | Crear (el código `id` es único) |
| PUT | `/api/productos/{id}` | admin | Editar (el código no cambia) |
| PATCH | `/api/productos/{id}/stock` | admin, vendedor | `{stock}` |
| DELETE | `/api/productos/{id}` | admin | Eliminar |
| GET | `/api/categorias` | público | Listar |
| POST / PUT / DELETE | `/api/categorias[/{id}]` | admin | No se elimina una categoría con productos (409) |
| GET | `/api/usuarios` | admin | Listar (incluye `bloqueado`) |
| POST | `/api/usuarios` | admin (cualquier rol), vendedor (solo cliente) | Crear |
| PUT | `/api/usuarios/{id}` | admin | Editar (no puede cambiar su propio rol) |
| DELETE | `/api/usuarios/{id}` | admin | Eliminar (no a sí mismo) |
| POST | `/api/usuarios/{id}/desbloquear` | admin | Reinicia los intentos fallidos |
| POST | `/api/compras` | público | `{items:[{productoId,cantidad}]}`: valida stock, lo descuenta y registra la compra (transaccional). Si hay token, la asocia al usuario |
| GET | `/api/compras/mias` | sesión | Historial propio |
| GET | `/api/compras` | admin | Todas las compras |
| POST | `/api/contacto` | público | Guarda la consulta del formulario |
| GET | `/api/contacto` | admin | Lista las consultas |

### Probar con curl

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"correo":"admin@duoc.cl","password":"Admin123"}'

# Usar el token
curl http://localhost:8080/api/usuarios -H "Authorization: Bearer <TOKEN>"

# Comprar como invitado
curl -X POST http://localhost:8080/api/compras -H "Content-Type: application/json" \
     -d '{"items":[{"productoId":"MC001","cantidad":2}]}'
```

## 6. Estructura

```
src/main/java/cl/ferreteria/losmaestros/
├── config/        SecurityConfig (rutas, CORS, JWT) · DataInitializer (datos iniciales)
├── security/      JwtService · JwtAuthFilter
├── model/         Entidades JPA: Usuario, Categoria, Producto, Compra, DetalleCompra, MensajeContacto, Rol
├── repository/    Interfaces Spring Data
├── dto/           Objetos de entrada/salida (misma forma que los tipos del frontend)
├── service/       Reglas de negocio
├── controller/    Endpoints REST
├── exception/     ApiException · ValidacionException · GlobalExceptionHandler
└── util/          Validaciones (mismas reglas que utils/validaciones.ts)
src/main/resources/  application.properties · seed/productos.json
sql/                 01_crear_base_de_datos.sql
```

## 7. Decisiones y diferencias respecto al frontend

- **Los precios y el stock se toman de la base de datos** al comprar; el cliente solo manda `productoId` y `cantidad`.
- **Permisos en dos capas**: `@PreAuthorize` en los controladores y verificación dentro de los servicios.
  El rol se lee de la BD en cada petición, no del token.
- **Bloqueo de cuenta**: el contador vive en `usuarios.intentos_fallidos`. Solo cuenta para correos que existen
  (el frontend también contaba correos inexistentes; así no se acumulan datos de correos ajenos).
- **Imágenes**: el campo `imagen` guarda el nombre de archivo (`MC001.png`) o una URL, igual que ahora;
  el frontend sigue resolviendo los archivos desde `src/assets/img/productos`.
- **Carrito**: sigue en el navegador (LocalStorage); el backend recibe el carrito solo al finalizar la compra.
- Eliminar un usuario conserva sus compras como compras de invitado.
- CORS permite `http://localhost:5173` (Vite). Para otros orígenes edita `app.cors.allowed-origins`.

## 8. Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `Communications link failure` / `Connection refused` | MySQL de XAMPP no está iniciado, o el puerto no es 3306 |
| `Access denied for user 'root'` | `root` tiene contraseña: ponla en `spring.datasource.password` |
| `Port 8080 was already in use` | Cambia `server.port` (Tomcat de XAMPP suele usar 8080) |
| Tildes raras en phpMyAdmin | La BD no se creó con `utf8mb4`: ejecuta el script `sql/01_...` y borra la BD anterior |
| Quiero reiniciar los datos | En phpMyAdmin: `DROP DATABASE ferreteria_los_maestros;`, vuelve a ejecutar el script SQL y reinicia la app |
