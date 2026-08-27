package servicio_cursos.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_cursos.dominio.Carrera;
import servicio_cursos.dominio.Ciclo;
import servicio_cursos.dominio.Curso;
import servicio_cursos.dominio.EstadoRegistro;
import servicio_cursos.dto.comun.PaginaRespuesta;
import servicio_cursos.dto.curso.ActualizarCursoSolicitud;
import servicio_cursos.dto.curso.CrearCursoSolicitud;
import servicio_cursos.dto.curso.CursoRespuesta;
import servicio_cursos.dto.curso.ValidacionCursoRespuesta;
import servicio_cursos.excepcion.ConflictoException;
import servicio_cursos.excepcion.RecursoNoEncontradoException;
import servicio_cursos.excepcion.ReglaNegocioException;
import servicio_cursos.repositorio.CursoRepositorio;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoServicio {

    private final CursoRepositorio cursoRepositorio;
    private final CarreraServicio carreraServicio;
    private final CicloServicio cicloServicio;

    @Transactional
    public CursoRespuesta crear(CrearCursoSolicitud solicitud) {
        String codigo = mayusculas(solicitud.codigo());
        if (cursoRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo de curso ya esta registrado");
        }
        Carrera carrera = carreraServicio.buscarEntidad(solicitud.carreraId());
        Ciclo ciclo = cicloServicio.buscarEntidad(solicitud.cicloId());
        validarUbicacionAcademica(carrera, ciclo);
        validarHoras(solicitud.horasTeoria(), solicitud.horasPractica());

        Curso curso = cursoRepositorio.save(new Curso(carrera, ciclo, codigo, limpiar(solicitud.nombre()),
                opcional(solicitud.descripcion()), solicitud.creditos(), solicitud.horasTeoria(),
                solicitud.horasPractica()));
        curso.cambiarPrerequisitos(resolverPrerequisitos(solicitud.prerequisitoIds(), carrera, ciclo, curso.getId()));
        return CursoRespuesta.desde(curso);
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<CursoRespuesta> listar(String busqueda, Long carreraId, Long cicloId,
                                                  EstadoRegistro estado, Pageable pageable) {
        Specification<Curso> spec = (root, query, cb) -> cb.conjunction();
        if (busqueda != null && !busqueda.isBlank()) {
            String patron = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("codigo")), patron)));
        }
        if (carreraId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("carrera").get("id"), carreraId));
        }
        if (cicloId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ciclo").get("id"), cicloId));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        return PaginaRespuesta.desde(cursoRepositorio.findAll(spec, pageable).map(CursoRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public CursoRespuesta obtener(Long id) {
        return CursoRespuesta.desde(buscar(id));
    }

    @Transactional
    public CursoRespuesta actualizar(Long id, ActualizarCursoSolicitud solicitud) {
        Curso curso = buscar(id);
        Carrera carrera = carreraServicio.buscarEntidad(solicitud.carreraId());
        Ciclo ciclo = cicloServicio.buscarEntidad(solicitud.cicloId());
        validarUbicacionAcademica(carrera, ciclo);
        validarHoras(solicitud.horasTeoria(), solicitud.horasPractica());
        validarDependientes(curso, carrera, ciclo);
        Set<Curso> prerequisitos = resolverPrerequisitos(solicitud.prerequisitoIds(), carrera, ciclo, id);

        curso.actualizar(carrera, ciclo, limpiar(solicitud.nombre()), opcional(solicitud.descripcion()),
                solicitud.creditos(), solicitud.horasTeoria(), solicitud.horasPractica());
        curso.cambiarPrerequisitos(prerequisitos);
        return CursoRespuesta.desde(curso);
    }

    @Transactional
    public CursoRespuesta cambiarEstado(Long id, EstadoRegistro estado) {
        Curso curso = buscar(id);
        if (estado == EstadoRegistro.ACTIVO) {
            validarUbicacionAcademica(curso.getCarrera(), curso.getCiclo());
            boolean prerequisitoInactivo = curso.getPrerequisitos().stream().anyMatch(p -> !p.estaActivo());
            if (prerequisitoInactivo) {
                throw new ReglaNegocioException("No se puede activar un curso con prerrequisitos inactivos");
            }
        } else {
            boolean tieneDependientesActivos = cursoRepositorio.findAllByPrerequisitosId(id).stream()
                    .anyMatch(Curso::estaActivo);
            if (tieneDependientesActivos) {
                throw new ReglaNegocioException(
                        "No se puede desactivar el curso mientras sea prerrequisito de cursos activos");
            }
        }
        curso.cambiarEstado(estado);
        return CursoRespuesta.desde(curso);
    }

    @Transactional(readOnly = true)
    public ValidacionCursoRespuesta validar(Long id) {
        return cursoRepositorio.findById(id)
                .map(curso -> new ValidacionCursoRespuesta(curso.getId(), true,
                        curso.estaActivo() && curso.getCarrera().estaActiva() && curso.getCiclo().estaActivo(),
                        curso.getCarrera().getId(), curso.getCiclo().getId(), curso.getCreditos(),
                        curso.getPrerequisitos().stream().map(Curso::getId).collect(Collectors.toSet())))
                .orElseGet(() -> new ValidacionCursoRespuesta(id, false, false,
                        null, null, null, Collections.emptySet()));
    }

    private Set<Curso> resolverPrerequisitos(Set<Long> ids, Carrera carrera, Ciclo ciclo, Long cursoId) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        if (cursoId != null && ids.contains(cursoId)) {
            throw new ReglaNegocioException("Un curso no puede ser su propio prerrequisito");
        }
        List<Curso> encontrados = cursoRepositorio.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RecursoNoEncontradoException("Uno o mas cursos prerrequisito no existen");
        }
        for (Curso prerequisito : encontrados) {
            if (!prerequisito.estaActivo()) {
                throw new ReglaNegocioException("Todos los prerrequisitos deben estar activos");
            }
            if (!prerequisito.getCarrera().getId().equals(carrera.getId())) {
                throw new ReglaNegocioException("Los prerrequisitos deben pertenecer a la misma carrera");
            }
            if (prerequisito.getCiclo().getNumero() >= ciclo.getNumero()) {
                throw new ReglaNegocioException("Un prerrequisito debe pertenecer a un ciclo anterior");
            }
        }
        return encontrados.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validarDependientes(Curso curso, Carrera carrera, Ciclo ciclo) {
        for (Curso dependiente : cursoRepositorio.findAllByPrerequisitosId(curso.getId())) {
            if (!dependiente.getCarrera().getId().equals(carrera.getId())
                    || dependiente.getCiclo().getNumero() <= ciclo.getNumero()) {
                throw new ReglaNegocioException(
                        "El cambio invalidaria a un curso que tiene este curso como prerrequisito");
            }
        }
    }

    private void validarUbicacionAcademica(Carrera carrera, Ciclo ciclo) {
        if (!carrera.estaActiva() || !ciclo.estaActivo()) {
            throw new ReglaNegocioException("La carrera y el ciclo deben estar activos");
        }
        if (!ciclo.getCarrera().getId().equals(carrera.getId())) {
            throw new ReglaNegocioException("El ciclo no pertenece a la carrera indicada");
        }
    }

    private void validarHoras(Integer teoria, Integer practica) {
        if (teoria + practica <= 0) {
            throw new ReglaNegocioException("El curso debe tener al menos una hora teorica o practica");
        }
    }

    private Curso buscar(Long id) {
        return cursoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el curso con id " + id));
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
