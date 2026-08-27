package servicio_matriculas.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_matriculas.dominio.EstadoMatricula;
import servicio_matriculas.dominio.EstadoSeccion;
import servicio_matriculas.dominio.Periodo;
import servicio_matriculas.dominio.Seccion;
import servicio_matriculas.dto.reporte.ResumenPeriodoRespuesta;
import servicio_matriculas.dto.seccion.SeccionRespuesta;
import servicio_matriculas.repositorio.MatriculaRepositorio;
import servicio_matriculas.repositorio.SeccionRepositorio;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServicio {

    private final PeriodoServicio periodoServicio;
    private final SeccionServicio seccionServicio;
    private final SeccionRepositorio seccionRepositorio;
    private final MatriculaRepositorio matriculaRepositorio;

    @Transactional(readOnly = true)
    public ResumenPeriodoRespuesta resumen(Long periodoId) {
        Periodo periodo = periodoServicio.buscarEntidad(periodoId);
        List<Seccion> secciones = seccionRepositorio.findAllByPeriodoIdAndEstadoNot(periodoId,
                EstadoSeccion.CANCELADA);
        long capacidad = secciones.stream().mapToLong(Seccion::getCapacidad).sum();
        long matriculas = matriculaRepositorio.countByPeriodoIdAndEstado(periodoId,
                EstadoMatricula.ACTIVA);
        long estudiantes = matriculaRepositorio.contarEstudiantesUnicos(periodoId,
                EstadoMatricula.ACTIVA);
        long vacantes = Math.max(0, capacidad - matriculas);
        double ocupacion = capacidad == 0 ? 0.0 : Math.round((matriculas * 10000.0) / capacidad) / 100.0;
        return new ResumenPeriodoRespuesta(periodo.getId(), periodo.getCodigo(), secciones.size(),
                matriculas, estudiantes, capacidad, vacantes, ocupacion);
    }

    @Transactional(readOnly = true)
    public List<SeccionRespuesta> ocupacion(Long periodoId) {
        periodoServicio.buscarEntidad(periodoId);
        return seccionRepositorio.findAllByPeriodoIdAndEstadoNot(periodoId, EstadoSeccion.CANCELADA)
                .stream().map(seccionServicio::respuesta).toList();
    }
}
