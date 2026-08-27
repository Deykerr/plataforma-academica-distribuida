'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { BookOpenCheck, RefreshCw } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { Feedback, ModuleToolbar } from '@/components/module-ui';
import { useAuth } from '@/context/auth-context';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Matricula, Seccion } from '@/lib/types';

export default function OfertaPage() {
  const { session } = useAuth(); const [sections, setSections] = useState<Seccion[]>([]); const [enrollments, setEnrollments] = useState<Matricula[]>([]); const [courses, setCourses] = useState<Record<number, Curso>>({});
  const [search, setSearch] = useState(''); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [submittingId, setSubmittingId] = useState<number | null>(null); const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null); const [requestId, setRequestId] = useState(0);
  const load = useCallback(() => { Promise.all([apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones?estado=ABIERTA&size=200'), apiFetch<PageResponse<Matricula>>(API.matriculas, '/api/v1/matriculas/mias?size=200')]).then(async ([sectionPage, enrollmentPage]) => {
    const ids = [...new Set(sectionPage.contenido.map((item) => item.cursoId))]; const values = await Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`))); setSections(sectionPage.contenido); setEnrollments(enrollmentPage.contenido); setCourses(Object.fromEntries(values.map((item) => [item.id, item]))); setError('');
  }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false)); }, []);
  useEffect(() => { load(); }, [load, requestId]);
  const filtered = useMemo(() => sections.filter((item) => `${item.periodoCodigo} ${item.codigo} ${courses[item.cursoId]?.codigo} ${courses[item.cursoId]?.nombre}`.toLowerCase().includes(search.toLowerCase())), [courses, search, sections]);
  async function enroll(section: Seccion) { if (!session) return; setSubmittingId(section.id); setFeedback(null); try { await apiFetch(API.matriculas, '/api/v1/matriculas', { method: 'POST', body: JSON.stringify({ estudianteId: session.usuarioId, seccionId: section.id }) }); setFeedback({ type: 'success', message: 'Matrícula registrada correctamente.' }); setLoading(true); setRequestId((value) => value + 1); } catch (reason) { setFeedback({ type: 'error', message: reason instanceof Error ? reason.message : 'No se pudo registrar la matrícula.' }); } finally { setSubmittingId(null); } }
  return <><PageHeading eyebrow="Estudiante" title="Oferta académica" description="Explora las secciones abiertas y matricúlate; el sistema comprobará vacantes, horarios y prerrequisitos." />
    {feedback && <Feedback {...feedback} onClose={() => setFeedback(null)} />}{error && <DashboardError message={error} retry={() => { setLoading(true); setRequestId((value) => value + 1); }} />}
    <section className="module-card"><ModuleToolbar search={search} onSearch={setSearch}><button className="secondary-button" onClick={() => { setLoading(true); setRequestId((value) => value + 1); }}><RefreshCw size={15} />Actualizar</button></ModuleToolbar><div className="module-body">{loading ? <DashboardSkeleton /> : filtered.length === 0 ? <EmptyState title="No hay secciones abiertas" description="La oferta aparecerá cuando el administrador abra las secciones del periodo." /> :
      <div className="catalog-grid">{filtered.map((section) => { const course = courses[section.cursoId]; const enrolled = enrollments.some((item) => item.cursoId === section.cursoId && item.periodoId === section.periodoId && item.estado === 'ACTIVA'); return <article className="catalog-item" key={section.id}><header><span className="section-code">{section.periodoCodigo} · Sección {section.codigo}</span><StatusBadge value={section.estado} /></header><h3>{course?.codigo ?? section.cursoId} · {course?.nombre ?? 'Curso'}</h3><p>{section.horarios.map((item) => `${item.diaSemana} ${item.horaInicio.slice(0,5)}–${item.horaFin.slice(0,5)}`).join(', ')}</p><div className="catalog-meta"><span>{course?.creditos ?? 0} créditos</span><span>{section.vacantesDisponibles} vacantes</span><span>{course?.prerequisitos.length ?? 0} prerrequisitos</span></div>
        {course?.prerequisitos.length ? <p className="prerequisite-copy">Requiere: {course.prerequisitos.map((item) => item.codigo).join(', ')}</p> : null}
        <footer><small>Aula {section.aulaId}</small><button className={enrolled ? 'secondary-button' : 'primary-small'} disabled={enrolled || submittingId === section.id || section.vacantesDisponibles === 0} onClick={() => enroll(section)}><BookOpenCheck size={15} />{enrolled ? 'Ya matriculado' : submittingId === section.id ? 'Validando…' : 'Matricularme'}</button></footer></article>; })}</div>}
    </div></section></>;
}
