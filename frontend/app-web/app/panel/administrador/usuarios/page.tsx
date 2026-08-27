'use client';

import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { Pencil, RefreshCw, UserRoundCheck, UserRoundX } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading, StatusBadge } from '@/components/dashboard-ui';
import { ActionIcon, Feedback, FormActions, Modal, ModuleTabs, ModuleToolbar, RowActions } from '@/components/module-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Carrera, Docente, Estudiante } from '@/lib/types';

type Tab = 'estudiantes' | 'docentes';

export default function UsuariosPage() {
  const [tab, setTab] = useState<Tab>('estudiantes');
  const [students, setStudents] = useState<Estudiante[]>([]);
  const [teachers, setTeachers] = useState<Docente[]>([]);
  const [careers, setCareers] = useState<Carrera[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Estudiante | Docente | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [requestId, setRequestId] = useState(0);

  const load = useCallback(() => {
    Promise.all([
      apiFetch<PageResponse<Estudiante>>(API.usuarios, '/api/v1/estudiantes?size=100'),
      apiFetch<PageResponse<Docente>>(API.usuarios, '/api/v1/docentes?size=100'),
      apiFetch<PageResponse<Carrera>>(API.cursos, '/api/v1/carreras?size=100'),
    ]).then(([studentPage, teacherPage, careerPage]) => {
      setStudents(studentPage.contenido); setTeachers(teacherPage.contenido); setCareers(careerPage.contenido);
      setError('');
    }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load, requestId]);

  const filteredStudents = useMemo(() => students.filter((item) =>
    `${item.codigo} ${item.nombres} ${item.apellidos} ${item.correo}`.toLowerCase().includes(search.toLowerCase())), [search, students]);
  const filteredTeachers = useMemo(() => teachers.filter((item) =>
    `${item.codigo} ${item.nombres} ${item.apellidos} ${item.especialidad} ${item.correo}`.toLowerCase().includes(search.toLowerCase())), [search, teachers]);

  async function saveUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSubmitting(true); setFeedback(null);
    const form = new FormData(event.currentTarget);
    try {
      if (tab === 'estudiantes') {
        await apiFetch(API.usuarios, editing ? `/api/v1/estudiantes/${editing.id}` : '/api/v1/estudiantes', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({
          ...(!editing ? { correo: form.get('correo'), clave: form.get('clave'), codigo: form.get('codigo') } : {}),
          nombres: form.get('nombres'), apellidos: form.get('apellidos'), documentoIdentidad: form.get('documentoIdentidad'),
          fechaNacimiento: form.get('fechaNacimiento'), telefono: form.get('telefono') || null,
          direccion: form.get('direccion') || null, carreraId: form.get('carreraId') ? Number(form.get('carreraId')) : null,
        }) });
      } else {
        await apiFetch(API.usuarios, editing ? `/api/v1/docentes/${editing.id}` : '/api/v1/docentes', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({
          ...(!editing ? { correo: form.get('correo'), clave: form.get('clave'), codigo: form.get('codigo') } : {}),
          nombres: form.get('nombres'), apellidos: form.get('apellidos'), documentoIdentidad: form.get('documentoIdentidad'),
          especialidad: form.get('especialidad'), telefono: form.get('telefono') || null,
        }) });
      }
      setModalOpen(false); setEditing(null); setFeedback({ type: 'success', message: `${tab === 'estudiantes' ? 'Estudiante' : 'Docente'} ${editing ? 'actualizado' : 'registrado'} correctamente.` });
      setLoading(true); setRequestId((value) => value + 1);
    } catch (reason) { setFeedback({ type: 'error', message: reason instanceof Error ? reason.message : 'No se pudo registrar.' }); }
    finally { setSubmitting(false); }
  }

  async function toggleState(userId: number, current: string) {
    const next = current === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    try {
      await apiFetch(API.usuarios, `/api/v1/usuarios/${userId}/estado`, { method: 'PATCH', body: JSON.stringify({ estado: next }) });
      setFeedback({ type: 'success', message: `Usuario ${next === 'ACTIVO' ? 'activado' : 'desactivado'} correctamente.` });
      setLoading(true); setRequestId((value) => value + 1);
    } catch (reason) { setFeedback({ type: 'error', message: reason instanceof Error ? reason.message : 'No se pudo cambiar el estado.' }); }
  }

  return <>
    <PageHeading eyebrow="Administración" title="Usuarios y perfiles" description="Registra estudiantes y docentes, consulta sus datos y controla el acceso institucional." />
    {feedback && <Feedback {...feedback} onClose={() => setFeedback(null)} />}
    {error && <DashboardError message={error} retry={() => { setLoading(true); setRequestId((value) => value + 1); }} />}
    <section className="module-card">
      <ModuleTabs items={[{ value: 'estudiantes', label: 'Estudiantes', count: students.length }, { value: 'docentes', label: 'Docentes', count: teachers.length }]}
        active={tab} onChange={(value) => { setTab(value); setSearch(''); }} />
      <ModuleToolbar search={search} onSearch={setSearch} actionLabel={`Nuevo ${tab === 'estudiantes' ? 'estudiante' : 'docente'}`} onAction={() => { setEditing(null); setModalOpen(true); }}>
        <button className="secondary-button" onClick={() => { setLoading(true); setRequestId((value) => value + 1); }}><RefreshCw size={15} />Actualizar</button>
      </ModuleToolbar>
      <div className="module-body">{loading ? <DashboardSkeleton /> : tab === 'estudiantes' ? (
        filteredStudents.length === 0 ? <EmptyState title="No hay estudiantes" description="Registra un estudiante o cambia el texto de búsqueda." /> :
          <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Estudiante</th><th>Código</th><th>Documento</th><th>Carrera</th><th>Estado</th><th /></tr></thead>
            <tbody>{filteredStudents.map((item) => <tr key={item.id}><td><div className="identity-cell"><span className="table-avatar">{item.nombres[0]}{item.apellidos[0]}</span><div><strong>{item.nombres} {item.apellidos}</strong><small>{item.correo}</small></div></div></td>
              <td>{item.codigo}</td><td>{item.documentoIdentidad}</td><td>{careers.find((career) => career.id === item.carreraId)?.codigo ?? 'Sin asignar'}</td><td><StatusBadge value={item.estado} /></td>
              <td><RowActions><ActionIcon label="Editar estudiante" onClick={() => { setEditing(item); setModalOpen(true); }}><Pencil size={15} /></ActionIcon><ActionIcon label={item.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'} danger={item.estado === 'ACTIVO'} onClick={() => toggleState(item.usuarioId, item.estado)}>
                {item.estado === 'ACTIVO' ? <UserRoundX size={15} /> : <UserRoundCheck size={15} />}</ActionIcon></RowActions></td></tr>)}</tbody></table></div>
      ) : filteredTeachers.length === 0 ? <EmptyState title="No hay docentes" description="Registra un docente o cambia el texto de búsqueda." /> :
        <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Docente</th><th>Código</th><th>Especialidad</th><th>Documento</th><th>Estado</th><th /></tr></thead>
          <tbody>{filteredTeachers.map((item) => <tr key={item.id}><td><div className="identity-cell"><span className="table-avatar">{item.nombres[0]}{item.apellidos[0]}</span><div><strong>{item.nombres} {item.apellidos}</strong><small>{item.correo}</small></div></div></td>
            <td>{item.codigo}</td><td>{item.especialidad}</td><td>{item.documentoIdentidad}</td><td><StatusBadge value={item.estado} /></td>
            <td><RowActions><ActionIcon label="Editar docente" onClick={() => { setEditing(item); setModalOpen(true); }}><Pencil size={15} /></ActionIcon><ActionIcon label={item.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'} danger={item.estado === 'ACTIVO'} onClick={() => toggleState(item.usuarioId, item.estado)}>
              {item.estado === 'ACTIVO' ? <UserRoundX size={15} /> : <UserRoundCheck size={15} />}</ActionIcon></RowActions></td></tr>)}</tbody></table></div>}
      </div>
    </section>
    <Modal open={modalOpen} onClose={() => { setModalOpen(false); setEditing(null); }} title={`${editing ? 'Editar' : 'Registrar'} ${tab === 'estudiantes' ? 'estudiante' : 'docente'}`}
      description={editing ? 'Actualiza los datos del perfil. El correo, código y credenciales se conservan.' : 'La cuenta podrá iniciar sesión inmediatamente con las credenciales registradas.'}>
      <form className="entity-form" onSubmit={saveUser}><div className="form-grid">
        {!editing && <><div className="form-field"><label>Correo institucional</label><input name="correo" type="email" required maxLength={150} /></div>
        <div className="form-field"><label>Contraseña inicial</label><input name="clave" type="password" required minLength={8} maxLength={72} /></div>
        <div className="form-field"><label>Código</label><input name="codigo" required minLength={3} maxLength={20} /></div></>}
        <div className="form-field"><label>Documento</label><input name="documentoIdentidad" defaultValue={editing?.documentoIdentidad} required minLength={6} maxLength={20} /></div>
        <div className="form-field"><label>Nombres</label><input name="nombres" defaultValue={editing?.nombres} required maxLength={100} /></div>
        <div className="form-field"><label>Apellidos</label><input name="apellidos" defaultValue={editing?.apellidos} required maxLength={100} /></div>
        {tab === 'estudiantes' ? <>
          <div className="form-field"><label>Fecha de nacimiento</label><input name="fechaNacimiento" type="date" defaultValue={(editing as Estudiante | null)?.fechaNacimiento} required /></div>
          <div className="form-field"><label>Carrera</label><select name="carreraId" defaultValue={(editing as Estudiante | null)?.carreraId ?? ''}><option value="">Sin asignar</option>{careers.filter((item) => item.estado === 'ACTIVO' || item.id === (editing as Estudiante | null)?.carreraId).map((item) => <option key={item.id} value={item.id}>{item.codigo} · {item.nombre}</option>)}</select></div>
          <div className="form-field"><label>Teléfono</label><input name="telefono" defaultValue={editing?.telefono ?? ''} /></div>
          <div className="form-field"><label>Dirección</label><input name="direccion" defaultValue={(editing as Estudiante | null)?.direccion ?? ''} maxLength={200} /></div>
        </> : <>
          <div className="form-field full"><label>Especialidad</label><input name="especialidad" defaultValue={(editing as Docente | null)?.especialidad} required maxLength={120} /></div>
          <div className="form-field"><label>Teléfono</label><input name="telefono" defaultValue={editing?.telefono ?? ''} /></div>
        </>}
      </div><FormActions submitting={submitting} onCancel={() => { setModalOpen(false); setEditing(null); }} label={editing ? 'Guardar cambios' : 'Registrar'} /></form>
    </Modal>
  </>;
}
