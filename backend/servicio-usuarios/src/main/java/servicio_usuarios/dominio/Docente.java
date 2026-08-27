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

@Getter
@Entity
@Table(name = "docentes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Docente {

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

    @Column(nullable = false, length = 120)
    private String especialidad;

    @Column(length = 20)
    private String telefono;

    public Docente(Usuario usuario, String codigo, String nombres, String apellidos,
                   String documentoIdentidad, String especialidad, String telefono) {
        this.usuario = usuario;
        this.codigo = codigo;
        actualizar(nombres, apellidos, documentoIdentidad, especialidad, telefono);
    }

    public void actualizar(String nombres, String apellidos, String documentoIdentidad,
                           String especialidad, String telefono) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.especialidad = especialidad;
        this.telefono = telefono;
    }
}
