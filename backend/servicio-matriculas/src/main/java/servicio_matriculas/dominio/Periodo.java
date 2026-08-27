package servicio_matriculas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "periodos")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Periodo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fecha_inicio_matricula", nullable = false)
    private LocalDate fechaInicioMatricula;

    @Column(name = "fecha_fin_matricula", nullable = false)
    private LocalDate fechaFinMatricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPeriodo estado;

    public Periodo(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                   LocalDate fechaInicioMatricula, LocalDate fechaFinMatricula) {
        this.estado = EstadoPeriodo.PLANIFICADO;
        actualizar(codigo, nombre, fechaInicio, fechaFin, fechaInicioMatricula, fechaFinMatricula);
        iniciarAuditoria();
    }

    public void actualizar(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                           LocalDate fechaInicioMatricula, LocalDate fechaFinMatricula) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaInicioMatricula = fechaInicioMatricula;
        this.fechaFinMatricula = fechaFinMatricula;
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoPeriodo estado) {
        this.estado = estado;
        marcarActualizacion();
    }
}
