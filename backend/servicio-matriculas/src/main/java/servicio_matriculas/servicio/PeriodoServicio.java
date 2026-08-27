package servicio_matriculas.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_matriculas.dominio.EstadoPeriodo;
import servicio_matriculas.dominio.Periodo;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.periodo.ActualizarPeriodoSolicitud;
import servicio_matriculas.dto.periodo.CrearPeriodoSolicitud;
import servicio_matriculas.dto.periodo.PeriodoRespuesta;
import servicio_matriculas.excepcion.ConflictoException;
import servicio_matriculas.excepcion.RecursoNoEncontradoException;
import servicio_matriculas.excepcion.ReglaNegocioException;
import servicio_matriculas.repositorio.PeriodoRepositorio;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PeriodoServicio {

    private final PeriodoRepositorio periodoRepositorio;

    @Transactional
    public PeriodoRespuesta crear(CrearPeriodoSolicitud solicitud) {
        String codigo = mayusculas(solicitud.codigo());
        if (periodoRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo del periodo ya esta registrado");
        }
        validarFechas(solicitud.fechaInicio(), solicitud.fechaFin(),
                solicitud.fechaInicioMatricula(), solicitud.fechaFinMatricula());
        Periodo periodo = new Periodo(codigo, limpiar(solicitud.nombre()), solicitud.fechaInicio(),
                solicitud.fechaFin(), solicitud.fechaInicioMatricula(), solicitud.fechaFinMatricula());
        return respuesta(periodoRepositorio.save(periodo));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<PeriodoRespuesta> listar(String busqueda, EstadoPeriodo estado,
                                                    Pageable pageable) {
        Specification<Periodo> spec = (root, query, cb) -> cb.conjunction();
        if (busqueda != null && !busqueda.isBlank()) {
            String patron = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("codigo")), patron),
                    cb.like(cb.lower(root.get("nombre")), patron)));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(periodoRepositorio.findAll(spec, pageable).map(this::respuesta));
    }

    @Transactional(readOnly = true)
    public PeriodoRespuesta obtener(Long id) {
        return respuesta(buscarEntidad(id));
    }

    @Transactional
    public PeriodoRespuesta actualizar(Long id, ActualizarPeriodoSolicitud solicitud) {
        Periodo periodo = buscarEntidad(id);
        if (periodo.getEstado() != EstadoPeriodo.PLANIFICADO) {
            throw new ReglaNegocioException("Solo se puede editar un periodo planificado");
        }
        String codigo = mayusculas(solicitud.codigo());
        if (periodoRepositorio.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ConflictoException("El codigo del periodo ya esta registrado");
        }
        validarFechas(solicitud.fechaInicio(), solicitud.fechaFin(),
                solicitud.fechaInicioMatricula(), solicitud.fechaFinMatricula());
        periodo.actualizar(codigo, limpiar(solicitud.nombre()), solicitud.fechaInicio(),
                solicitud.fechaFin(), solicitud.fechaInicioMatricula(), solicitud.fechaFinMatricula());
        return respuesta(periodo);
    }

    @Transactional
    public PeriodoRespuesta cambiarEstado(Long id, EstadoPeriodo nuevoEstado) {
        Periodo periodo = buscarEntidad(id);
        if (periodo.getEstado() == nuevoEstado) {
            return respuesta(periodo);
        }
        validarTransicion(periodo.getEstado(), nuevoEstado);
        if (nuevoEstado == EstadoPeriodo.MATRICULA_ABIERTA) {
            LocalDate hoy = LocalDate.now();
            if (hoy.isBefore(periodo.getFechaInicioMatricula())
                    || hoy.isAfter(periodo.getFechaFinMatricula())) {
                throw new ReglaNegocioException(
                        "La fecha actual no esta dentro del rango de matricula del periodo");
            }
            if (periodoRepositorio.existsByEstadoAndIdNot(EstadoPeriodo.MATRICULA_ABIERTA, id)) {
                throw new ConflictoException("Ya existe otro periodo con matricula abierta");
            }
        }
        periodo.cambiarEstado(nuevoEstado);
        return respuesta(periodo);
    }

    @Transactional(readOnly = true)
    public Periodo buscarEntidad(Long id) {
        return periodoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el periodo con id " + id));
    }

    public boolean matriculaVigente(Periodo periodo) {
        LocalDate hoy = LocalDate.now();
        return periodo.getEstado() == EstadoPeriodo.MATRICULA_ABIERTA
                && !hoy.isBefore(periodo.getFechaInicioMatricula())
                && !hoy.isAfter(periodo.getFechaFinMatricula());
    }

    private void validarFechas(LocalDate inicio, LocalDate fin, LocalDate inicioMatricula,
                               LocalDate finMatricula) {
        if (!inicio.isBefore(fin)) {
            throw new ReglaNegocioException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        if (inicioMatricula.isAfter(finMatricula)) {
            throw new ReglaNegocioException("El inicio de matricula no puede ser posterior a su fin");
        }
        if (finMatricula.isAfter(inicio)) {
            throw new ReglaNegocioException(
                    "El proceso de matricula debe finalizar antes o el mismo dia que inician las clases");
        }
    }

    private void validarTransicion(EstadoPeriodo actual, EstadoPeriodo nuevo) {
        boolean valida = switch (actual) {
            case PLANIFICADO -> Set.of(EstadoPeriodo.MATRICULA_ABIERTA,
                    EstadoPeriodo.CANCELADO).contains(nuevo);
            case MATRICULA_ABIERTA -> Set.of(EstadoPeriodo.EN_CURSO,
                    EstadoPeriodo.CANCELADO).contains(nuevo);
            case EN_CURSO -> Set.of(EstadoPeriodo.FINALIZADO,
                    EstadoPeriodo.CANCELADO).contains(nuevo);
            case FINALIZADO, CANCELADO -> false;
        };
        if (!valida) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el periodo de " + actual + " a " + nuevo);
        }
    }

    private PeriodoRespuesta respuesta(Periodo periodo) {
        return new PeriodoRespuesta(periodo.getId(), periodo.getCodigo(), periodo.getNombre(),
                periodo.getFechaInicio(), periodo.getFechaFin(), periodo.getFechaInicioMatricula(),
                periodo.getFechaFinMatricula(), periodo.getEstado(), matriculaVigente(periodo),
                periodo.getCreadoEn(), periodo.getActualizadoEn());
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String mayusculas(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }
}
