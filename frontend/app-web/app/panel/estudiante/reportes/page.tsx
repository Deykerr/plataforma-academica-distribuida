'use client';

import { useEffect, useMemo, useState } from 'react';
import { Award, BookOpenCheck, CheckCircle2, GraduationCap } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ReportActions, ReportHeader } from '@/components/report-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { downloadCsv, reportDate } from '@/lib/reports';
import { Curso, Estudiante, Historial, Matricula } from '@/lib/types';

export default function ReporteEstudiantePage() {
  const [profile, setProfile] = useState<Estudiante | null>(null); const [enrollments, setEnrollments] = useState<Matricula[]>([]); const [histories, setHistories] = useState<Historial[]>([]); const [courses, setCourses] = useState<Record<number, Curso>>({});
  const [loading, setLoading] = useState(true); const [error, setError] = useState('');
  useEffect(() => { Promise.all([apiFetch<Estudiante>(API.usuarios, '/api/v1/estudiantes/me'), apiFetch<PageResponse<Matricula>>(API.matriculas, '/api/v1/matriculas/mias?size=500')]).then(async ([student, page]) => {
    const ids = [...new Set(page.contenido.map((item) => item.cursoId))]; const [courseValues, historyValues] = await Promise.all([Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`))), Promise.allSettled(page.contenido.map((item) => apiFetch<Historial>(API.evaluaciones, `/api/v1/historial/matriculas/${item.id}`)))]);
    setProfile(student); setEnrollments(page.contenido); setCourses(Object.fromEntries(courseValues.map((item) => [item.id, item]))); setHistories(historyValues.flatMap((item) => item.status === 'fulfilled' ? [item.value] : []));
  }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false)); }, []);
  const approved = histories.filter((item) => item.estadoFinal === 'APROBADO').length;
  const weightedAverage = useMemo(() => histories.length ? histories.reduce((sum, item) => sum + Number(item.promedioSobreLoEvaluado), 0) / histories.length : 0, [histories]);
  function exportHistory() { downloadCsv(`historial-${profile?.codigo ?? 'estudiante'}.csv`, ['Periodo', 'Curso', 'Nombre', 'Créditos', 'Matrícula', 'Promedio', 'Avance %', 'Estado'], histories.map((item) => { const enrollment = enrollments.find((value) => value.id === item.matriculaId); const course = courses[item.cursoId]; return [enrollment?.periodoCodigo, course?.codigo ?? item.cursoId, course?.nombre, course?.creditos, item.matriculaId, item.promedioSobreLoEvaluado, item.ponderacionEvaluada, item.estadoFinal]; })); }
  return <><PageHeading eyebrow="Estudiante" title="Mi reporte académico" description="Consulta, descarga o imprime una constancia de tu avance académico actual." />
    {error && <DashboardError message={error} />}
    {loading ? <DashboardSkeleton /> : !profile ? <EmptyState title="No se encontró tu perfil" description="Solicita al administrador que revise tu cuenta." /> : <section className="report-surface"><ReportHeader title={`Historial académico · ${profile.nombres} ${profile.apellidos}`} subtitle={`${profile.codigo} · ${profile.correo} · Generado el ${reportDate()}`} />
      <div className="report-toolbar"><div><h3>Resumen del estudiante</h3><p>Documento: {profile.documentoIdentidad}</p></div><ReportActions onExport={exportHistory} disabled={!histories.length} /></div>
      <div className="report-metrics"><article><BookOpenCheck /><span>Cursos calificados</span><strong>{histories.length}</strong></article><article><CheckCircle2 /><span>Cursos aprobados</span><strong>{approved}</strong></article><article><Award /><span>Promedio referencial</span><strong>{weightedAverage.toFixed(2)}</strong></article><article><GraduationCap /><span>Matrículas registradas</span><strong>{enrollments.length}</strong></article></div>
      {histories.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Periodo</th><th>Curso</th><th>Créditos</th><th>Promedio</th><th>Avance</th><th>Resultado</th></tr></thead><tbody>{histories.map((item) => { const enrollment = enrollments.find((value) => value.id === item.matriculaId); const course = courses[item.cursoId]; return <tr key={item.matriculaId}><td>{enrollment?.periodoCodigo ?? '—'}</td><td><strong>{course?.codigo ?? item.cursoId} · {course?.nombre ?? 'Curso'}</strong><small>Matrícula #{item.matriculaId}</small></td><td>{course?.creditos ?? '—'}</td><td><strong>{Number(item.promedioSobreLoEvaluado).toFixed(2)}</strong></td><td>{Number(item.ponderacionEvaluada).toFixed(0)}%</td><td><StatusBadge value={item.estadoFinal} /></td></tr>; })}</tbody></table></div> : <EmptyState title="Sin historial calificable" description="Los cursos aparecerán cuando existan evaluaciones publicadas." />}
    </section>}
  </>;
}
