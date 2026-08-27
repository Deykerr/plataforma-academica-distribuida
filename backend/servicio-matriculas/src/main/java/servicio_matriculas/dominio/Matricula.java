package servicio_matriculas.dominio;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
@Table(name = "matriculas")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Matricula extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private Periodo periodo;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(name = "fecha_matricula", nullable = false)
    private OffsetDateTime fechaMatricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMatricula estado;

    @Column(name = "fecha_retiro")
    private OffsetDateTime fechaRetiro;

    @Column(name = "motivo_retiro", length = 300)
    private String motivoRetiro;

    public Matricula(Long estudianteId, Seccion seccion) {
        this.estudianteId = estudianteId;
        this.seccion = seccion;
        this.periodo = seccion.getPeriodo();
        this.cursoId = seccion.getCursoId();
        this.fechaMatricula = OffsetDateTime.now(ZoneOffset.UTC);
        this.estado = EstadoMatricula.ACTIVA;
        iniciarAuditoria();
    }

    public void retirar(EstadoMatricula nuevoEstado, String motivo) {
        this.estado = nuevoEstado;
        this.motivoRetiro = motivo;
        this.fechaRetiro = OffsetDateTime.now(ZoneOffset.UTC);
        marcarActualizacion();
    }

    public void completar() {
        this.estado = EstadoMatricula.COMPLETADA;
        marcarActualizacion();
    }
}
