package servicio_cursos.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_cursos.dominio.Carrera;
import servicio_cursos.dominio.Ciclo;
import servicio_cursos.dominio.EstadoRegistro;
import servicio_cursos.dto.ciclo.ActualizarCicloSolicitud;
import servicio_cursos.dto.ciclo.CicloRespuesta;
import servicio_cursos.dto.ciclo.CrearCicloSolicitud;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.excepcion.ConflictoException;
import servicio_cursos.excepcion.RecursoNoEncontradoException;
import servicio_cursos.excepcion.ReglaNegocioException;
import servicio_cursos.repositorio.CicloRepositorio;

@Service
@RequiredArgsConstructor
public class CicloServicio {

    private final CicloRepositorio cicloRepositorio;
    private final CarreraServicio carreraServicio;

    @Transactional
    public CicloRespuesta crear(CrearCicloSolicitud solicitud) {
        Carrera carrera = carreraServicio.buscarEntidad(solicitud.carreraId());
        validarCarreraYNumero(carrera, solicitud.numero());
        if (cicloRepositorio.existsByCarreraIdAndNumero(carrera.getId(), solicitud.numero())) {
            throw new ConflictoException("La carrera ya tiene registrado ese numero de ciclo");
        }
        return CicloRespuesta.desde(cicloRepositorio.save(
                new Ciclo(carrera, solicitud.numero(), limpiar(solicitud.nombre()))));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<CicloRespuesta> listar(Long carreraId, EstadoRegistro estado, Pageable pageable) {
        Specification<Ciclo> spec = (root, query, cb) -> cb.conjunction();
        if (carreraId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("carrera").get("id"), carreraId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(cicloRepositorio.findAll(spec, pageable).map(CicloRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public CicloRespuesta obtener(Long id) {
        return CicloRespuesta.desde(buscar(id));
    }

    @Transactional
    public CicloRespuesta actualizar(Long id, ActualizarCicloSolicitud solicitud) {
        Ciclo ciclo = buscar(id);
        validarNumero(ciclo.getCarrera(), solicitud.numero());
        if (cicloRepositorio.existsByCarreraIdAndNumeroAndIdNot(
                ciclo.getCarrera().getId(), solicitud.numero(), id)) {
            throw new ConflictoException("La carrera ya tiene registrado ese numero de ciclo");
        }
        ciclo.actualizar(solicitud.numero(), limpiar(solicitud.nombre()));
        return CicloRespuesta.desde(ciclo);
    }

    @Transactional
    public CicloRespuesta cambiarEstado(Long id, EstadoRegistro estado) {
        Ciclo ciclo = buscar(id);
        if (estado == EstadoRegistro.ACTIVO && !ciclo.getCarrera().estaActiva()) {
            throw new ReglaNegocioException("No se puede activar un ciclo de una carrera inactiva");
        }
        ciclo.cambiarEstado(estado);
        return CicloRespuesta.desde(ciclo);
    }

    Ciclo buscarEntidad(Long id) {
        return buscar(id);
    }

    private Ciclo buscar(Long id) {
        return cicloRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ciclo con id " + id));
    }

    private void validarCarreraYNumero(Carrera carrera, Integer numero) {
        if (!carrera.estaActiva()) {
            throw new ReglaNegocioException("No se pueden agregar ciclos a una carrera inactiva");
        }
        validarNumero(carrera, numero);
    }

    private void validarNumero(Carrera carrera, Integer numero) {
        if (numero > carrera.getDuracionCiclos()) {
            throw new ReglaNegocioException("El ciclo excede la duracion configurada de la carrera");
        }
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }
}
