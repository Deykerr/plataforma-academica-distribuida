'use client';

import { useCallback, useEffect, useState } from 'react';
import { Eye, RefreshCw } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { Modal } from '@/components/module-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Curso, Historial, Matricula } from '@/lib/types';

export default function NotasEstudiantePage() {
  const [histories, setHistories] = useState<Historial[]>([]); const [courses, setCourses] = useState<Record<number, Curso>>({}); const [selected, setSelected] = useState<Historial | null>(null);
  const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [requestId, setRequestId] = useState(0);
  const load = useCallback(() => { apiFetch<PageResponse<Matricula>>(API.matriculas, '/api/v1/matriculas/mias?size=200').then(async (page) => { const ids = [...new Set(page.contenido.map((item) => item.cursoId))]; const [courseValues, historyResults] = await Promise.all([Promise.all(ids.map((id) => apiFetch<Curso>(API.cursos, `/api/v1/cursos/${id}`))), Promise.allSettled(page.contenido.map((item) => apiFetch<Historial>(API.evaluaciones, `/api/v1/historial/matriculas/${item.id}`)))]); setCourses(Object.fromEntries(courseValues.map((item) => [item.id, item]))); setHistories(historyResults.flatMap((item) => item.status === 'fulfilled' ? [item.value] : [])); setError(''); }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false)); }, []);
  useEffect(() => { load(); }, [load, requestId]);
  return <><PageHeading eyebrow="Estudiante" title="Mis notas" description="Revisa promedios ponderados y el detalle de cada evaluación publicada." action={<button className="secondary-button" onClick={() => { setLoading(true); setRequestId((value) => value + 1); }}><RefreshCw size={15} />Actualizar</button>} />
    {error && <DashboardError message={error} retry={() => { setLoading(true); setRequestId((value) => value + 1); }} />}
    <section className="module-card"><div className="module-body notes-body">{loading ? <DashboardSkeleton /> : histories.length === 0 ? <EmptyState title="No hay calificaciones publicadas" description="Las notas aparecerán cuando los docentes publiquen sus evaluaciones." /> :
      <div className="grade-course-grid">{histories.map((item) => <article className="grade-course" key={item.matriculaId}><header><div><p>{courses[item.cursoId]?.codigo ?? item.cursoId}</p><h2>{courses[item.cursoId]?.nombre ?? 'Curso'}</h2></div><StatusBadge value={item.estadoFinal} /></header><div className="grade-main"><strong>{Number(item.promedioSobreLoEvaluado).toFixed(1)}</strong><span>Promedio sobre lo evaluado</span></div><div className="progress-track"><span style={{ width: `${Math.min(100, Number(item.ponderacionEvaluada))}%` }} /></div><div className="grade-footer"><span>{item.ponderacionEvaluada}% evaluado</span><span>{item.calificaciones.filter((grade) => grade.nota !== null).length} notas</span></div><button className="secondary-button" onClick={() => setSelected(item)}><Eye size={14} />Ver detalle</button></article>)}</div>}
    </div></section>
    <Modal open={!!selected} onClose={() => setSelected(null)} title={courses[selected?.cursoId ?? 0]?.nombre ?? 'Detalle de calificaciones'} description={`Promedio acumulado: ${Number(selected?.promedioAcumulado ?? 0).toFixed(2)} · Nota aprobatoria: ${selected?.notaAprobatoria ?? 11}`} wide>
      <div className="entity-form">{selected?.calificaciones.length ? <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Evaluación</th><th>Tipo</th><th>Peso</th><th>Nota</th><th>Aporte</th></tr></thead><tbody>{selected.calificaciones.map((item) => <tr key={item.evaluacionId}><td><strong>{item.codigo} · {item.nombre}</strong><small>{item.observacion}</small></td><td>{item.tipo}</td><td>{item.ponderacion}%</td><td>{item.nota === null ? 'Pendiente' : `${Number(item.nota).toFixed(2)} / ${item.notaMaxima}`}</td><td>{Number(item.aportePonderado).toFixed(2)}</td></tr>)}</tbody></table></div> : <EmptyState title="Sin detalle" description="No hay componentes publicados para este curso." />}</div>
    </Modal></>;
}
