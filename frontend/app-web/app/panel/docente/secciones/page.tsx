'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { ClipboardCheck, RefreshCw } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ModuleToolbar } from '@/components/module-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Seccion } from '@/lib/types';

export default function SeccionesDocentePage() {
  const [sections, setSections] = useState<Seccion[]>([]); const [courses, setCourses] = useState<Record<number, Curso>>({});
  const [search, setSearch] = useState(''); const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [requestId, setRequestId] = useState(0);
  const load = useCallback(() => { apiFetch<PageResponse<Seccion>>(API.matriculas, '/api/v1/secciones/mias?size=100').then(async (page) => {
    const ids = [...new Set(page.contenido.map((item) => item.cursoId))]; const values = await Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`)));
    setSections(page.contenido); setCourses(Object.fromEntries(values.map((item) => [item.id, item]))); setError('');
  }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false)); }, []);
  useEffect(() => { load(); }, [load, requestId]);
  const filtered = useMemo(() => sections.filter((item) => `${item.periodoCodigo} ${item.codigo} ${courses[item.cursoId]?.codigo} ${courses[item.cursoId]?.nombre}`.toLowerCase().includes(search.toLowerCase())), [courses, search, sections]);
  return <><PageHeading eyebrow="Docencia" title="Mis secciones" description="Consulta horarios, estudiantes matriculados y el estado de cada curso asignado." />
    {error && <DashboardError message={error} retry={() => { setLoading(true); setRequestId((value) => value + 1); }} />}
    <section className="module-card"><ModuleToolbar search={search} onSearch={setSearch}><button className="secondary-button" onClick={() => { setLoading(true); setRequestId((value) => value + 1); }}><RefreshCw size={15} />Actualizar</button></ModuleToolbar>
      <div className="module-body">{loading ? <DashboardSkeleton /> : filtered.length === 0 ? <EmptyState title="Sin secciones asignadas" description="Las secciones aparecerán cuando el administrador te asigne como docente." /> :
        <div className="catalog-grid">{filtered.map((item) => <article className="catalog-item" key={item.id}><header><span className="section-code">{item.periodoCodigo} · {item.codigo}</span><StatusBadge value={item.estado} /></header>
          <h3>{courses[item.cursoId]?.codigo ?? `Curso ${item.cursoId}`} · {courses[item.cursoId]?.nombre ?? 'Curso asignado'}</h3>
          <p>{item.horarios.length ? item.horarios.map((schedule) => `${schedule.diaSemana} ${schedule.horaInicio.slice(0,5)}–${schedule.horaFin.slice(0,5)}`).join(', ') : 'Horario por definir'}</p>
          <div className="catalog-meta"><span>{item.matriculados} estudiantes</span><span>{item.vacantesDisponibles} vacantes</span><span>Aula {item.aulaId}</span></div>
          <footer><small>Capacidad {item.capacidad}</small><Link className="primary-small" href={`/panel/docente/evaluaciones?seccionId=${item.id}`}><ClipboardCheck size={15} />Gestionar notas</Link></footer>
        </article>)}</div>}
      </div></section></>;
}
