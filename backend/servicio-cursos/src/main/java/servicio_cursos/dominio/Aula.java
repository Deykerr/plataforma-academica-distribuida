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
@Table(name = "aulas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Aula extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAula tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false, length = 200)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoAula estado;

    public Aula(String codigo, String nombre, TipoAula tipo, Integer capacidad, String ubicacion) {
        this.codigo = codigo;
        this.estado = EstadoAula.DISPONIBLE;
        actualizar(nombre, tipo, capacidad, ubicacion);
        iniciarAuditoria();
    }

    public void actualizar(String nombre, TipoAula tipo, Integer capacidad, String ubicacion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
        marcarActualizacion();
    }

    public void cambiarEstado(EstadoAula estado) {
        this.estado = estado;
        marcarActualizacion();
    }

    public boolean estaDisponible() {
        return estado == EstadoAula.DISPONIBLE;
    }
}
