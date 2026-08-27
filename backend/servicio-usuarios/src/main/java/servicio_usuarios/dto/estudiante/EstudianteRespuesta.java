package servicio_usuarios.dto.estudiante;

import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.Estudiante;
import servicio_usuarios.dominio.RolUsuario;

import java.time.LocalDate;
import java.util.Set;

public record EstudianteRespuesta(
        Long id, Long usuarioId, String correo, Set<RolUsuario> roles, EstadoUsuario estado,
        String codigo, String nombres, String apellidos, String documentoIdentidad,
        LocalDate fechaNacimiento, String telefono, String direccion, Long carreraId
) {
    public static EstudianteRespuesta desde(Estudiante estudiante) {
        return new EstudianteRespuesta(
                estudiante.getId(), estudiante.getUsuario().getId(), estudiante.getUsuario().getCorreo(),
                Set.copyOf(estudiante.getUsuario().getRoles()), estudiante.getUsuario().getEstado(),
                estudiante.getCodigo(), estudiante.getNombres(), estudiante.getApellidos(),
                estudiante.getDocumentoIdentidad(), estudiante.getFechaNacimiento(), estudiante.getTelefono(),
                estudiante.getDireccion(), estudiante.getCarreraId());
    }
}
