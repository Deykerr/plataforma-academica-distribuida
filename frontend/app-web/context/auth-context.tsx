'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { API, ApiError } from '@/lib/api';
import { readSession, Session, SESSION_KEY, toSession } from '@/lib/auth';

interface AuthContextValue {
  session: Session | null;
  ready: boolean;
  login: (correo: string, clave: string) => Promise<Session>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [ready, setReady] = useState(false);

  const logout = useCallback(() => {
    window.localStorage.removeItem(SESSION_KEY);
    setSession(null);
  }, []);

  useEffect(() => {
    const initialization = window.setTimeout(() => {
      setSession(readSession());
      setReady(true);
    }, 0);
    window.addEventListener('aula-nexus:unauthorized', logout);
    return () => {
      window.clearTimeout(initialization);
      window.removeEventListener('aula-nexus:unauthorized', logout);
    };
  }, [logout]);

  const login = useCallback(async (correo: string, clave: string) => {
    let response: Response;
    try {
      response = await fetch(`${API.usuarios}/api/v1/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ correo, clave }),
      });
    } catch {
      throw new ApiError('No se pudo conectar con el Servicio de Usuarios.', 0);
    }
    if (!response.ok) {
      throw new ApiError(
        response.status === 401 ? 'Correo o contraseña incorrectos.' : 'No fue posible iniciar sesión.',
        response.status,
      );
    }
    const nextSession = toSession(await response.json());
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession));
    setSession(nextSession);
    return nextSession;
  }, []);

  const value = useMemo(() => ({ session, ready, login, logout }), [login, logout, ready, session]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return context;
}
