package servicio_matriculas.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import servicio_matriculas.dominio.EstadoMatricula;
import servicio_matriculas.dto.comun.PaginaRespuesta;
import servicio_matriculas.dto.matricula.CrearMatriculaSolicitud;
import servicio_matriculas.dto.matricula.MatriculaRespuesta;
import servicio_matriculas.dto.matricula.RetirarMatriculaSolicitud;
import servicio_matriculas.dto.matricula.ValidacionMatriculaRespuesta;
import servicio_matriculas.servicio.MatriculaServicio;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matriculas", description = "Inscripciones, retiros, anulaciones y validacion")
public class MatriculaControlador {

    private final MatriculaServicio matriculaServicio;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE')")
    @Operation(summary = "Matricular un estudiante en una seccion")
    public ResponseEntity<MatriculaRespuesta> crear(
            @Valid @RequestBody CrearMatriculaSolicitud solicitud) {
        MatriculaRespuesta respuesta = matriculaServicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(respuesta.id()).toUri();
        return ResponseEntity.created(ubicacion).body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todas las matriculas con filtros administrativos")
    public PaginaRespuesta<MatriculaRespuesta> listar(
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) Long periodoId,
            @RequestParam(required = false) Long seccionId,
            @RequestParam(required = false) EstadoMatricula estado,
            @ParameterObject @PageableDefault(size = 20, sort = "fechaMatricula") Pageable pageable) {
        return matriculaServicio.listar(estudianteId, periodoId, seccionId, estado, pageable);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Listar las matriculas del estudiante autenticado")
    public PaginaRespuesta<MatriculaRespuesta> mias(
            @RequestParam(required = false) Long periodoId,
            @RequestParam(required = false) EstadoMatricula estado,
            @ParameterObject @PageableDefault(size = 20, sort = "fechaMatricula") Pageable pageable) {
        return matriculaServicio.mias(periodoId, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una matricula si es propietario, docente de la seccion o administrador")
    public MatriculaRespuesta obtener(@PathVariable Long id) {
        return matriculaServicio.obtener(id);
    }

    @GetMapping("/{id}/validacion")
    @Operation(summary = "Validar una matricula para el Servicio de Evaluaciones")
    public ValidacionMatriculaRespuesta validar(@PathVariable Long id) {
        return matriculaServicio.validar(id);
    }

    @PatchMapping("/{id}/retiro")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE')")
    @Operation(summary = "Retirar una matricula activa")
    public MatriculaRespuesta retirar(@PathVariable Long id,
                                       @Valid @RequestBody RetirarMatriculaSolicitud solicitud) {
        return matriculaServicio.retirar(id, solicitud.motivo());
    }

    @PatchMapping("/{id}/anulacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Anular administrativamente una matricula")
    public MatriculaRespuesta anular(@PathVariable Long id,
                                      @Valid @RequestBody RetirarMatriculaSolicitud solicitud) {
        return matriculaServicio.anular(id, solicitud.motivo());
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Marcar una matricula como completada")
    public MatriculaRespuesta completar(@PathVariable Long id) {
        return matriculaServicio.completar(id);
    }
}
