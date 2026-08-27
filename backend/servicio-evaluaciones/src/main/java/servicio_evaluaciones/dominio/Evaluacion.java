package servicio_evaluaciones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Evaluacion extends EntidadAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "seccion_id", nullable = false)
    private Long seccionId;
    @Column(name = "periodo_id", nullable = false)
    private Long periodoId;
    @Column(name = "curso_id", nullable = false)
    private Long cursoId;
    @Column(name = "docente_id", nullable = false)
    private Long docenteId;
    @Column(nullable = false, length = 20)
    private String codigo;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvaluacion tipo;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ponderacion;
    @Column(name = "nota_maxima", nullable = false, precision = 5, scale = 2)
    private BigDecimal notaMaxima;
    @Column(nullable = false)
    private LocalDate fecha;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvaluacion estado;
    @Version
    @Column(nullable = false)
    private Long version;

    public Evaluacion(Long seccionId, Long periodoId, Long cursoId, Long docenteId,
                      String codigo, String nombre, TipoEvaluacion tipo,
                      BigDecimal ponderacion, BigDecimal notaMaxima, LocalDate fecha) {
        this.seccionId = seccionId;
        this.periodoId = periodoId;
        this.cursoId = cursoId;
        this.docenteId = docenteId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ponderacion = ponderacion;
        this.notaMaxima = notaMaxima;
        this.fecha = fecha;
        this.estado = EstadoEvaluacion.BORRADOR;
        this.version = 0L;
        iniciarAuditoria();
    }

    public void actualizar(String codigo, String nombre, TipoEvaluacion tipo,
                           BigDecimal ponderacion, BigDecimal notaMaxima, LocalDate fecha) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ponderacion = ponderacion;
        this.notaMaxima = notaMaxima;
        this.fecha = fecha;
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoEvaluacion estado) {
        this.estado = estado;
        marcarActualizacion();
    }
}
