package servicio_cursos.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import servicio_cursos.dominio.EstadoRegistro;
import servicio_cursos.dto.catalogo.CatalogoCarreraRespuesta;
import servicio_cursos.dto.catalogo.CatalogoCicloRespuesta;
import servicio_cursos.dto.catalogo.CatalogoCursoRespuesta;
import servicio_cursos.repositorio.CarreraRepositorio;
import servicio_cursos.repositorio.CicloRepositorio;
import servicio_cursos.repositorio.CursoRepositorio;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoServicio {

    private final CarreraRepositorio carreraRepositorio;
    private final CicloRepositorio cicloRepositorio;
    private final CursoRepositorio cursoRepositorio;

    @Transactional(readOnly = true)
    public List<CatalogoCarreraRespuesta> obtenerCatalogoActivo() {
        return carreraRepositorio.findAllByEstadoOrderByNombreAsc(EstadoRegistro.ACTIVO).stream()
                .map(carrera -> {
                    List<CatalogoCicloRespuesta> ciclos = cicloRepositorio
                            .findAllByCarreraIdAndEstadoOrderByNumeroAsc(carrera.getId(), EstadoRegistro.ACTIVO)
                            .stream()
                            .map(ciclo -> new CatalogoCicloRespuesta(ciclo.getId(), ciclo.getNumero(),
                                    ciclo.getNombre(), cursoRepositorio
                                            .findAllByCicloIdAndEstadoOrderByNombreAsc(
                                                    ciclo.getId(), EstadoRegistro.ACTIVO)
                                            .stream().map(CatalogoCursoRespuesta::desde).toList()))
                            .toList();
                    return new CatalogoCarreraRespuesta(carrera.getId(), carrera.getCodigo(),
                            carrera.getNombre(), carrera.getDuracionCiclos(), ciclos);
                })
                .toList();
    }
}
