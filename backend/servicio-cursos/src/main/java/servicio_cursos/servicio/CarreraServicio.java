package servicio_cursos.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_cursos.dominio.Carrera;
import servicio_cursos.dominio.EstadoRegistro;
import servicio_cursos.dto.carrera.ActualizarCarreraSolicitud;
import servicio_cursos.dto.carrera.CarreraRespuesta;
import servicio_cursos.dto.carrera.CrearCarreraSolicitud;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.excepcion.ConflictoException;
import servicio_cursos.excepcion.RecursoNoEncontradoException;
import servicio_cursos.repositorio.CarreraRepositorio;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CarreraServicio {

    private final CarreraRepositorio carreraRepositorio;

    @Transactional
    public CarreraRespuesta crear(CrearCarreraSolicitud solicitud) {
        String codigo = mayusculas(solicitud.codigo());
        String nombre = limpiar(solicitud.nombre());
        if (carreraRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo de carrera ya esta registrado");
        }
        if (carreraRepositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("El nombre de carrera ya esta registrado");
        }
        Carrera carrera = new Carrera(codigo, nombre, opcional(solicitud.descripcion()),
                solicitud.duracionCiclos());
        return CarreraRespuesta.desde(carreraRepositorio.save(carrera));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<CarreraRespuesta> listar(String busqueda, EstadoRegistro estado, Pageable pageable) {
        Specification<Carrera> spec = (root, query, cb) -> cb.conjunction();
        if (busqueda != null && !busqueda.isBlank()) {
            String patron = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("codigo")), patron)));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(carreraRepositorio.findAll(spec, pageable).map(CarreraRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public CarreraRespuesta obtener(Long id) {
        return CarreraRespuesta.desde(buscar(id));
    }

    @Transactional
    public CarreraRespuesta actualizar(Long id, ActualizarCarreraSolicitud solicitud) {
        Carrera carrera = buscar(id);
        String nombre = limpiar(solicitud.nombre());
        if (carreraRepositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("El nombre de carrera ya esta registrado");
        }
        carrera.actualizar(nombre, opcional(solicitud.descripcion()), solicitud.duracionCiclos());
        return CarreraRespuesta.desde(carrera);
    }

    @Transactional
    public CarreraRespuesta cambiarEstado(Long id, EstadoRegistro estado) {
        Carrera carrera = buscar(id);
        carrera.cambiarEstado(estado);
        return CarreraRespuesta.desde(carrera);
    }

    Carrera buscarEntidad(Long id) {
        return buscar(id);
    }

    private Carrera buscar(Long id) {
        return carreraRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la carrera con id " + id));
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String mayusculas(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : limpiar(valor);
    }
}
