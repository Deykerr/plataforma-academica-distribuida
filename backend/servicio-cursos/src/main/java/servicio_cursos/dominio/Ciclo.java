package servicio_cursos.dominio;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ciclos", uniqueConstraints =
        @UniqueConstraint(name = "uk_ciclos_carrera_numero", columnNames = {"carrera_id", "numero"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ciclo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRegistro estado;

    public Ciclo(Carrera carrera, Integer numero, String nombre) {
        this.carrera = carrera;
        this.numero = numero;
        this.nombre = nombre;
        this.estado = EstadoRegistro.ACTIVO;
        iniciarAuditoria();
    }

    public void actualizar(Integer numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoRegistro estado) {
        this.estado = estado;
        marcarActualizacion();
    }

    public boolean estaActivo() {
        return estado == EstadoRegistro.ACTIVO;
    }
}
