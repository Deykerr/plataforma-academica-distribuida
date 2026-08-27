package servicio_matriculas.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "secciones")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Seccion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private Periodo periodo;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(name = "aula_id", nullable = false)
    private Long aulaId;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSeccion estado;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioSeccion> horarios = new ArrayList<>();

    public Seccion(Periodo periodo, Long cursoId, Long aulaId, Long docenteId, String codigo,
                   Integer capacidad) {
        this.estado = EstadoSeccion.PLANIFICADA;
        actualizar(periodo, cursoId, aulaId, docenteId, codigo, capacidad);
        iniciarAuditoria();
    }

    public void actualizar(Periodo periodo, Long cursoId, Long aulaId, Long docenteId,
                           String codigo, Integer capacidad) {
        this.periodo = periodo;
        this.cursoId = cursoId;
        this.aulaId = aulaId;
        this.docenteId = docenteId;
        this.codigo = codigo;
        this.capacidad = capacidad;
        marcarActualizacion();
    }

    public void reemplazarHorarios(List<HorarioSeccion> nuevos) {
        horarios.clear();
        nuevos.forEach(horario -> horario.asignarSeccion(this));
        horarios.addAll(nuevos);
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoSeccion estado) {
        this.estado = estado;
        marcarActualizacion();
    }
}
