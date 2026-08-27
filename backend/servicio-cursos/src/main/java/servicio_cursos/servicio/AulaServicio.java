package servicio_cursos.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_cursos.dominio.Aula;
import servicio_cursos.dominio.EstadoAula;
import servicio_cursos.dominio.TipoAula;
import servicio_cursos.dto.aula.ActualizarAulaSolicitud;
import servicio_cursos.dto.aula.AulaRespuesta;
import servicio_cursos.dto.aula.CrearAulaSolicitud;
import servicio_cursos.dto.aula.ValidacionAulaRespuesta;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.excepcion.ConflictoException;
import servicio_cursos.excepcion.RecursoNoEncontradoException;
import servicio_cursos.repositorio.AulaRepositorio;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AulaServicio {

    private final AulaRepositorio aulaRepositorio;

    @Transactional
    public AulaRespuesta crear(CrearAulaSolicitud solicitud) {
        String codigo = mayusculas(solicitud.codigo());
        if (aulaRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo de aula ya esta registrado");
        }
        Aula aula = new Aula(codigo, limpiar(solicitud.nombre()), solicitud.tipo(),
                solicitud.capacidad(), limpiar(solicitud.ubicacion()));
        return AulaRespuesta.desde(aulaRepositorio.save(aula));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<AulaRespuesta> listar(String busqueda, TipoAula tipo, EstadoAula estado,
                                                 Pageable pageable) {
        Specification<Aula> spec = (root, query, cb) -> cb.conjunction();
        if (busqueda != null && !busqueda.isBlank()) {
            String patron = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("codigo")), patron),
                    cb.like(cb.lower(root.get("ubicacion")), patron)));
        }
        if (tipo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(aulaRepositorio.findAll(spec, pageable).map(AulaRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public AulaRespuesta obtener(Long id) {
        return AulaRespuesta.desde(buscar(id));
    }

    @Transactional
    public AulaRespuesta actualizar(Long id, ActualizarAulaSolicitud solicitud) {
        Aula aula = buscar(id);
        aula.actualizar(limpiar(solicitud.nombre()), solicitud.tipo(), solicitud.capacidad(),
                limpiar(solicitud.ubicacion()));
        return AulaRespuesta.desde(aula);
    }

    @Transactional
    public AulaRespuesta cambiarEstado(Long id, EstadoAula estado) {
        Aula aula = buscar(id);
        aula.cambiarEstado(estado);
        return AulaRespuesta.desde(aula);
    }

    @Transactional(readOnly = true)
    public ValidacionAulaRespuesta validar(Long id, Integer aforoRequerido) {
        return aulaRepositorio.findById(id)
                .map(aula -> {
                    boolean aforoSuficiente = aforoRequerido == null || aula.getCapacidad() >= aforoRequerido;
                    return new ValidacionAulaRespuesta(aula.getId(), true,
                            aula.estaDisponible() && aforoSuficiente, aforoSuficiente,
                            aula.getCapacidad(), aula.getTipo(), aula.getEstado());
                })
                .orElseGet(() -> new ValidacionAulaRespuesta(id, false, false,
                        false, null, null, null));
    }

    private Aula buscar(Long id) {
        return aulaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el aula con id " + id));
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String mayusculas(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }
}
