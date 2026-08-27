package servicio_evaluaciones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "calificaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calificacion extends EntidadAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;
    @Column(name = "matricula_id", nullable = false)
    private Long matriculaId;
    @Column(name = "estudiante_id", nullable = false)
    private Long estudianteId;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal valor;
    @Column(length = 500)
    private String observacion;
    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;
    @Version
    @Column(nullable = false)
    private Long version;

    public Calificacion(Evaluacion evaluacion, Long matriculaId, Long estudianteId,
                        BigDecimal valor, String observacion, Long registradoPor) {
        this.evaluacion = evaluacion;
        this.matriculaId = matriculaId;
        this.estudianteId = estudianteId;
        this.valor = valor;
        this.observacion = observacion;
        this.registradoPor = registradoPor;
        this.version = 0L;
        iniciarAuditoria();
    }

    public void actualizar(BigDecimal valor, String observacion, Long registradoPor) {
        this.valor = valor;
        this.observacion = observacion;
        this.registradoPor = registradoPor;
        marcarActualizacion();
    }
}
