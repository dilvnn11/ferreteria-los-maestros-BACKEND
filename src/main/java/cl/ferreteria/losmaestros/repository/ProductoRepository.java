package cl.ferreteria.losmaestros.repository;

import cl.ferreteria.losmaestros.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    boolean existsByIdIgnoreCase(String id);

    long countByCategoriaId(String categoriaId);

    /** Bloquea las filas mientras se descuenta el stock, para que dos compras simultáneas no vendan el mismo stock. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> bloquearPorIds(@Param("ids") Collection<String> ids);
}
