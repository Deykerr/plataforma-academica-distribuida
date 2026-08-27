export interface Estudiante {
  id: number;
  usuarioId: number;
  correo: string;
  codigo: string;
  nombres: string;
  apellidos: string;
  documentoIdentidad: string;
  fechaNacimiento: string;
  telefono: string | null;
  direccion: string | null;
  carreraId: number;
  estado: string;
}

export interface Docente {
  id: number;
  usuarioId: number;
  correo: string;
  codigo: string;
  nombres: string;
  apellidos: string;
  documentoIdentidad: string;
  especialidad: string;
  telefono: string | null;
  estado: string;
}

export interface Carrera {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  duracionCiclos: number;
  estado: string;
}

export interface Ciclo {
  id: number;
  carreraId: number;
  carreraCodigo: string;
  numero: number;
  nombre: string;
  estado: string;
}

export interface Curso {
  id: number;
  carreraId: number;
  carreraCodigo: string;
  cicloId: number;
  cicloNumero: number;
  codigo: string;
  nombre: string;
  creditos: number;
  descripcion: string | null;
  horasTeoria: number;
  horasPractica: number;
  prerequisitos: { id: number; codigo: string; nombre: string; creditos: number }[];
  estado: string;
}

export interface Aula {
  id: number;
  codigo: string;
  nombre: string;
  tipo: string;
  capacidad: number;
  ubicacion: string;
  estado: string;
}

export interface Periodo {
  id: number;
  codigo: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  fechaInicioMatricula: string;
  fechaFinMatricula: string;
  estado: string;
  matriculaVigente: boolean;
}

export interface Horario {
  id: number;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
}

export interface Seccion {
  id: number;
  periodoId: number;
  periodoCodigo: string;
  cursoId: number;
  aulaId: number;
  docenteId: number;
  codigo: string;
  capacidad: number;
  matriculados: number;
  vacantesDisponibles: number;
  estado: string;
  horarios: Horario[];
}

export interface Matricula {
  id: number;
  estudianteId: number;
  seccionId: number;
  seccionCodigo: string;
  periodoId: number;
  periodoCodigo: string;
  cursoId: number;
  fechaMatricula: string;
  estado: string;
}

export interface Evaluacion {
  id: number;
  seccionId: number;
  periodoId: number;
  cursoId: number;
  docenteId: number;
  codigo: string;
  nombre: string;
  tipo: string;
  ponderacion: number;
  notaMaxima: number;
  fecha: string;
  estado: string;
}

export interface Historial {
  matriculaId: number;
  estudianteId: number;
  seccionId: number;
  periodoId: number;
  cursoId: number;
  promedioAcumulado: number;
  promedioSobreLoEvaluado: number;
  notaAprobatoria: number;
  estadoFinal: string;
  ponderacionConfigurada: number;
  ponderacionEvaluada: number;
  calificaciones: {
    evaluacionId: number;
    codigo: string;
    nombre: string;
    tipo: string;
    ponderacion: number;
    notaMaxima: number;
    nota: number | null;
    aportePonderado: number;
    observacion: string | null;
  }[];
}

export interface Calificacion {
  id: number;
  evaluacionId: number;
  evaluacionCodigo: string;
  evaluacionNombre: string;
  tipo: string;
  estadoEvaluacion: string;
  matriculaId: number;
  estudianteId: number;
  valor: number;
  notaMaxima: number;
  ponderacion: number;
  observacion: string | null;
}

export interface ResumenPeriodo {
  periodoId: number;
  periodoCodigo: string;
  totalSecciones: number;
  matriculasActivas: number;
  estudiantesUnicos: number;
  capacidadTotal: number;
  vacantesDisponibles: number;
  porcentajeOcupacion: number;
}

export interface ResumenSeccion {
  seccionId: number;
  periodoId: number;
  cursoId: number;
  evaluacionesOficiales: number;
  ponderacionConfigurada: number;
  matriculasCalificadas: number;
  resultadosCompletos: number;
  aprobados: number;
  desaprobados: number;
  promedioGeneral: number;
}
