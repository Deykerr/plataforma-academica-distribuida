export interface Estudiante {
  id: number;
  usuarioId: number;
  correo: string;
  codigo: string;
  nombres: string;
  apellidos: string;
  carreraId: number;
  estado: string;
}

export interface Curso {
  id: number;
  codigo: string;
  nombre: string;
  creditos: number;
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
}
