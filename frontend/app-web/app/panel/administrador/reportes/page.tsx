'use client';

import { useEffect, useMemo, useState } from 'react';
import { BarChart3, BookOpenCheck, GraduationCap, UsersRound } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ReportActions, ReportHeader } from '@/components/report-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { downloadCsv, reportDate } from '@/lib/reports';
import { Curso, Estudiante, Historial, Periodo, ResumenPeriodo, ResumenSeccion, Seccion } from '@/lib/types';

export default function ReportesAdministradorPage() {
  const [periods, setPeriods] = useState<Periodo[]>([]);
  const [sections, setSections] = useState<Seccion[]>([]);
  const [courses, setCourses] = useState<Curso[]>([]);
  const [students, setStudents] = useState<Estudiante[]>([]);
  const [selectedPeriod, setSelectedPeriod] = useState<number | ''>('');
  const [selectedSection, setSelectedSection] = useState<number | ''>('');
  const [periodSummary, setPeriodSummary] = useState<ResumenPeriodo | null>(null);
  const [occupancy, setOccupancy] = useState<Seccion[]>([]);
  const [sectionSummary, setSectionSummary] = useState<ResumenSeccion | null>(null);
  const [results, setResults] = useState<Historial[]>([]);
  const [loadingBase, setLoadingBase] = useState(true);
  const [loadingPeriod, setLoadingPeriod] = useState(false);
  const [loadingSection, setLoadingSection] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      apiFetch<PageResponse<Periodo>>(API.matriculas, '/api/v1/periodos?size=100'),
      apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones?size=500'),
      apiFetch<PageResponse<Curso>>(API.cursos, '/api/v1/cursos?size=500'),
      apiFetch<PageResponse<Estudiante>>(API.usuarios, '/api/v1/estudiantes?size=500'),
    ]).then(([periodPage, sectionPage, coursePage, studentPage]) => {
      setPeriods(periodPage.contenido); setSections(sectionPage.contenido); setCourses(coursePage.contenido); setStudents(studentPage.contenido);
      setSelectedPeriod(periodPage.contenido.find((item) => item.matriculaVigente)?.id ?? periodPage.contenido[0]?.id ?? '');
    }).catch((reason: Error) => setError(reason.message)).finally(() => setLoadingBase(false));
  }, []);

  const periodSections = useMemo(() => sections.filter((item) => item.periodoId === selectedPeriod), [sections, selectedPeriod]);
  const courseById = useMemo(() => Object.fromEntries(courses.map((item) => [item.id, item])), [courses]);
  const studentById = useMemo(() => Object.fromEntries(students.map((item) => [item.usuarioId, item])), [students]);

  useEffect(() => {
    if (!selectedPeriod) return;
    Promise.resolve().then(() => { setLoadingPeriod(true); setError(''); return Promise.all([
      apiFetch<ResumenPeriodo>(API.matriculas, `/api/v1/reportes/periodos/${selectedPeriod}/resumen`),
      apiFetch<Seccion[]>(API.matriculas, `/api/v1/reportes/periodos/${selectedPeriod}/ocupacion`),
    ]); }).then(([summary, rows]) => {
      setPeriodSummary(summary); setOccupancy(rows);
      setSelectedSection((current) => rows.some((item) => item.id === current) ? current : rows[0]?.id ?? '');
    }).catch((reason: Error) => setError(reason.message)).finally(() => setLoadingPeriod(false));
  }, [selectedPeriod]);

  useEffect(() => {
    if (!selectedSection) return;
    Promise.resolve().then(() => { setLoadingSection(true); setError(''); return Promise.all([
      apiFetch<ResumenSeccion>(API.evaluaciones, `/api/v1/reportes/secciones/${selectedSection}/resumen`),
      apiFetch<Historial[]>(API.evaluaciones, `/api/v1/reportes/secciones/${selectedSection}/resultados`),
    ]); }).then(([summary, rows]) => { setSectionSummary(summary); setResults(rows); })
      .catch((reason: Error) => setError(reason.message)).finally(() => setLoadingSection(false));
  }, [selectedSection]);

  const selectedPeriodData = periods.find((item) => item.id === selectedPeriod);
  const selectedSectionData = periodSections.find((item) => item.id === selectedSection);

  function exportOccupancy() {
    downloadCsv(`ocupacion-${selectedPeriodData?.codigo ?? 'periodo'}.csv`,
      ['Periodo', 'Curso', 'Sección', 'Estado', 'Capacidad', 'Matriculados', 'Vacantes', 'Ocupación %'],
      occupancy.map((item) => [item.periodoCodigo, courseById[item.cursoId]?.codigo ?? item.cursoId, item.codigo, item.estado, item.capacidad, item.matriculados, item.vacantesDisponibles, item.capacidad ? ((item.matriculados / item.capacidad) * 100).toFixed(2) : 0]));
  }

  function exportResults() {
    downloadCsv(`resultados-${courseById[selectedSectionData?.cursoId ?? 0]?.codigo ?? 'seccion'}-${selectedSectionData?.codigo ?? ''}.csv`,
      ['Matrícula', 'Estudiante', 'Curso', 'Sección', 'Promedio acumulado', 'Promedio evaluado', 'Avance %', 'Estado final'],
      results.map((item) => { const student = studentById[item.estudianteId]; return [item.matriculaId, student ? `${student.codigo} · ${student.apellidos}, ${student.nombres}` : item.estudianteId, courseById[item.cursoId]?.codigo ?? item.cursoId, selectedSectionData?.codigo, item.promedioAcumulado, item.promedioSobreLoEvaluado, item.ponderacionEvaluada, item.estadoFinal]; }));
  }

  return <>
    <PageHeading eyebrow="Administración" title="Reportes académicos" description="Consulta indicadores de matrícula y rendimiento; exporta los datos a CSV o guárdalos como PDF." />
    {error && <DashboardError message={error} />}
    <section className="report-controls no-print"><div className="form-field"><label>Periodo académico</label><select value={selectedPeriod} onChange={(event) => setSelectedPeriod(event.target.value ? Number(event.target.value) : '')}><option value="">Selecciona un periodo</option>{periods.map((item) => <option key={item.id} value={item.id}>{item.codigo} · {item.nombre}</option>)}</select></div>
      <div className="form-field"><label>Sección para resultados</label><select value={selectedSection} onChange={(event) => setSelectedSection(event.target.value ? Number(event.target.value) : '')}><option value="">Selecciona una sección</option>{periodSections.map((item) => <option key={item.id} value={item.id}>{courseById[item.cursoId]?.codigo ?? item.cursoId} · {item.codigo}</option>)}</select></div></section>

    {loadingBase || loadingPeriod ? <DashboardSkeleton /> : !periodSummary ? <EmptyState title="Selecciona un periodo" description="El reporte se generará con la información registrada en matrículas." /> : <section className="report-surface">
      <ReportHeader title={`Ocupación del periodo ${periodSummary.periodoCodigo}`} subtitle={`Generado el ${reportDate()}`} />
      <div className="report-toolbar"><h3>Resumen de matrículas</h3><ReportActions onExport={exportOccupancy} disabled={!occupancy.length} /></div>
      <div className="report-metrics"><article><UsersRound /><span>Estudiantes únicos</span><strong>{periodSummary.estudiantesUnicos}</strong></article><article><BookOpenCheck /><span>Matrículas activas</span><strong>{periodSummary.matriculasActivas}</strong></article><article><BarChart3 /><span>Ocupación total</span><strong>{Number(periodSummary.porcentajeOcupacion).toFixed(1)}%</strong></article><article><GraduationCap /><span>Vacantes</span><strong>{periodSummary.vacantesDisponibles}</strong></article></div>
      <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Curso y sección</th><th>Estado</th><th>Capacidad</th><th>Matriculados</th><th>Vacantes</th><th>Ocupación</th></tr></thead><tbody>{occupancy.map((item) => <tr key={item.id}><td><strong>{courseById[item.cursoId]?.codigo ?? item.cursoId} · {item.codigo}</strong><small>{courseById[item.cursoId]?.nombre}</small></td><td><StatusBadge value={item.estado} /></td><td>{item.capacidad}</td><td>{item.matriculados}</td><td>{item.vacantesDisponibles}</td><td>{item.capacidad ? ((item.matriculados / item.capacidad) * 100).toFixed(1) : 0}%</td></tr>)}</tbody></table></div>

      <div className="report-divider" />
      <div className="report-toolbar"><div><h3>Rendimiento por sección</h3><p>{selectedSectionData ? `${courseById[selectedSectionData.cursoId]?.nombre ?? 'Curso'} · Sección ${selectedSectionData.codigo}` : 'Sin sección seleccionada'}</p></div><ReportActions onExport={exportResults} disabled={!results.length} /></div>
      {loadingSection ? <DashboardSkeleton /> : !sectionSummary ? <EmptyState title="Sin resultados" description="Selecciona una sección con evaluaciones publicadas." /> : <><div className="report-metrics compact"><article><span>Promedio general</span><strong>{Number(sectionSummary.promedioGeneral).toFixed(2)}</strong></article><article><span>Aprobados</span><strong>{sectionSummary.aprobados}</strong></article><article><span>Desaprobados</span><strong>{sectionSummary.desaprobados}</strong></article><article><span>Plan evaluado</span><strong>{Number(sectionSummary.ponderacionConfigurada).toFixed(0)}%</strong></article></div>
        {results.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Matrícula</th><th>Estudiante</th><th>Promedio</th><th>Avance</th><th>Estado final</th></tr></thead><tbody>{results.map((item) => { const student = studentById[item.estudianteId]; return <tr key={item.matriculaId}><td>#{item.matriculaId}</td><td><strong>{student ? `${student.apellidos}, ${student.nombres}` : `Estudiante ${item.estudianteId}`}</strong><small>{student?.codigo}</small></td><td><strong>{Number(item.promedioSobreLoEvaluado).toFixed(2)}</strong></td><td>{Number(item.ponderacionEvaluada).toFixed(0)}%</td><td><StatusBadge value={item.estadoFinal} /></td></tr>; })}</tbody></table></div> : <EmptyState title="Aún no hay matrículas calificadas" description="El resumen se completará cuando se registren notas." />}</>}
    </section>}
  </>;
}
