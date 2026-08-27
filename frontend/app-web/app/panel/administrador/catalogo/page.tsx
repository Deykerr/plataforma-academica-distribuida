'use client';

import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { BookOpen, Building2, Layers3, Pencil, Power, RefreshCw, School } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ActionIcon, Feedback, FormActions, Modal, ModuleTabs, ModuleToolbar, RowActions } from '@/components/module-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Aula, Carrera, Ciclo, Curso } from '@/lib/types';

type Tab = 'carreras' | 'ciclos' | 'cursos' | 'aulas';
const singular: Record<Tab, string> = { carreras: 'carrera', ciclos: 'ciclo', cursos: 'curso', aulas: 'aula' };
const createLabel: Record<Tab, string> = { carreras: 'Nueva carrera', ciclos: 'Nuevo ciclo', cursos: 'Nuevo curso', aulas: 'Nueva aula' };

export default function CatalogoPage() {
  const [tab, setTab] = useState<Tab>('carreras');
  const [careers, setCareers] = useState<Carrera[]>([]);
  const [cycles, setCycles] = useState<Ciclo[]>([]);
  const [courses, setCourses] = useState<Curso[]>([]);
  const [classrooms, setClassrooms] = useState<Aula[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Carrera | Ciclo | Curso | Aula | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [requestId, setRequestId] = useState(0);

  const load = useCallback(() => {
    Promise.all([
      apiFetch<PageResponse<Carrera>>(API.cursos, '/api/v1/carreras?size=100'),
      apiFetch<PageResponse<Ciclo>>(API.cursos, '/api/v1/ciclos?size=100'),
      apiFetch<PageResponse<Curso>>(API.cursos, '/api/v1/cursos?size=200'),
      apiFetch<PageResponse<Aula>>(API.cursos, '/api/v1/aulas?size=100'),
    ]).then(([careerPage, cyclePage, coursePage, classroomPage]) => {
      setCareers(careerPage.contenido); setCycles(cyclePage.contenido); setCourses(coursePage.contenido); setClassrooms(classroomPage.contenido); setError('');
    }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false));
  }, []);
  useEffect(() => { load(); }, [load, requestId]);

  const items = { carreras: careers, ciclos: cycles, cursos: courses, aulas: classrooms }[tab];
  const filtered = useMemo(() => items.filter((item) => JSON.stringify(item).toLowerCase().includes(search.toLowerCase())), [items, search]);

  async function saveItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSubmitting(true); setFeedback(null);
    const form = new FormData(event.currentTarget);
    try {
      const path = editing ? `/api/v1/${tab}/${editing.id}` : `/api/v1/${tab}`;
      const method = editing ? 'PUT' : 'POST';
      if (tab === 'carreras') await apiFetch(API.cursos, path, { method, body: JSON.stringify({
        ...(!editing ? { codigo: form.get('codigo') } : {}), nombre: form.get('nombre'), descripcion: form.get('descripcion') || null, duracionCiclos: Number(form.get('duracionCiclos')),
      }) });
      if (tab === 'ciclos') await apiFetch(API.cursos, path, { method, body: JSON.stringify({
        ...(!editing ? { carreraId: Number(form.get('carreraId')) } : {}), numero: Number(form.get('numero')), nombre: form.get('nombre'),
      }) });
      if (tab === 'cursos') await apiFetch(API.cursos, path, { method, body: JSON.stringify({
        carreraId: Number(form.get('carreraId')), cicloId: Number(form.get('cicloId')), ...(!editing ? { codigo: form.get('codigo') } : {}), nombre: form.get('nombre'),
        descripcion: form.get('descripcion') || null, creditos: Number(form.get('creditos')), horasTeoria: Number(form.get('horasTeoria')),
        horasPractica: Number(form.get('horasPractica')), prerequisitoIds: form.getAll('prerequisitoIds').map(Number),
      }) });
      if (tab === 'aulas') await apiFetch(API.cursos, path, { method, body: JSON.stringify({
        ...(!editing ? { codigo: form.get('codigo') } : {}), nombre: form.get('nombre'), tipo: form.get('tipo'), capacidad: Number(form.get('capacidad')), ubicacion: form.get('ubicacion'),
      }) });
      setModalOpen(false); setFeedback({ type: 'success', message: `Registro ${editing ? 'actualizado' : 'creado'} correctamente.` }); setEditing(null); setLoading(true); setRequestId((value) => value + 1);
    } catch (reason) { setFeedback({ type: 'error', message: reason instanceof Error ? reason.message : 'No se pudo crear el registro.' }); }
    finally { setSubmitting(false); }
  }

  async function toggleItem(item: Carrera | Ciclo | Curso | Aula) {
    try {
      if (tab === 'aulas') {
        const classroom = item as Aula;
        await apiFetch(API.cursos, `/api/v1/aulas/${item.id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado: classroom.estado === 'DISPONIBLE' ? 'MANTENIMIENTO' : 'DISPONIBLE' }) });
      } else {
        await apiFetch(API.cursos, `/api/v1/${tab}/${item.id}/estado`, { method: 'PATCH', body: JSON.stringify({ estado: item.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO' }) });
      }
      setFeedback({ type: 'success', message: 'Estado actualizado.' }); setLoading(true); setRequestId((value) => value + 1);
    } catch (reason) { setFeedback({ type: 'error', message: reason instanceof Error ? reason.message : 'No se pudo cambiar el estado.' }); }
  }

  function details(item: Carrera | Ciclo | Curso | Aula) {
    if (tab === 'carreras') { const value = item as Carrera; return { title: `${value.codigo} · ${value.nombre}`, description: value.descripcion || 'Sin descripción', meta: [`${value.duracionCiclos} ciclos`] }; }
    if (tab === 'ciclos') { const value = item as Ciclo; return { title: `${value.carreraCodigo} · Ciclo ${value.numero}`, description: value.nombre, meta: [`Carrera ${value.carreraCodigo}`] }; }
    if (tab === 'cursos') { const value = item as Curso; return { title: `${value.codigo} · ${value.nombre}`, description: value.descripcion || 'Sin descripción', meta: [`${value.creditos} créditos`, `Ciclo ${value.cicloNumero}`, `${value.prerequisitos.length} prerrequisitos`] }; }
    const value = item as Aula; return { title: `${value.codigo} · ${value.nombre}`, description: value.ubicacion, meta: [value.tipo, `Capacidad ${value.capacidad}`] };
  }

  return <>
    <PageHeading eyebrow="Administración" title="Catálogo académico" description="Configura carreras, ciclos, cursos con prerrequisitos y espacios de enseñanza." />
    {feedback && <Feedback {...feedback} onClose={() => setFeedback(null)} />}
    {error && <DashboardError message={error} retry={() => { setLoading(true); setRequestId((value) => value + 1); }} />}
    <section className="module-card">
      <ModuleTabs items={[{ value: 'carreras', label: 'Carreras', count: careers.length }, { value: 'ciclos', label: 'Ciclos', count: cycles.length }, { value: 'cursos', label: 'Cursos', count: courses.length }, { value: 'aulas', label: 'Aulas', count: classrooms.length }]}
        active={tab} onChange={(value) => { setTab(value); setSearch(''); }} />
      <ModuleToolbar search={search} onSearch={setSearch} actionLabel={createLabel[tab]} onAction={() => { setEditing(null); setModalOpen(true); }}>
        <button className="secondary-button" onClick={() => { setLoading(true); setRequestId((value) => value + 1); }}><RefreshCw size={15} />Actualizar</button>
      </ModuleToolbar>
      <div className="module-body">{loading ? <DashboardSkeleton /> : filtered.length === 0 ? <EmptyState title="Sin registros" description="Crea el primer elemento o cambia la búsqueda." /> :
        <div className="catalog-grid">{filtered.map((raw) => { const item = raw as Carrera | Ciclo | Curso | Aula; const info = details(item); return <article className="catalog-item" key={item.id}>
          <header><span className="metric-icon">{tab === 'carreras' ? <School size={19} /> : tab === 'ciclos' ? <Layers3 size={19} /> : tab === 'cursos' ? <BookOpen size={19} /> : <Building2 size={19} />}</span><StatusBadge value={item.estado} /></header>
          <h3>{info.title}</h3><p>{info.description}</p><div className="catalog-meta">{info.meta.map((value) => <span key={value}>{value}</span>)}</div>
          <footer><small>ID {item.id}</small><RowActions><ActionIcon label="Editar registro" onClick={() => { setEditing(item); setModalOpen(true); }}><Pencil size={15} /></ActionIcon><ActionIcon label="Cambiar estado" danger={item.estado === 'ACTIVO' || item.estado === 'DISPONIBLE'} onClick={() => toggleItem(item)}><Power size={15} /></ActionIcon></RowActions></footer>
        </article>; })}</div>}
      </div>
    </section>
    <Modal open={modalOpen} onClose={() => { setModalOpen(false); setEditing(null); }} title={`${editing ? 'Editar' : 'Crear'} ${singular[tab]}`} description={editing ? 'Los códigos de identificación se conservan al actualizar.' : 'Completa los datos obligatorios definidos por el servicio.'} wide={tab === 'cursos'}>
      <form className="entity-form" onSubmit={saveItem}><div className="form-grid">
        {tab === 'carreras' && <>{!editing && <div className="form-field"><label>Código</label><input name="codigo" required minLength={2} maxLength={20} /></div>}<div className="form-field"><label>Nombre</label><input name="nombre" defaultValue={(editing as Carrera | null)?.nombre} required maxLength={120} /></div>
          <div className="form-field"><label>Duración en ciclos</label><input name="duracionCiclos" type="number" defaultValue={(editing as Carrera | null)?.duracionCiclos} min={1} max={15} required /></div><div className="form-field full"><label>Descripción</label><textarea name="descripcion" defaultValue={(editing as Carrera | null)?.descripcion ?? ''} maxLength={500} /></div></>}
        {tab === 'ciclos' && <>{!editing && <div className="form-field"><label>Carrera</label><select name="carreraId" required defaultValue=""><option value="" disabled>Selecciona una carrera</option>{careers.filter((item) => item.estado === 'ACTIVO').map((item) => <option key={item.id} value={item.id}>{item.codigo} · {item.nombre}</option>)}</select></div>}
          <div className="form-field"><label>Número</label><input name="numero" type="number" defaultValue={(editing as Ciclo | null)?.numero} min={1} max={15} required /></div><div className="form-field full"><label>Nombre</label><input name="nombre" defaultValue={(editing as Ciclo | null)?.nombre} required maxLength={80} placeholder="Primer ciclo" /></div></>}
        {tab === 'cursos' && <><div className="form-field"><label>Carrera</label><select name="carreraId" required defaultValue={(editing as Curso | null)?.carreraId ?? ''}><option value="" disabled>Selecciona</option>{careers.filter((item) => item.estado === 'ACTIVO' || item.id === (editing as Curso | null)?.carreraId).map((item) => <option key={item.id} value={item.id}>{item.codigo} · {item.nombre}</option>)}</select></div>
          <div className="form-field"><label>Ciclo</label><select name="cicloId" required defaultValue={(editing as Curso | null)?.cicloId ?? ''}><option value="" disabled>Selecciona</option>{cycles.filter((item) => item.estado === 'ACTIVO' || item.id === (editing as Curso | null)?.cicloId).map((item) => <option key={item.id} value={item.id}>{item.carreraCodigo} · {item.numero} · {item.nombre}</option>)}</select></div>
          {!editing && <div className="form-field"><label>Código</label><input name="codigo" required minLength={2} maxLength={20} /></div>}<div className="form-field"><label>Nombre</label><input name="nombre" defaultValue={(editing as Curso | null)?.nombre} required maxLength={150} /></div>
          <div className="form-field"><label>Créditos</label><input name="creditos" type="number" defaultValue={(editing as Curso | null)?.creditos} min={1} max={10} required /></div><div className="form-field"><label>Horas teóricas</label><input name="horasTeoria" type="number" defaultValue={(editing as Curso | null)?.horasTeoria} min={0} max={20} required /></div>
          <div className="form-field"><label>Horas prácticas</label><input name="horasPractica" type="number" defaultValue={(editing as Curso | null)?.horasPractica} min={0} max={20} required /></div><div className="form-field"><label>Prerrequisitos</label><select name="prerequisitoIds" multiple defaultValue={(editing as Curso | null)?.prerequisitos.map((item) => String(item.id))}>{courses.filter((item) => item.estado === 'ACTIVO' && item.id !== editing?.id).map((item) => <option key={item.id} value={item.id}>{item.codigo} · {item.nombre}</option>)}</select><small>Usa Ctrl para seleccionar varios cursos.</small></div>
          <div className="form-field full"><label>Descripción</label><textarea name="descripcion" defaultValue={(editing as Curso | null)?.descripcion ?? ''} maxLength={500} /></div></>}
        {tab === 'aulas' && <>{!editing && <div className="form-field"><label>Código</label><input name="codigo" required minLength={2} maxLength={20} /></div>}<div className="form-field"><label>Nombre</label><input name="nombre" defaultValue={(editing as Aula | null)?.nombre} required maxLength={100} /></div>
          <div className="form-field"><label>Tipo</label><select name="tipo" defaultValue={(editing as Aula | null)?.tipo ?? 'AULA'}><option value="AULA">Aula</option><option value="LABORATORIO">Laboratorio</option></select></div><div className="form-field"><label>Capacidad</label><input name="capacidad" type="number" defaultValue={(editing as Aula | null)?.capacidad} min={1} max={500} required /></div>
          <div className="form-field full"><label>Ubicación</label><input name="ubicacion" defaultValue={(editing as Aula | null)?.ubicacion} required maxLength={200} /></div></>}
      </div><FormActions submitting={submitting} onCancel={() => { setModalOpen(false); setEditing(null); }} label={editing ? 'Guardar cambios' : 'Crear registro'} /></form>
    </Modal>
  </>;
}
