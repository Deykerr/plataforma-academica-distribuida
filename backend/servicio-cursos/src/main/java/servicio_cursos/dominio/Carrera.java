package servicio_cursos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "carreras")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carrera extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "duracion_ciclos", nullable = false)
    private Integer duracionCiclos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRegistro estado;

    public Carrera(String codigo, String nombre, String descripcion, Integer duracionCiclos) {
        this.codigo = codigo;
        this.estado = EstadoRegistro.ACTIVO;
        actualizar(nombre, descripcion, duracionCiclos);
        iniciarAuditoria();
    }

    public void actualizar(String nombre, String descripcion, Integer duracionCiclos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionCiclos = duracionCiclos;
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoRegistro estado) {
        this.estado = estado;
        marcarActualizacion();
    }

    public boolean estaActiva() {
        return estado == EstadoRegistro.ACTIVO;
    }
}
