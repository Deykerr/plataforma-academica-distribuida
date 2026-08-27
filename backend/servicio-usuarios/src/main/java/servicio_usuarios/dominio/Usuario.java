package servicio_usuarios.dominio;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "usuarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Set<RolUsuario> roles = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    public Usuario(String correo, String claveHash, Set<RolUsuario> roles) {
        this.correo = correo;
        this.claveHash = claveHash;
        this.roles = new LinkedHashSet<>(roles);
        this.estado = EstadoUsuario.ACTIVO;
        this.creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        this.actualizadoEn = this.creadoEn;
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public void cambiarEstado(EstadoUsuario nuevoEstado) {
        this.estado = nuevoEstado;
        marcarActualizacion();
    }

    public void cambiarRoles(Set<RolUsuario> nuevosRoles) {
        this.roles.clear();
        this.roles.addAll(nuevosRoles);
        marcarActualizacion();
    }

    public void cambiarClave(String nuevaClaveHash) {
        this.claveHash = nuevaClaveHash;
        marcarActualizacion();
    }

    private void marcarActualizacion() {
        this.actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
