package cl.ferreteria.losmaestros.repository;

import cl.ferreteria.losmaestros.model.Compra;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    List<Compra> findAllByOrderByFechaDesc();

    List<Compra> findByFechaBetweenOrderByFechaDesc(Instant desde, Instant hasta);

    /** Al eliminar un usuario, sus compras se conservan como compras de invitado. */
    @Modifying
    @Query("update Compra c set c.usuario = null where c.usuario.id = :usuarioId")
    void desvincularUsuario(@Param("usuarioId") String usuarioId);

    @Query("select coalesce(sum(c.total), 0) from Compra c")
    long sumaTotales();

    @Query("select coalesce(sum(c.total), 0) from Compra c where c.fecha >= :desde and c.fecha < :hasta")
    long sumaTotalesEntre(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    @Query("select count(c) from Compra c where c.fecha >= :desde and c.fecha < :hasta")
    long contarEntre(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
