-- Ejecutar en phpMyAdmin (pestaña SQL) o en el cliente mysql de XAMPP.
-- Solo crea la base vacía con soporte completo de tildes y ñ.
-- Las tablas y los datos iniciales los crea la aplicación al arrancar.
CREATE DATABASE IF NOT EXISTS ferreteria_los_maestros
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
