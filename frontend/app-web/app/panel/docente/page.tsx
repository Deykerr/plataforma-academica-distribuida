'use client';

import { useEffect, useState } from 'react';
import { BookCheck, CalendarDays, ClipboardCheck, RadioTower, UsersRound } from 'lucide-react';
import { ContentCard, DashboardError, DashboardSkeleton, EmptyState, MetricCard, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Evaluacion, Seccion } from '@/lib/types';

interface TeacherOverview {
  secciones: PageResponse<Seccion>;
  evaluaciones: PageResponse<Evaluacion>;
  cursos: Record<number, Curso>;
}

const dayNames: Record<string, string> = {
  LUNES: 'Lun', MARTES: 'Mar', MIERCOLES: 'Mié', JUEVES: 'Jue', VIERNES: 'Vie', SABADO: 'Sáb', DOMINGO: 'Dom',
};

export default function DocentePage() {
  const [data, setData] = useState<TeacherOverview | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [requestId, setRequestId] = useState(0);

  useEffect(() => {
    let active = true;
    Promise.all([
      apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones/mias?size=20'),
      apiFetch<PageResponse<Evaluacion>>(API.evaluaciones, '/api/v1/evaluaciones?size=50'),
    ]).then(async ([secciones, evaluaciones]) => {
      const ids = [...new Set(secciones.contenido.map((item) => item.cursoId))];
      const courses = await Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`)));
      if (!active) return;
      setData({ secciones, evaluaciones, cursos: Object.fromEntries(courses.map((course) => [course.id, course])) });
      setError('');
    }).catch((reason: Error) => active && setError(reason.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [requestId]);

  function retry() { setLoading(true); setRequestId((value) => value + 1); }

  return (
    <>
      <PageHeading eyebrow="Panel de docente" title="Tus secciones y evaluaciones"
        description="Revisa la carga lectiva, el avance de calificaciones y tus próximas fechas."
        action={<span className="date-badge"><RadioTower size={16} /> Sincronizado</span>} />
      {error && <DashboardError message={error} retry={retry} />}
      {loading ? <DashboardSkeleton /> : data && (
        <>
          <section className="metric-grid four" aria-label="Indicadores docentes">
            <MetricCard label="Secciones" value={data.secciones.totalElementos} note="Asignadas a tu cuenta" icon={BookCheck} />
            <MetricCard label="Estudiantes" value={data.secciones.contenido.reduce((sum, item) => sum + item.matriculados, 0)} note="Matrículas visibles" icon={UsersRound} tone={1} />
            <MetricCard label="Evaluaciones" value={data.evaluaciones.totalElementos} note="Componentes creados" icon={ClipboardCheck} tone={2} />
            <MetricCard label="Borradores" value={data.evaluaciones.contenido.filter((item) => item.estado === 'BORRADOR').length} note="Pendientes de publicar" icon={CalendarDays} tone={3} />
          </section>
          <section className="dashboard-grid">
            <ContentCard eyebrow="Carga lectiva" title="Mis secciones" className="wide-card">
              {data.secciones.contenido.length === 0 ? <EmptyState title="No tienes secciones asignadas"
                description="Cuando el administrador te asigne una sección aparecerá en esta lista." /> : (
                <div className="data-table-wrap"><table className="data-table"><thead><tr>
                  <th>Curso y sección</th><th>Horario</th><th>Ocupación</th><th>Estado</th>
                </tr></thead><tbody>{data.secciones.contenido.map((section) => <tr key={section.id}>
                  <td><strong>{data.cursos[section.cursoId]?.codigo ?? `Curso ${section.cursoId}`} · {section.codigo}</strong>
                    <small>{data.cursos[section.cursoId]?.nombre ?? section.periodoCodigo}</small></td>
                  <td>{section.horarios.length ? section.horarios.map((schedule) => `${dayNames[schedule.diaSemana] ?? schedule.diaSemana} ${schedule.horaInicio.slice(0, 5)}`).join(', ') : 'Por definir'}</td>
                  <td>{section.matriculados} / {section.capacidad}</td><td><StatusBadge value={section.estado} /></td>
                </tr>)}</tbody></table></div>
              )}
            </ContentCard>
            <ContentCard eyebrow="Calificaciones" title="Próximas evaluaciones">
              {data.evaluaciones.contenido.length === 0 ? <EmptyState title="Sin evaluaciones"
                description="Los componentes que registres en Evaluaciones aparecerán aquí." /> : (
                <div className="activity-list">{[...data.evaluaciones.contenido]
                  .sort((a, b) => a.fecha.localeCompare(b.fecha)).slice(0, 6).map((evaluation) => (
                    <div key={evaluation.id}><span className="activity-date">{evaluation.fecha.slice(5).replace('-', '/')}</span>
                      <div><strong>{evaluation.nombre}</strong><small>{evaluation.codigo} · {evaluation.ponderacion}%</small></div>
                      <StatusBadge value={evaluation.estado} /></div>
                  ))}</div>
              )}
            </ContentCard>
          </section>
        </>
      )}
    </>
  );
}
