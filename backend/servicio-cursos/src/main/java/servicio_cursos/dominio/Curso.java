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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "cursos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curso extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private Ciclo ciclo;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer creditos;

    @Column(name = "horas_teoria", nullable = false)
    private Integer horasTeoria;

    @Column(name = "horas_practica", nullable = false)
    private Integer horasPractica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRegistro estado;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "curso_prerequisitos",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisito_id"))
    private Set<Curso> prerequisitos = new LinkedHashSet<>();

    public Curso(Carrera carrera, Ciclo ciclo, String codigo, String nombre, String descripcion,
                 Integer creditos, Integer horasTeoria, Integer horasPractica) {
        this.carrera = carrera;
        this.ciclo = ciclo;
        this.codigo = codigo;
        this.estado = EstadoRegistro.ACTIVO;
        actualizar(carrera, ciclo, nombre, descripcion, creditos, horasTeoria, horasPractica);
        iniciarAuditoria();
    }

    public void actualizar(Carrera carrera, Ciclo ciclo, String nombre, String descripcion,
                           Integer creditos, Integer horasTeoria, Integer horasPractica) {
        this.carrera = carrera;
        this.ciclo = ciclo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creditos = creditos;
        this.horasTeoria = horasTeoria;
        this.horasPractica = horasPractica;
        marcarActualizacion();
    }

    public void cambiarPrerequisitos(Set<Curso> cursos) {
        prerequisitos.clear();
        prerequisitos.addAll(cursos);
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
