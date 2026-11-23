package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.ReporteResumenDto;
import cl.ferreteria.losmaestros.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Dashboard administrativo con indicadores reales de la BD")
@SecurityRequirement(name = "bearer-jwt")
public class ReporteController {

    private final ReporteService reportes;

    public ReporteController(ReporteService reportes) {
        this.reportes = reportes;
    }

    /**
     * @param periodo semana | mes | anio — desglose temporal (por defecto mes)
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Resumen de ventas",
            description = "Total de operaciones, monto vendido, unidades y productos más solicitados. "
                    + "Incluye desglose por semana, mes o año.")
    public ReporteResumenDto resumen(@RequestParam(defaultValue = "mes") String periodo) {
        return reportes.resumen(periodo);
    }
}
