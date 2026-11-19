package cl.ferreteria.losmaestros.repository;

import cl.ferreteria.losmaestros.model.Compra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    List<Compra> findAllByOrderByFechaDesc();

    /** Al eliminar un usuario, sus compras se conservan como compras de invitado. */
    @Modifying
    @Query("update Compra c set c.usuario = null where c.usuario.id = :usuarioId")
    void desvincularUsuario(@Param("usuarioId") String usuarioId);
}
