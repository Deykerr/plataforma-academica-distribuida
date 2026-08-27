'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { BarChart3, Bell, BookOpen, BookOpenCheck, CalendarRange, ClipboardCheck, GraduationCap, LayoutDashboard, Library, LogOut, Menu, NotebookTabs, ShieldCheck, UserCircle, UserRound, UsersRound, X } from 'lucide-react';
import { Brand } from './brand';
import { LoadingScreen } from './loading-screen';
import { useAuth } from '@/context/auth-context';
import { dashboardFor, Role, roleLabel } from '@/lib/auth';

const roleIcon = { ADMINISTRADOR: ShieldCheck, DOCENTE: BookOpenCheck, ESTUDIANTE: GraduationCap };

const navigation = {
  ADMINISTRADOR: [
    { href: '/panel/administrador', label: 'Resumen', icon: LayoutDashboard },
    { href: '/panel/administrador/usuarios', label: 'Usuarios', icon: UsersRound },
    { href: '/panel/administrador/catalogo', label: 'Catálogo', icon: Library },
    { href: '/panel/administrador/operacion', label: 'Operación académica', icon: CalendarRange },
    { href: '/panel/administrador/reportes', label: 'Reportes', icon: BarChart3 },
  ],
  DOCENTE: [
    { href: '/panel/docente', label: 'Resumen', icon: LayoutDashboard },
    { href: '/panel/docente/secciones', label: 'Mis secciones', icon: BookOpenCheck },
    { href: '/panel/docente/evaluaciones', label: 'Evaluaciones y notas', icon: ClipboardCheck },
    { href: '/panel/docente/reportes', label: 'Reportes', icon: BarChart3 },
  ],
  ESTUDIANTE: [
    { href: '/panel/estudiante', label: 'Resumen', icon: LayoutDashboard },
    { href: '/panel/estudiante/oferta', label: 'Oferta académica', icon: BookOpen },
    { href: '/panel/estudiante/matriculas', label: 'Mis matrículas', icon: NotebookTabs },
    { href: '/panel/estudiante/notas', label: 'Mis notas', icon: GraduationCap },
    { href: '/panel/estudiante/reportes', label: 'Mi reporte académico', icon: BarChart3 },
    { href: '/panel/estudiante/perfil', label: 'Mi perfil', icon: UserCircle },
  ],
};

function expectedRole(pathname: string): Role | undefined {
  if (pathname.startsWith('/panel/administrador')) return 'ADMINISTRADOR';
  if (pathname.startsWith('/panel/docente')) return 'DOCENTE';
  if (pathname.startsWith('/panel/estudiante')) return 'ESTUDIANTE';
  return undefined;
}

export function ProtectedShell({ children }: { children: React.ReactNode }) {
  const { session, ready, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    if (!ready) return;
    if (!session) {
      router.replace('/login');
      return;
    }
    const expected = expectedRole(pathname);
    if (expected && !session.roles.includes(expected)) router.replace(dashboardFor(session.roles));
  }, [pathname, ready, router, session]);

  if (!ready || !session) return <LoadingScreen message="Protegiendo tu sesión" />;
  const expected = expectedRole(pathname);
  if (expected && !session.roles.includes(expected)) return <LoadingScreen message="Abriendo tu panel" />;

  const mainRole = session.roles.includes('ADMINISTRADOR') ? 'ADMINISTRADOR'
    : session.roles.includes('DOCENTE') ? 'DOCENTE' : 'ESTUDIANTE';
  const RoleIcon = roleIcon[mainRole];
  const items = navigation[mainRole];

  function signOut() {
    logout();
    router.replace('/login');
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`}>
        <button className="sidebar-close" onClick={() => setMenuOpen(false)} aria-label="Cerrar menú"><X size={20} /></button>
        <Brand />
        <nav className="sidebar-nav" aria-label="Navegación principal">
          <p>Espacio de trabajo</p>
          {items.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href || (item.href !== dashboardFor(session.roles) && pathname.startsWith(`${item.href}/`));
            return <Link key={item.href} href={item.href} onClick={() => setMenuOpen(false)} className={`nav-item ${active ? 'active' : ''}`}>
              <Icon size={19} /> {item.label}
            </Link>;
          })}
          <div className="nav-hint"><RoleIcon size={18} /><span>Vista de {roleLabel(session.roles).toLowerCase()}</span></div>
        </nav>
        <div className="service-status">
          <span className="status-dot" />
          <div><strong>Backend local</strong><small>4 servicios configurados</small></div>
        </div>
        <button className="logout-button" onClick={signOut}><LogOut size={19} /> Cerrar sesión</button>
      </aside>
      {menuOpen && <button className="sidebar-overlay" onClick={() => setMenuOpen(false)} aria-label="Cerrar menú" />}

      <div className="app-main">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Abrir menú"><Menu size={21} /></button>
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
