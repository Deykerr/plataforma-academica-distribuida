import { readSession } from './auth';

export const API = {
  usuarios: process.env.NEXT_PUBLIC_USUARIOS_API ?? 'http://localhost:8081',
  cursos: process.env.NEXT_PUBLIC_CURSOS_API ?? 'http://localhost:8082',
  matriculas: process.env.NEXT_PUBLIC_MATRICULAS_API ?? 'http://localhost:8083',
  evaluaciones: process.env.NEXT_PUBLIC_EVALUACIONES_API ?? 'http://localhost:8084',
};

export class ApiError extends Error {
  constructor(message: string, public status: number) {
    super(message);
  }
}

async function errorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json() as Record<string, unknown>;
    const message = String(body.detalle ?? body.mensaje ?? body.message ?? 'No se pudo completar la solicitud.');
    if (body.campos && typeof body.campos === 'object' && !Array.isArray(body.campos)) {
      const labels: Record<string, string> = {
        codigo: 'Código', nombre: 'Nombre', descripcion: 'Descripción', duracionCiclos: 'Duración en ciclos',
        documentoIdentidad: 'Documento', fechaNacimiento: 'Fecha de nacimiento', fechaInicio: 'Fecha de inicio',
        fechaFin: 'Fecha de fin', fechaInicioMatricula: 'Inicio de matrícula', fechaFinMatricula: 'Fin de matrícula',
      };
      const details = Object.entries(body.campos as Record<string, unknown>)
        .map(([field, value]) => `${labels[field] ?? field}: ${String(value)}`).join(' · ');
      if (details) return `${message}. ${details}`;
    }
    return message;
  } catch {
    return response.status === 503
      ? 'Uno de los servicios académicos no está disponible.'
      : 'No se pudo completar la solicitud.';
  }
}

export async function apiFetch<T>(baseUrl: string, path: string, init: RequestInit = {}): Promise<T> {
  const session = readSession();
  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');
  if (init.body) headers.set('Content-Type', 'application/json');
  if (session) headers.set('Authorization', `Bearer ${session.token}`);

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, { ...init, headers });
  } catch {
    throw new ApiError('No se pudo conectar con el backend. Comprueba que Docker esté ejecutándose.', 0);
  }

  if (!response.ok) {
    if (response.status === 401 && typeof window !== 'undefined') {
      window.dispatchEvent(new Event('aula-nexus:unauthorized'));
    }
    throw new ApiError(await errorMessage(response), response.status);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export interface PageResponse<T> {
  contenido: T[];
  pagina: number;
  elementosPorPagina: number;
  totalElementos: number;
  totalPaginas: number;
  ultima: boolean;
}
