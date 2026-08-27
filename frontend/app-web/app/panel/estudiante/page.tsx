'use client';

import { useEffect, useState } from 'react';
import { Award, BookOpen, GraduationCap, RadioTower, Sigma } from 'lucide-react';
import { ContentCard, DashboardError, DashboardSkeleton, EmptyState, MetricCard, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Estudiante, Historial, Matricula } from '@/lib/types';

interface StudentOverview {
  perfil: Estudiante;
  matriculas: PageResponse<Matricula>;
  cursos: Record<number, Curso>;
  historiales: Historial[];
}

export default function EstudiantePage() {
  const [data, setData] = useState<StudentOverview | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [requestId, setRequestId] = useState(0);

  useEffect(() => {
    let active = true;
    Promise.all([
      apiFetch<Estudiante>(API.usuarios, '/api/v1/estudiantes/me'),
      apiFetch<PageResponse<Matricula>>(API.matriculas, '/api/v1/matriculas/mias?size=50'),
    ]).then(async ([perfil, matriculas]) => {
      const ids = [...new Set(matriculas.contenido.map((item) => item.cursoId))];
      const [courses, histories] = await Promise.all([
        Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`))),
        Promise.allSettled(matriculas.contenido.map((item) =>
          apiFetch<Historial>(API.evaluaciones, `/api/v1/historial/matriculas/${item.id}`))),
      ]);
      if (!active) return;
      setData({
        perfil,
        matriculas,
        cursos: Object.fromEntries(courses.map((course) => [course.id, course])),
        historiales: histories.flatMap((result) => result.status === 'fulfilled' ? [result.value] : []),
      });
      setError('');
    }).catch((reason: Error) => active && setError(reason.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [requestId]);

  function retry() { setLoading(true); setRequestId((value) => value + 1); }

  const activeEnrollments = data?.matriculas.contenido.filter((item) => item.estado === 'ACTIVA') ?? [];
  const average = data?.historiales.length
    ? (data.historiales.reduce((sum, item) => sum + Number(item.promedioSobreLoEvaluado), 0) / data.historiales.length).toFixed(1)
    : '—';

  return (
    <>
      <PageHeading eyebrow="Panel de estudiante"
        title={data ? `Hola, ${data.perfil.nombres.split(' ')[0]}` : 'Tu avance académico'}
        description="Consulta tus matrículas, cursos y resultados desde un solo lugar."
        action={<span className="date-badge"><RadioTower size={16} /> Información actualizada</span>} />
      {error && <DashboardError message={error} retry={retry} />}
      {loading ? <DashboardSkeleton /> : data && (
        <>
          <section className="metric-grid four" aria-label="Indicadores del estudiante">
            <MetricCard label="Matrículas activas" value={activeEnrollments.length} note="Cursos del periodo" icon={BookOpen} />
            <MetricCard label="Créditos actuales" value={activeEnrollments.reduce((sum, item) => sum + (data.cursos[item.cursoId]?.creditos ?? 0), 0)} note="Carga académica" icon={Sigma} tone={1} />
            <MetricCard label="Promedio visible" value={average} note="Sobre notas evaluadas" icon={Award} tone={2} />
            <MetricCard label="Cursos aprobados" value={data.historiales.filter((item) => item.estadoFinal === 'APROBADO').length} note="Historial disponible" icon={GraduationCap} tone={3} />
          </section>
          <section className="dashboard-grid">
            <ContentCard eyebrow="Periodo académico" title="Mis matrículas" className="wide-card">
              {data.matriculas.contenido.length === 0 ? <EmptyState title="Aún no tienes matrículas"
                description="Cuando te inscribas en una sección, el curso aparecerá en esta lista." /> : (
                <div className="data-table-wrap"><table className="data-table"><thead><tr>
                  <th>Curso</th><th>Sección</th><th>Periodo</th><th>Estado</th>
                </tr></thead><tbody>{data.matriculas.contenido.map((enrollment) => <tr key={enrollment.id}>
                  <td><strong>{data.cursos[enrollment.cursoId]?.codigo ?? `Curso ${enrollment.cursoId}`}</strong>
                    <small>{data.cursos[enrollment.cursoId]?.nombre ?? 'Información del curso'}</small></td>
                  <td>{enrollment.seccionCodigo}</td><td>{enrollment.periodoCodigo}</td>
                  <td><StatusBadge value={enrollment.estado} /></td>
                </tr>)}</tbody></table></div>
              )}
            </ContentCard>
            <ContentCard eyebrow="Resultados" title="Resumen de notas">
              {data.historiales.length === 0 ? <EmptyState title="Sin notas publicadas"
                description="Tu promedio aparecerá cuando los docentes registren calificaciones." /> : (
                <div className="grade-list">{data.historiales.slice(0, 6).map((history) => (
                  <div key={history.matriculaId}><div><strong>{data.cursos[history.cursoId]?.codigo ?? `Curso ${history.cursoId}`}</strong>
                    <small>{data.cursos[history.cursoId]?.nombre ?? `Matrícula ${history.matriculaId}`}</small></div>
                    <span className="grade-value">{Number(history.promedioSobreLoEvaluado).toFixed(1)}</span>
                    <StatusBadge value={history.estadoFinal} /></div>
                ))}</div>
              )}
            </ContentCard>
          </section>
        </>
      )}
    </>
  );
}
