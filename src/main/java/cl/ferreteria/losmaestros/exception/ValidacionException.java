package cl.ferreteria.losmaestros.exception;

import java.util.Map;

/** Uno o más campos inválidos. El mapa es campo -> mensaje (mismos nombres que usa el frontend). */
public class ValidacionException extends RuntimeException {

    private final Map<String, String> campos;

    public ValidacionException(Map<String, String> campos) {
        super(campos.values().iterator().next());
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
