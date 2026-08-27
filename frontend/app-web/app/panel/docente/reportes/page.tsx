'use client';

import { useEffect, useMemo, useState } from 'react';
import { Award, BarChart3, CheckCircle2, UsersRound } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ReportActions, ReportHeader } from '@/components/report-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { downloadCsv, reportDate } from '@/lib/reports';
import { Curso, Estudiante, Historial, ResumenSeccion, Seccion } from '@/lib/types';

export default function ReportesDocentePage() {
  const [sections, setSections] = useState<Seccion[]>([]); const [courses, setCourses] = useState<Record<number, Curso>>({}); const [students, setStudents] = useState<Estudiante[]>([]);
  const [selectedSection, setSelectedSection] = useState<number | ''>(''); const [summary, setSummary] = useState<ResumenSeccion | null>(null); const [results, setResults] = useState<Historial[]>([]);
  const [loading, setLoading] = useState(true); const [loadingReport, setLoadingReport] = useState(false); const [error, setError] = useState('');

  useEffect(() => { Promise.all([
    apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones/mias?size=200'), apiFetch<PageResponse<Estudiante>>(API.usuarios, '/api/v1/estudiantes?size=500'),
  ]).then(async ([sectionPage, studentPage]) => { const ids = [...new Set(sectionPage.contenido.map((item) => item.cursoId))]; const values = await Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`))); setSections(sectionPage.contenido); setStudents(studentPage.contenido); setCourses(Object.fromEntries(values.map((item) => [item.id, item]))); setSelectedSection(sectionPage.contenido[0]?.id ?? ''); }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false)); }, []);

  useEffect(() => { if (!selectedSection) return; Promise.resolve().then(() => { setLoadingReport(true); setError(''); return Promise.all([
    apiFetch<ResumenSeccion>(API.evaluaciones, `/api/v1/reportes/secciones/${selectedSection}/resumen`), apiFetch<Historial[]>(API.evaluaciones, `/api/v1/reportes/secciones/${selectedSection}/resultados`),
  ]); }).then(([value, rows]) => { setSummary(value); setResults(rows); }).catch((reason: Error) => setError(reason.message)).finally(() => setLoadingReport(false)); }, [selectedSection]);

  const section = sections.find((item) => item.id === selectedSection); const course = courses[section?.cursoId ?? 0];
  const studentById = useMemo(() => Object.fromEntries(students.map((item) => [item.usuarioId, item])), [students]);
  function exportResults() { downloadCsv(`resultados-${course?.codigo ?? 'curso'}-${section?.codigo ?? ''}.csv`, ['Código', 'Estudiante', 'Correo', 'Matrícula', 'Promedio acumulado', 'Promedio evaluado', 'Avance %', 'Estado'], results.map((item) => { const student = studentById[item.estudianteId]; return [student?.codigo ?? item.estudianteId, student ? `${student.apellidos}, ${student.nombres}` : `Estudiante ${item.estudianteId}`, student?.correo, item.matriculaId, item.promedioAcumulado, item.promedioSobreLoEvaluado, item.ponderacionEvaluada, item.estadoFinal]; })); }

  return <><PageHeading eyebrow="Docencia" title="Reportes de rendimiento" description="Analiza los resultados de tus secciones y entrega una copia en CSV o PDF." />
    {error && <DashboardError message={error} />}
    <section className="report-controls no-print"><div className="form-field"><label>Sección asignada</label><select value={selectedSection} onChange={(event) => setSelectedSection(event.target.value ? Number(event.target.value) : '')}><option value="">Selecciona una sección</option>{sections.map((item) => <option key={item.id} value={item.id}>{item.periodoCodigo} · {courses[item.cursoId]?.codigo ?? item.cursoId} · {item.codigo}</option>)}</select></div></section>
    {loading || loadingReport ? <DashboardSkeleton /> : !summary ? <EmptyState title="No hay reporte disponible" description="Selecciona una sección o registra sus evaluaciones y calificaciones." /> : <section className="report-surface"><ReportHeader title={`${course?.nombre ?? 'Curso'} · Sección ${section?.codigo ?? ''}`} subtitle={`${section?.periodoCodigo ?? ''} · Generado el ${reportDate()}`} />
      <div className="report-toolbar"><h3>Resultados de la sección</h3><ReportActions onExport={exportResults} disabled={!results.length} /></div>
      <div className="report-metrics"><article><BarChart3 /><span>Promedio general</span><strong>{Number(summary.promedioGeneral).toFixed(2)}</strong></article><article><CheckCircle2 /><span>Aprobados</span><strong>{summary.aprobados}</strong></article><article><UsersRound /><span>Calificados</span><strong>{summary.matriculasCalificadas}</strong></article><article><Award /><span>Plan evaluado</span><strong>{Number(summary.ponderacionConfigurada).toFixed(0)}%</strong></article></div>
      {results.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Estudiante</th><th>Código</th><th>Promedio</th><th>Avance</th><th>Estado final</th></tr></thead><tbody>{results.map((item) => { const student = studentById[item.estudianteId]; return <tr key={item.matriculaId}><td><strong>{student ? `${student.apellidos}, ${student.nombres}` : `Estudiante ${item.estudianteId}`}</strong><small>{student?.correo}</small></td><td>{student?.codigo ?? '—'}</td><td><strong>{Number(item.promedioSobreLoEvaluado).toFixed(2)}</strong></td><td>{Number(item.ponderacionEvaluada).toFixed(0)}%</td><td><StatusBadge value={item.estadoFinal} /></td></tr>; })}</tbody></table></div> : <EmptyState title="Aún no hay resultados" description="Registra las notas para que aparezcan en este reporte." />}
    </section>}
  </>;
}
