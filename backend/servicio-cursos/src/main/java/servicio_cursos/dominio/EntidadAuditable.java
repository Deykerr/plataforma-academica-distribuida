package servicio_cursos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@MappedSuperclass
public abstract class EntidadAuditable {

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected void iniciarAuditoria() {
        creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        actualizadoEn = creadoEn;
    }

    protected void marcarActualizacion() {
        actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
