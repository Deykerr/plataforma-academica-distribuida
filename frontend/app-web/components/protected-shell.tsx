'use client';

import { useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { Bell, BookOpenCheck, GraduationCap, LayoutDashboard, LogOut, Menu, ShieldCheck, UserRound } from 'lucide-react';
import { Brand } from './brand';
import { LoadingScreen } from './loading-screen';
import { useAuth } from '@/context/auth-context';
import { dashboardFor, Role, roleLabel } from '@/lib/auth';

const requiredRole: Record<string, Role> = {
  '/panel/administrador': 'ADMINISTRADOR',
  '/panel/docente': 'DOCENTE',
  '/panel/estudiante': 'ESTUDIANTE',
};

const roleIcon = { ADMINISTRADOR: ShieldCheck, DOCENTE: BookOpenCheck, ESTUDIANTE: GraduationCap };

export function ProtectedShell({ children }: { children: React.ReactNode }) {
  const { session, ready, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (!ready) return;
    if (!session) {
      router.replace('/login');
      return;
    }
    const expected = requiredRole[pathname];
    if (expected && !session.roles.includes(expected)) router.replace(dashboardFor(session.roles));
  }, [pathname, ready, router, session]);

  if (!ready || !session) return <LoadingScreen message="Protegiendo tu sesión" />;
  const expected = requiredRole[pathname];
  if (expected && !session.roles.includes(expected)) return <LoadingScreen message="Abriendo tu panel" />;

  const mainRole = session.roles.includes('ADMINISTRADOR') ? 'ADMINISTRADOR'
    : session.roles.includes('DOCENTE') ? 'DOCENTE' : 'ESTUDIANTE';
  const RoleIcon = roleIcon[mainRole];

  function signOut() {
    logout();
    router.replace('/login');
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Brand />
        <nav className="sidebar-nav" aria-label="Navegación principal">
          <p>Espacio de trabajo</p>
          <Link href={dashboardFor(session.roles)} className="nav-item active">
            <LayoutDashboard size={19} /> Resumen
          </Link>
          <div className="nav-hint"><RoleIcon size={18} /><span>Vista de {roleLabel(session.roles).toLowerCase()}</span></div>
        </nav>
        <div className="service-status">
          <span className="status-dot" />
          <div><strong>Backend local</strong><small>4 servicios configurados</small></div>
        </div>
        <button className="logout-button" onClick={signOut}><LogOut size={19} /> Cerrar sesión</button>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <button className="icon-button mobile-menu" aria-label="Abrir menú"><Menu size={21} /></button>
          <div className="topbar-context"><span>Plataforma académica</span><strong>{roleLabel(session.roles)}</strong></div>
          <div className="topbar-actions">
            <button className="icon-button" aria-label="Notificaciones"><Bell size={20} /></button>
            <div className="user-chip">
              <span className="avatar"><UserRound size={18} /></span>
              <div><strong>{session.correo.split('@')[0]}</strong><small>{session.correo}</small></div>
            </div>
          </div>
        </header>
        <main className="dashboard-content">{children}</main>
      </div>
    </div>
  );
}
