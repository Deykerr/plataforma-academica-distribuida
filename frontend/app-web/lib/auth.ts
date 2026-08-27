export type Role = 'ADMINISTRADOR' | 'DOCENTE' | 'ESTUDIANTE';

export interface Session {
  token: string;
  tipo: string;
  usuarioId: number;
  correo: string;
  roles: Role[];
  expiresAt: number;
}

interface LoginResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuarioId: number;
  correo: string;
  roles: Role[];
}

export const SESSION_KEY = 'aula-nexus-session';

export function toSession(response: LoginResponse): Session {
  return {
    token: response.token,
    tipo: response.tipo,
    usuarioId: response.usuarioId,
    correo: response.correo,
    roles: response.roles,
    expiresAt: Date.now() + response.expiraEnSegundos * 1000,
  };
}

export function readSession(): Session | null {
  if (typeof window === 'undefined') return null;
  try {
    const stored = window.localStorage.getItem(SESSION_KEY);
    if (!stored) return null;
    const session = JSON.parse(stored) as Session;
    if (!session.token || session.expiresAt <= Date.now()) {
      window.localStorage.removeItem(SESSION_KEY);
      return null;
    }
    return session;
  } catch {
    window.localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function dashboardFor(roles: Role[]): string {
  if (roles.includes('ADMINISTRADOR')) return '/panel/administrador';
  if (roles.includes('DOCENTE')) return '/panel/docente';
  return '/panel/estudiante';
}

export function roleLabel(roles: Role[]): string {
  if (roles.includes('ADMINISTRADOR')) return 'Administrador';
  if (roles.includes('DOCENTE')) return 'Docente';
  return 'Estudiante';
}
