'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { CalendarDays, ChevronLeft, ChevronRight, Clock3, List, MapPin, RefreshCw, Rows3 } from 'lucide-react';
import { DashboardError, DashboardSkeleton, EmptyState, PageHeading } from '@/components/dashboard-ui';
import { API, apiFetch, PageResponse } from '@/lib/api';
import { Aula, Curso, Horario, Matricula, Periodo, Seccion } from '@/lib/types';

const DAY_NAMES = ['DOMINGO', 'LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO'];
const DAY_SHORT = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
const START_MINUTES = 6 * 60;
const END_MINUTES = 22 * 60;
const PX_PER_MINUTE = 0.72;

interface ScheduleEvent {
  key: string;
  date: Date;
  enrollment: Matricula;
  section: Seccion;
  course: Curso;
  classroom?: Aula;
  period: Periodo;
  schedule: Horario;
}

function startOfWeek(value: Date) {
  const result = new Date(value.getFullYear(), value.getMonth(), value.getDate());
  result.setDate(result.getDate() - result.getDay());
  return result;
}

function addDays(value: Date, amount: number) {
  const result = new Date(value);
  result.setDate(result.getDate() + amount);
  return result;
}

function dateKey(value: Date) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function minutesOf(time: string) {
  const [hours, minutes] = time.split(':').map(Number);
  return hours * 60 + minutes;
}

function displayTime(time: string) {
  const [hours, minutes] = time.split(':').map(Number);
  const suffix = hours >= 12 ? 'p. m.' : 'a. m.';
  const displayHours = hours % 12 || 12;
  return `${displayHours}:${String(minutes).padStart(2, '0')} ${suffix}`;
}

function rangeLabel(start: Date) {
  const end = addDays(start, 6);
  const first = new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short' }).format(start);
  const last = new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short', year: 'numeric' }).format(end);
  return `${first} – ${last}`;
}

async function recordMap<T extends { id: number }>(ids: number[], baseUrl: string, path: string) {
  const uniqueIds = [...new Set(ids)];
  const values = await Promise.all(uniqueIds.map((id) => apiFetch<T>(baseUrl, `${path}/${id}`)));
  return Object.fromEntries(values.map((item) => [item.id, item])) as Record<number, T>;
}

export default function HorarioEstudiantePage() {
  const [enrollments, setEnrollments] = useState<Matricula[]>([]);
  const [sections, setSections] = useState<Record<number, Seccion>>({});
  const [courses, setCourses] = useState<Record<number, Curso>>({});
  const [classrooms, setClassrooms] = useState<Record<number, Aula>>({});
  const [periods, setPeriods] = useState<Record<number, Periodo>>({});
  const [weekStart, setWeekStart] = useState(() => startOfWeek(new Date()));
  const [view, setView] = useState<'semana' | 'lista'>('semana');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [requestId, setRequestId] = useState(0);

  const load = useCallback(() => {
    apiFetch<PageResponse<Matricula>>(API.matriculas, '/api/v1/matriculas/mias?size=500').then(async (page) => {
      const active = page.contenido.filter((item) => item.estado === 'ACTIVA');
      const sectionMap = await recordMap<Seccion>(active.map((item) => item.seccionId), API.matriculas, '/api/v1/secciones');
      const [courseMap, classroomMap, periodMap] = await Promise.all([
        recordMap<Curso>(active.map((item) => item.cursoId), API.cursos, '/api/v1/cursos'),
        recordMap<Aula>(Object.values(sectionMap).map((item) => item.aulaId), API.cursos, '/api/v1/aulas'),
        recordMap<Periodo>(active.map((item) => item.periodoId), API.matriculas, '/api/v1/periodos'),
      ]);
      setEnrollments(active); setSections(sectionMap); setCourses(courseMap); setClassrooms(classroomMap); setPeriods(periodMap); setError('');
    }).catch((reason: Error) => setError(reason.message)).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load, requestId]);

  const days = useMemo(() => Array.from({ length: 7 }, (_, index) => addDays(weekStart, index)), [weekStart]);
  const events = useMemo<ScheduleEvent[]>(() => enrollments.flatMap((enrollment) => {
    const section = sections[enrollment.seccionId]; const course = courses[enrollment.cursoId]; const period = periods[enrollment.periodoId];
    if (!section || !course || !period || ['CANCELADA', 'FINALIZADA'].includes(section.estado)) return [];
    return section.horarios.flatMap((schedule) => {
      const dayIndex = DAY_NAMES.indexOf(schedule.diaSemana);
      const date = days[dayIndex];
      if (!date || dateKey(date) < period.fechaInicio || dateKey(date) > period.fechaFin) return [];
      return [{ key: `${enrollment.id}-${schedule.id}-${dateKey(date)}`, date, enrollment, section, course, classroom: classrooms[section.aulaId], period, schedule }];
    });
  }), [classrooms, courses, days, enrollments, periods, sections]);

  const todayKey = dateKey(new Date());
  const currentMinutes = new Date().getHours() * 60 + new Date().getMinutes();
  const timeLabels = Array.from({ length: 17 }, (_, index) => START_MINUTES + index * 60);

  function moveWeek(amount: number) { setWeekStart((current) => addDays(current, amount * 7)); }
  function refresh() { setLoading(true); setRequestId((value) => value + 1); }

  return <>
    <PageHeading eyebrow="Estudiante" title="Mi horario" description="Consulta tus clases semanales, horas y aulas de las matrículas activas."
      action={<button className="secondary-button" onClick={refresh}><RefreshCw size={15} />Actualizar</button>} />
    {error && <DashboardError message={error} retry={refresh} />}
    {loading ? <DashboardSkeleton /> : enrollments.length === 0 ? <EmptyState title="No tienes un horario activo" description="Cuando te matricules en una sección, sus clases aparecerán automáticamente aquí." /> : <section className="schedule-card">
      <header className="schedule-toolbar">
        <div className="schedule-navigation"><button className="icon-button" onClick={() => moveWeek(-1)} aria-label="Semana anterior"><ChevronLeft size={19} /></button><button className="icon-button" onClick={() => moveWeek(1)} aria-label="Semana siguiente"><ChevronRight size={19} /></button><button className="secondary-button" onClick={() => setWeekStart(startOfWeek(new Date()))}>Hoy</button></div>
        <div className="schedule-title"><CalendarDays size={18} /><strong>{rangeLabel(weekStart)}</strong></div>
        <div className="schedule-view-toggle" aria-label="Vista de horario"><button className={view === 'semana' ? 'active' : ''} onClick={() => setView('semana')}><Rows3 size={15} />Semana</button><button className={view === 'lista' ? 'active' : ''} onClick={() => setView('lista')}><List size={15} />Lista</button></div>
      </header>

      {view === 'semana' ? <div className="schedule-scroll"><div className="schedule-week">
        <div className="schedule-week-header"><div className="schedule-corner" />{days.map((day, index) => <div key={dateKey(day)} className={dateKey(day) === todayKey ? 'today' : ''}><span>{DAY_SHORT[index]}</span><strong>{day.getDate()}</strong></div>)}</div>
        <div className="schedule-week-body" style={{ height: `${(END_MINUTES - START_MINUTES) * PX_PER_MINUTE}px` }}>
          <div className="schedule-time-rail">{timeLabels.map((minutes) => <span key={minutes} style={{ top: `${(minutes - START_MINUTES) * PX_PER_MINUTE}px` }}>{displayTime(`${Math.floor(minutes / 60)}:${String(minutes % 60).padStart(2, '0')}`)}</span>)}</div>
          {days.map((day) => <div key={dateKey(day)} className={`schedule-day-column ${dateKey(day) === todayKey ? 'today' : ''}`}>
            {dateKey(day) === todayKey && currentMinutes >= START_MINUTES && currentMinutes <= END_MINUTES && <div className="current-time-line" style={{ top: `${(currentMinutes - START_MINUTES) * PX_PER_MINUTE}px` }}><span /></div>}
            {events.filter((item) => dateKey(item.date) === dateKey(day)).map((item) => { const start = minutesOf(item.schedule.horaInicio); const end = minutesOf(item.schedule.horaFin); const clippedStart = Math.max(start, START_MINUTES); const clippedEnd = Math.min(end, END_MINUTES); if (clippedEnd <= clippedStart) return null; const tone = item.course.id % 8; return <article key={item.key} className={`schedule-event tone-${tone}`} style={{ top: `${(clippedStart - START_MINUTES) * PX_PER_MINUTE}px`, height: `${Math.max(38, (clippedEnd - clippedStart) * PX_PER_MINUTE)}px` }} title={`${item.course.nombre} · ${displayTime(item.schedule.horaInicio)} a ${displayTime(item.schedule.horaFin)}`}>
              <span>{displayTime(item.schedule.horaInicio)} – {displayTime(item.schedule.horaFin)}</span><strong>{item.course.codigo}</strong><p>{item.course.nombre}</p><small>{item.classroom?.codigo ?? 'Aula por confirmar'} · Sec. {item.section.codigo}</small>
            </article>; })}
          </div>)}
        </div>
      </div></div> : <div className="schedule-list">{days.map((day, index) => { const dayEvents = events.filter((item) => dateKey(item.date) === dateKey(day)).sort((a, b) => minutesOf(a.schedule.horaInicio) - minutesOf(b.schedule.horaInicio)); return <section key={dateKey(day)} className={dateKey(day) === todayKey ? 'today' : ''}><header><span>{DAY_SHORT[index]}</span><strong>{day.getDate()}</strong><p>{new Intl.DateTimeFormat('es-PE', { month: 'long', year: 'numeric' }).format(day)}</p></header><div>{dayEvents.length ? dayEvents.map((item) => <article key={item.key} className={`tone-${item.course.id % 8}`}><div className="schedule-list-time"><Clock3 size={15} /><strong>{displayTime(item.schedule.horaInicio)}</strong><span>{displayTime(item.schedule.horaFin)}</span></div><div><strong>{item.course.codigo} · {item.course.nombre}</strong><p>Sección {item.section.codigo} · {item.period.codigo}</p><small><MapPin size={13} />{item.classroom ? `${item.classroom.codigo} · ${item.classroom.nombre}` : 'Aula por confirmar'}</small></div></article>) : <p className="schedule-free-day">Sin clases programadas</p>}</div></section>; })}</div>}
      {events.length === 0 && <div className="schedule-week-empty"><CalendarDays size={22} /><span>No hay clases dentro de las fechas de esta semana.</span></div>}
    </section>}
  </>;
}
