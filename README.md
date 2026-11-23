# Ferretería Los Maestros — Backend

API REST con **Spring Boot 3.5**, **Spring Security + JWT**, **JPA** y **MariaDB/MySQL (XAMPP)**.

## Requisitos

- JDK 17+
- Maven 3.9+
- XAMPP con MariaDB/MySQL en el puerto **3306** (usuario `root` sin contraseña por defecto)

## Base de datos

1. Inicia MySQL desde el panel de XAMPP.
2. (Opcional) Ejecuta el script `sql/01_crear_base_de_datos.sql` para crear la BD con `utf8mb4`.
3. Con `spring.jpa.hibernate.ddl-auto=update` las tablas se crean solas al arrancar.
4. Con `app.seed.enabled=true` se insertan categorías, productos y 3 cuentas demo si las tablas están vacías.

## Arranque

```bash
# Desde la carpeta del backend
mvn spring-boot:run
```

API: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Si el puerto 8080 está ocupado (Tomcat de XAMPP), cambia `server.port` en `application.properties`.

## Cuentas demo

| Rol            | Correo             | Contraseña   |
|----------------|--------------------|--------------|
| Administrador  | admin@duoc.cl      | Admin123     |
| Vendedor       | vendedor@duoc.cl   | Vendedor123  |
| Cliente        | cliente@duoc.cl    | Cliente123   |

Las contraseñas se guardan con **BCrypt**.

## Endpoints principales

### Autenticación (`/api/auth`)
| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/login` | público | JWT + usuario |
| POST | `/registro` | público | Crea cliente |
| GET | `/me` | autenticado | Usuario actual |
| PUT | `/perfil` | autenticado | Editar perfil propio |
| POST | `/refresh` | autenticado | Renovar JWT |

### Productos (`/api/productos`)
CRUD completo. GET público. POST/PUT/DELETE administrador. PATCH stock: admin o vendedor.

### Categorías (`/api/categorias`)
GET público. Escritura solo administrador.

### Usuarios (`/api/usuarios`)
Listar/editar/eliminar/desbloquear: administrador. Crear: admin o vendedor (vendedor solo clientes).

### Compras (`/api/compras`)
| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/` | público (invitado o autenticado) |
| GET | `/mias` | autenticado (solo las propias) |
| GET | `/` | administrador (todas) |

### Reportes (`/api/reportes`)
| Método | Ruta | Acceso |
|--------|------|--------|
| GET | `/resumen?periodo=mes\|semana\|anio` | administrador |

Indicadores: total de operaciones, monto vendido, unidades, top productos y desglose temporal.

### Contacto (`/api/contacto`)
POST público. GET mensajes: administrador.

## Seguridad

- JWT Bearer en cabecera `Authorization`.
- El token solo lleva el **id** del usuario; el **rol** se lee de la BD en cada petición.
- CORS: `http://localhost:5173` y `http://127.0.0.1:5173` (configurable en `app.cors.allowed-origins`).
- Secreto JWT: variable de entorno `JWT_SECRET` (mín. 32 caracteres) o valor de desarrollo en properties.
- Vigencia por defecto: **480 minutos** (`app.jwt.expiration-minutes`). Renovar con `POST /api/auth/refresh`.

## Estructura

```
src/main/java/cl/ferreteria/losmaestros/
├── config/        SecurityConfig · OpenApiConfig · DataInitializer
├── security/      JwtService · JwtAuthFilter
├── model/         Entidades JPA
├── repository/    Spring Data
├── dto/           Request/Response
├── service/       Lógica de negocio
├── controller/    REST + anotaciones Swagger
├── exception/     ApiException · GlobalExceptionHandler
└── util/          Validaciones
```

## Probar con Swagger

1. Abre `http://localhost:8080/swagger-ui.html`
2. `POST /api/auth/login` con `{"correo":"admin@duoc.cl","password":"Admin123"}`
3. Copia el `token` de la respuesta
4. Pulsa **Authorize** → `Bearer <token>`
5. Prueba los endpoints protegidos (CRUD, reportes, etc.)

## Notas

- No subas a GitHub contraseñas reales, el secreto JWT de producción ni credenciales de BD.
- El frontend Vite debe apuntar a esta API (`VITE_API_URL=http://localhost:8080`).
