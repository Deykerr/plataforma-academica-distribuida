'use client';

import { useEffect, useState } from 'react';
import { BookOpen, CalendarRange, GraduationCap, RadioTower, UsersRound } from 'lucide-react';
import { ContentCard, DashboardError, DashboardSkeleton, EmptyState, MetricCard, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Periodo, Seccion } from '@/lib/types';

interface AdminOverview {
  estudiantes: number;
  docentes: number;
  cursos: PageResponse<Curso>;
  periodos: PageResponse<Periodo>;
  secciones: PageResponse<Seccion>;
}

export default function AdministradorPage() {
  const [data, setData] = useState<AdminOverview | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [requestId, setRequestId] = useState(0);

  useEffect(() => {
    let active = true;
    Promise.all([
      apiFetch<PageResponse<unknown>>(API.usuarios, '/api/v1/estudiantes?size=1'),
      apiFetch<PageResponse<unknown>>(API.usuarios, '/api/v1/docentes?size=1'),
      apiFetch<PageResponse<Curso>>(API.cursos, '/api/v1/cursos?size=6'),
      apiFetch<PageResponse<Periodo>>(API.matriculas, '/api/v1/periodos?size=5'),
      apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones?size=1'),
    ]).then(([estudiantes, docentes, cursos, periodos, secciones]) => {
      if (!active) return;
      setData({ estudiantes: estudiantes.totalElementos, docentes: docentes.totalElementos, cursos, periodos, secciones });
      setError('');
    }).catch((reason: Error) => active && setError(reason.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [requestId]);

  function retry() { setLoading(true); setRequestId((value) => value + 1); }

  return (
    <>
      <PageHeading eyebrow="Panel de administrador" title="Control académico central"
        description="Una lectura rápida de la comunidad, la oferta y el periodo académico."
        action={<span className="date-badge"><RadioTower size={16} /> Datos en tiempo real</span>} />
      {error && <DashboardError message={error} retry={retry} />}
      {loading ? <DashboardSkeleton /> : data && (
        <>
          <section className="metric-grid four" aria-label="Indicadores administrativos">
            <MetricCard label="Estudiantes" value={data.estudiantes} note="Perfiles registrados" icon={GraduationCap} />
            <MetricCard label="Docentes" value={data.docentes} note="Cuentas docentes" icon={UsersRound} tone={1} />
            <MetricCard label="Cursos" value={data.cursos.totalElementos} note="Oferta académica" icon={BookOpen} tone={2} />
            <MetricCard label="Secciones" value={data.secciones.totalElementos} note="Aperturas registradas" icon={CalendarRange} tone={3} />
          </section>
          <section className="dashboard-grid">
            <ContentCard eyebrow="Calendario" title="Periodos académicos" className="wide-card">
              {data.periodos.contenido.length === 0 ? <EmptyState title="Aún no hay periodos"
                description="Crea el primer periodo desde el Servicio de Matrículas para comenzar la planificación." /> : (
                <div className="data-table-wrap"><table className="data-table"><thead><tr>
                  <th>Periodo</th><th>Inicio</th><th>Fin</th><th>Estado</th>
                </tr></thead><tbody>{data.periodos.contenido.map((periodo) => <tr key={periodo.id}>
                  <td><strong>{periodo.codigo}</strong><small>{periodo.nombre}</small></td>
                  <td>{periodo.fechaInicio}</td><td>{periodo.fechaFin}</td><td><StatusBadge value={periodo.estado} /></td>
                </tr>)}</tbody></table></div>
              )}
            </ContentCard>
            <ContentCard eyebrow="Arquitectura" title="Servicios conectados">
              <div className="service-list">
                {[['Usuarios', '8081'], ['Cursos', '8082'], ['Matrículas', '8083'], ['Evaluaciones', '8084']].map(([name, port]) => (
                  <div key={name}><span className="status-dot" /><div><strong>{name}</strong><small>localhost:{port}</small></div><StatusBadge value="Activo" /></div>
                ))}
              </div>
            </ContentCard>
          </section>
        </>
      )}
    </>
  );
}
