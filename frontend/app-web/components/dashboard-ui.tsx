import type { LucideIcon } from 'lucide-react';
import { AlertCircle, Inbox, RefreshCw } from 'lucide-react';

export function PageHeading({ eyebrow, title, description, action }: {
  eyebrow: string; title: string; description: string; action?: React.ReactNode;
}) {
  return (
    <section className="page-heading">
      <div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{description}</p></div>
      {action}
    </section>
  );
}

export function MetricCard({ label, value, note, icon: Icon, tone = 0 }: {
  label: string; value: string | number; note: string; icon: LucideIcon; tone?: number;
}) {
  return (
    <article className="metric-card">
      <span className={`metric-icon tone-${tone}`}><Icon size={21} /></span>
      <p>{label}</p><strong>{value}</strong><small>{note}</small>
    </article>
  );
}

export function StatusBadge({ value }: { value: string }) {
  const normalized = value.toLowerCase();
  const tone = normalized.includes('activ') || normalized.includes('abiert') || normalized.includes('public')
    || normalized.includes('aprobad') ? 'success'
    : normalized.includes('cancel') || normalized.includes('anulad') ? 'danger' : 'neutral';
  return <span className={`status-badge ${tone}`}>{value.replaceAll('_', ' ')}</span>;
}

export function DashboardError({ message, retry }: { message: string; retry: () => void }) {
  return (
    <div className="dashboard-error" role="alert"><AlertCircle size={20} />
      <div><strong>No pudimos cargar este panel</strong><span>{message}</span></div>
      <button onClick={retry}><RefreshCw size={15} /> Reintentar</button>
    </div>
  );
}

export function DashboardSkeleton() {
  return <div className="dashboard-skeleton" role="status" aria-label="Cargando información"><span /><span /><span /><span /></div>;
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return <div className="empty-state"><Inbox size={30} /><h3>{title}</h3><p>{description}</p></div>;
}

export function ContentCard({ eyebrow, title, children, className = '' }: {
  eyebrow: string; title: string; children: React.ReactNode; className?: string;
}) {
  return (
    <article className={`content-card ${className}`}>
      <div className="card-heading"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div></div>
      {children}
    </article>
  );
}
