package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.ReporteResumenDto;
import cl.ferreteria.losmaestros.dto.ReporteResumenDto.PeriodoDto;
import cl.ferreteria.losmaestros.dto.ReporteResumenDto.ProductoTopDto;
import cl.ferreteria.losmaestros.repository.CompraRepository;
import cl.ferreteria.losmaestros.repository.DetalleCompraRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private final CompraRepository compras;
    private final DetalleCompraRepository detalles;

    public ReporteService(CompraRepository compras, DetalleCompraRepository detalles) {
        this.compras = compras;
        this.detalles = detalles;
    }

    /**
     * @param periodo "semana" | "mes" | "anio" — agrupa indicadores en ese horizonte reciente
     */
    @Transactional(readOnly = true)
    public ReporteResumenDto resumen(String periodo) {
        long totalOps = compras.count();
        long montoTotal = compras.sumaTotales();
        long unidades = detalles.sumaUnidades();

        List<ProductoTopDto> top = detalles.topProductos().stream()
                .limit(10)
                .map(row -> new ProductoTopDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .toList();

        List<PeriodoDto> porPeriodo = construirPeriodos(periodo == null ? "mes" : periodo.toLowerCase(Locale.ROOT));

        return new ReporteResumenDto(totalOps, montoTotal, unidades, top, porPeriodo);
    }

    private List<PeriodoDto> construirPeriodos(String periodo) {
        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        List<PeriodoDto> lista = new ArrayList<>();

        switch (periodo) {
            case "semana" -> {
                // Últimas 8 semanas
                for (int i = 7; i >= 0; i--) {
                    LocalDate inicioSemana = hoy.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                    LocalDate finSemana = inicioSemana.plusDays(7);
                    Instant desde = inicioSemana.atStartOfDay().toInstant(ZoneOffset.UTC);
                    Instant hasta = finSemana.atStartOfDay().toInstant(ZoneOffset.UTC);
                    String etiqueta = "Sem " + inicioSemana.format(DateTimeFormatter.ofPattern("dd/MM"));
                    lista.add(new PeriodoDto(etiqueta, compras.contarEntre(desde, hasta), compras.sumaTotalesEntre(desde, hasta)));
                }
            }
            case "anio", "año" -> {
                int anioActual = hoy.getYear();
                for (int a = anioActual - 4; a <= anioActual; a++) {
                    Instant desde = LocalDate.of(a, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
                    Instant hasta = LocalDate.of(a + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
                    lista.add(new PeriodoDto(String.valueOf(a), compras.contarEntre(desde, hasta), compras.sumaTotalesEntre(desde, hasta)));
                }
            }
            default -> {
                // mes: últimos 12 meses
                for (int i = 11; i >= 0; i--) {
                    LocalDate inicioMes = hoy.minusMonths(i).withDayOfMonth(1);
                    LocalDate finMes = inicioMes.plusMonths(1);
                    Instant desde = inicioMes.atStartOfDay().toInstant(ZoneOffset.UTC);
                    Instant hasta = finMes.atStartOfDay().toInstant(ZoneOffset.UTC);
                    String etiqueta = inicioMes.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("es-CL")));
                    lista.add(new PeriodoDto(etiqueta, compras.contarEntre(desde, hasta), compras.sumaTotalesEntre(desde, hasta)));
                }
            }
        }
        return lista;
    }
}
