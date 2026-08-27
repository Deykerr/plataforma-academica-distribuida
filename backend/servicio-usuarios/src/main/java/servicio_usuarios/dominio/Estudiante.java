package servicio_usuarios.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "estudiantes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "documento_identidad", nullable = false, unique = true, length = 20)
    private String documentoIdentidad;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(name = "carrera_id")
    private Long carreraId;

    public Estudiante(Usuario usuario, String codigo, String nombres, String apellidos,
                      String documentoIdentidad, LocalDate fechaNacimiento, String telefono,
                      String direccion, Long carreraId) {
        this.usuario = usuario;
        this.codigo = codigo;
        actualizar(nombres, apellidos, documentoIdentidad, fechaNacimiento, telefono, direccion, carreraId);
    }

    public void actualizar(String nombres, String apellidos, String documentoIdentidad,
                           LocalDate fechaNacimiento, String telefono, String direccion, Long carreraId) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.direccion = direccion;
        this.carreraId = carreraId;
    }
}
