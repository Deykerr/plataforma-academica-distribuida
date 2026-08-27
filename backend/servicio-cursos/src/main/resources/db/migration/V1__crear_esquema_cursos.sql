CREATE TABLE carreras (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    duracion_ciclos INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_carreras_duracion CHECK (duracion_ciclos BETWEEN 1 AND 15),
    CONSTRAINT ck_carreras_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE ciclos (
    id BIGSERIAL PRIMARY KEY,
    carrera_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ciclos_carrera FOREIGN KEY (carrera_id) REFERENCES carreras (id),
    CONSTRAINT uk_ciclos_carrera_numero UNIQUE (carrera_id, numero),
    CONSTRAINT ck_ciclos_numero CHECK (numero BETWEEN 1 AND 15),
    CONSTRAINT ck_ciclos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE cursos (
    id BIGSERIAL PRIMARY KEY,
    carrera_id BIGINT NOT NULL,
    ciclo_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    creditos INTEGER NOT NULL,
    horas_teoria INTEGER NOT NULL,
    horas_practica INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cursos_carrera FOREIGN KEY (carrera_id) REFERENCES carreras (id),
    CONSTRAINT fk_cursos_ciclo FOREIGN KEY (ciclo_id) REFERENCES ciclos (id),
    CONSTRAINT ck_cursos_creditos CHECK (creditos BETWEEN 1 AND 10),
    CONSTRAINT ck_cursos_horas_teoria CHECK (horas_teoria BETWEEN 0 AND 20),
    CONSTRAINT ck_cursos_horas_practica CHECK (horas_practica BETWEEN 0 AND 20),
    CONSTRAINT ck_cursos_horas_totales CHECK (horas_teoria + horas_practica > 0),
    CONSTRAINT ck_cursos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE curso_prerequisitos (
    curso_id BIGINT NOT NULL,
    prerequisito_id BIGINT NOT NULL,
    PRIMARY KEY (curso_id, prerequisito_id),
    CONSTRAINT fk_prerequisitos_curso FOREIGN KEY (curso_id) REFERENCES cursos (id),
    CONSTRAINT fk_prerequisitos_requerido FOREIGN KEY (prerequisito_id) REFERENCES cursos (id),
    CONSTRAINT ck_prerequisito_distinto CHECK (curso_id <> prerequisito_id)
);

CREATE TABLE aulas (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    capacidad INTEGER NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_aulas_tipo CHECK (tipo IN ('AULA', 'LABORATORIO')),
    CONSTRAINT ck_aulas_capacidad CHECK (capacidad BETWEEN 1 AND 500),
    CONSTRAINT ck_aulas_estado CHECK (estado IN ('DISPONIBLE', 'MANTENIMIENTO', 'INACTIVA'))
);

CREATE INDEX idx_ciclos_carrera ON ciclos (carrera_id);
CREATE INDEX idx_cursos_carrera ON cursos (carrera_id);
CREATE INDEX idx_cursos_ciclo ON cursos (ciclo_id);
CREATE INDEX idx_cursos_nombre ON cursos (nombre);
CREATE INDEX idx_aulas_tipo_estado ON aulas (tipo, estado);
