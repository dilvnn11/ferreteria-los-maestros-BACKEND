package cl.ferreteria.losmaestros.repository;

import cl.ferreteria.losmaestros.model.MensajeContacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
    List<MensajeContacto> findAllByOrderByFechaDesc();
}
