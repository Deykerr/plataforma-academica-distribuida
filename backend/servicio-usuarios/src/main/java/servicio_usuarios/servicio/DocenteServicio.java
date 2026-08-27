package servicio_usuarios.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_usuarios.dominio.Docente;
import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.RolUsuario;
import servicio_usuarios.dominio.Usuario;
import servicio_usuarios.dto.comun.PaginaRespuesta;
import servicio_usuarios.dto.docente.ActualizarDocenteSolicitud;
import servicio_usuarios.dto.docente.DocenteRespuesta;
import servicio_usuarios.dto.docente.RegistroDocenteSolicitud;
import servicio_usuarios.excepcion.ConflictoException;
import servicio_usuarios.excepcion.RecursoNoEncontradoException;
import servicio_usuarios.repositorio.DocenteRepositorio;
import servicio_usuarios.repositorio.UsuarioRepositorio;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocenteServicio {

    private final DocenteRepositorio docenteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DocenteRespuesta registrar(RegistroDocenteSolicitud solicitud) {
        String correo = normalizarCorreo(solicitud.correo());
        String codigo = normalizarMayusculas(solicitud.codigo());
        String documento = normalizarMayusculas(solicitud.documentoIdentidad());
        validarNuevo(correo, codigo, documento);

        Usuario usuario = usuarioRepositorio.save(new Usuario(correo,
                passwordEncoder.encode(solicitud.clave()), Set.of(RolUsuario.DOCENTE)));
        Docente docente = new Docente(usuario, codigo, limpiar(solicitud.nombres()),
                limpiar(solicitud.apellidos()), documento, limpiar(solicitud.especialidad()),
                limpiarOpcional(solicitud.telefono()));
        return DocenteRespuesta.desde(docenteRepositorio.save(docente));
    }

    @Transactional(readOnly = true)
    public PaginaRespuesta<DocenteRespuesta> listar(String busqueda, Pageable pageable) {
        var pagina = busqueda == null || busqueda.isBlank()
                ? docenteRepositorio.findAll(pageable)
                : docenteRepositorio
                    .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCodigoContainingIgnoreCase(
                            busqueda.trim(), busqueda.trim(), busqueda.trim(), pageable);
        return PaginaRespuesta.desde(pagina.map(DocenteRespuesta::desde));
    }

    @Transactional(readOnly = true)
    public DocenteRespuesta obtener(Long id) {
        return DocenteRespuesta.desde(buscar(id));
    }

    @Transactional
    public DocenteRespuesta actualizar(Long id, ActualizarDocenteSolicitud solicitud) {
        Docente docente = buscar(id);
        String documento = normalizarMayusculas(solicitud.documentoIdentidad());
        if (docenteRepositorio.existsByDocumentoIdentidadAndIdNot(documento, docente.getId())) {
            throw new ConflictoException("El documento de identidad ya esta registrado");
        }
        docente.actualizar(limpiar(solicitud.nombres()), limpiar(solicitud.apellidos()), documento,
                limpiar(solicitud.especialidad()), limpiarOpcional(solicitud.telefono()));
        return DocenteRespuesta.desde(docente);
    }

    @Transactional
    public void desactivar(Long id) {
        buscar(id).getUsuario().cambiarEstado(EstadoUsuario.INACTIVO);
    }

    private void validarNuevo(String correo, String codigo, String documento) {
        if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("El correo ya esta registrado");
        }
        if (docenteRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("El codigo de docente ya esta registrado");
        }
        if (docenteRepositorio.existsByDocumentoIdentidad(documento)) {
            throw new ConflictoException("El documento de identidad ya esta registrado");
        }
    }

    private Docente buscar(Long id) {
        return docenteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el docente con id " + id));
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
