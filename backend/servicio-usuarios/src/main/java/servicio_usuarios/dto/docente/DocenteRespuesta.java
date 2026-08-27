package servicio_usuarios.dto.docente;

import servicio_usuarios.dominio.Docente;
import servicio_usuarios.dominio.EstadoUsuario;
import servicio_usuarios.dominio.RolUsuario;

import java.util.Set;

public record DocenteRespuesta(
        Long id, Long usuarioId, String correo, Set<RolUsuario> roles, EstadoUsuario estado,
        String codigo, String nombres, String apellidos, String documentoIdentidad,
        String especialidad, String telefono
) {
    public static DocenteRespuesta desde(Docente docente) {
        return new DocenteRespuesta(
                docente.getId(), docente.getUsuario().getId(), docente.getUsuario().getCorreo(),
                Set.copyOf(docente.getUsuario().getRoles()), docente.getUsuario().getEstado(),
                docente.getCodigo(), docente.getNombres(), docente.getApellidos(),
                docente.getDocumentoIdentidad(), docente.getEspecialidad(), docente.getTelefono());
    }
}
