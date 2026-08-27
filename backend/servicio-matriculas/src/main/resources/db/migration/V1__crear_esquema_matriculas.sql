CREATE TABLE periodos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    fecha_inicio_matricula DATE NOT NULL,
    fecha_fin_matricula DATE NOT NULL,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_periodos_fechas CHECK (fecha_inicio < fecha_fin),
    CONSTRAINT ck_periodos_matricula CHECK (fecha_inicio_matricula <= fecha_fin_matricula),
    CONSTRAINT ck_periodos_estado CHECK (estado IN
        ('PLANIFICADO', 'MATRICULA_ABIERTA', 'EN_CURSO', 'FINALIZADO', 'CANCELADO'))
);

CREATE TABLE secciones (
    id BIGSERIAL PRIMARY KEY,
    periodo_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    aula_id BIGINT NOT NULL,
    docente_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    capacidad INTEGER NOT NULL,
    estado VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_secciones_periodo FOREIGN KEY (periodo_id) REFERENCES periodos (id),
    CONSTRAINT uk_secciones_periodo_codigo UNIQUE (periodo_id, codigo),
    CONSTRAINT ck_secciones_capacidad CHECK (capacidad BETWEEN 1 AND 500),
    CONSTRAINT ck_secciones_estado CHECK (estado IN
        ('PLANIFICADA', 'ABIERTA', 'CERRADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'))
);

CREATE TABLE horarios_seccion (
    id BIGSERIAL PRIMARY KEY,
    seccion_id BIGINT NOT NULL,
    dia_semana VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT fk_horarios_seccion FOREIGN KEY (seccion_id) REFERENCES secciones (id) ON DELETE CASCADE,
    CONSTRAINT ck_horarios_horas CHECK (hora_inicio < hora_fin),
    CONSTRAINT ck_horarios_dia CHECK (dia_semana IN
        ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'))
);

CREATE TABLE matriculas (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL,
    seccion_id BIGINT NOT NULL,
    periodo_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    fecha_matricula TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_retiro TIMESTAMP WITH TIME ZONE,
    motivo_retiro VARCHAR(300),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_matriculas_seccion FOREIGN KEY (seccion_id) REFERENCES secciones (id),
    CONSTRAINT fk_matriculas_periodo FOREIGN KEY (periodo_id) REFERENCES periodos (id),
    CONSTRAINT ck_matriculas_estado CHECK (estado IN ('ACTIVA', 'RETIRADA', 'ANULADA', 'COMPLETADA'))
);

CREATE INDEX idx_secciones_periodo ON secciones (periodo_id);
CREATE INDEX idx_secciones_curso ON secciones (curso_id);
CREATE INDEX idx_secciones_docente ON secciones (docente_id);
CREATE INDEX idx_horarios_seccion ON horarios_seccion (seccion_id);
CREATE INDEX idx_matriculas_estudiante ON matriculas (estudiante_id);
CREATE INDEX idx_matriculas_seccion ON matriculas (seccion_id);
CREATE INDEX idx_matriculas_periodo ON matriculas (periodo_id);
CREATE UNIQUE INDEX uk_matricula_activa_seccion
    ON matriculas (estudiante_id, seccion_id) WHERE estado = 'ACTIVA';
CREATE UNIQUE INDEX uk_matricula_activa_curso
    ON matriculas (estudiante_id, periodo_id, curso_id) WHERE estado = 'ACTIVA';
