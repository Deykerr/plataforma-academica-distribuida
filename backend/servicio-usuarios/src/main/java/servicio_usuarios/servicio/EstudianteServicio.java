package servicio_usuarios.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_usuarios.dominio.Estudiante;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;
import servicio_usuarios.dto.comun.PaginaRespuesta;
import servicio_usuarios.dto.estudiante.ActualizarEstudianteSolicitud;
import servicio_usuarios.dto.estudiante.EstudianteRespuesta;
import servicio_usuarios.dto.estudiante.RegistroEstudianteSolicitud;
import servicio_usuarios.excepcion.ConflictoException;
import servicio_usuarios.excepcion.RecursoNoEncontradoException;
import servicio_usuarios.repositorio.EstudianteRepositorio;
import servicio_usuarios.repositorio.UsuarioRepositorio;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EstudianteServicio {

    private final EstudianteRepositorio estudianteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EstudianteRespuesta registrar(RegistroEstudianteSolicitud solicitud) {
        String correo = normalizarCorreo(solicitud.correo());
        String codigo = normalizarMayusculas(solicitud.codigo());
        String documento = normalizarMayusculas(solicitud.documentoIdentidad());
        validarNuevo(correo, codigo, documento);

        Usuario usuario = usuarioRepositorio.save(new Usuario(correo,
                passwordEncoder.encode(solicitud.clave()), Set.of(RolUsuario.ESTUDIANTE)));
        Estudiante estudiante = new Estudiante(usuario, codigo, limpiar(solicitud.nombres()),
                limpiar(solicitud.apellidos()), documento, solicitud.fechaNacimiento(),
                limpiarOpcional(solicitud.telefono()), limpiarOpcional(solicitud.direccion()),
                solicitud.carreraId());
        return EstudianteRespuesta.desde(estudianteRepositorio.save(estudiante));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<EstudianteRespuesta> listar(String busqueda, Pageable pageable) {
        var pagina = busqueda == null || busqueda.isBlank()
                ? estudianteRepositorio.findAll(pageable)
                : estudianteRepositorio
                    .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCodigoContainingIgnoreCase(
                            busqueda.trim(), busqueda.trim(), busqueda.trim(), pageable);
        return PaginaRespuesta.desde(pagina.map(EstudianteRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public EstudianteRespuesta obtener(Long id) {
        return EstudianteRespuesta.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public EstudianteRespuesta obtenerPropio(String correo) {
        return EstudianteRespuesta.desde(estudianteRepositorio.findByUsuarioCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no tiene un perfil de estudiante")));
    }

    @Transactional
    public EstudianteRespuesta actualizar(Long id, ActualizarEstudianteSolicitud solicitud) {
        Estudiante estudiante = buscar(id);
        actualizarEntidad(estudiante, solicitud);
        return EstudianteRespuesta.desde(estudiante);
    }

    @Transactional
    public EstudianteRespuesta actualizarPropio(String correo, ActualizarEstudianteSolicitud solicitud) {
        Estudiante estudiante = estudianteRepositorio.findByUsuarioCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no tiene un perfil de estudiante"));
        actualizarEntidad(estudiante, solicitud);
        return EstudianteRespuesta.desde(estudiante);
    }

    @Transactional
    public void desactivar(Long id) {
        buscar(id).getUsuario().cambiarEstado(servicio_usuarios.dominio.EstadoUsuario.INACTIVO);
    }

    private void actualizarEntidad(Estudiante estudiante, ActualizarEstudianteSolicitud solicitud) {
        String documento = normalizarMayusculas(solicitud.documentoIdentidad());
        if (estudianteRepositorio.existsByDocumentoIdentidadAndIdNot(documento, estudiante.getId())) {
            throw new ConflictoException("El documento de identidad ya esta registrado");
        }
        estudiante.actualizar(limpiar(solicitud.nombres()), limpiar(solicitud.apellidos()), documento,
                solicitud.fechaNacimiento(), limpiarOpcional(solicitud.telefono()),
                limpiarOpcional(solicitud.direccion()), solicitud.carreraId());
    }

    private void validarNuevo(String correo, String codigo, String documento) {
        if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("El correo ya esta registrado");
        }
        if (estudianteRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo de estudiante ya esta registrado");
        }
        if (estudianteRepositorio.existsByDocumentoIdentidad(documento)) {
            throw new ConflictoException("El documento de identidad ya esta registrado");
        }
    }

    private Estudiante buscar(Long id) {
        return estudianteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estudiante con id " + id));
    }

    private String normalizarCorreo(String valor) {
        return limpiar(valor).toLowerCase(Locale.ROOT);
    }

    private String normalizarMayusculas(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : limpiar(valor);
    }
}
