package cl.ferreteria.losmaestros.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** En JSON viaja en minúsculas ("administrador", "vendedor", "cliente"), igual que en el frontend. */
public enum Rol {
    ADMINISTRADOR("administrador"),
    VENDEDOR("vendedor"),
    CLIENTE("cliente");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /** Devuelve null si el texto no corresponde a un rol (la validación lo reporta después). */
    @JsonCreator
    public static Rol desde(String texto) {
        if (texto == null) return null;
        for (Rol r : values()) {
            if (r.valor.equalsIgnoreCase(texto.trim())) return r;
        }
        return null;
    }
}
