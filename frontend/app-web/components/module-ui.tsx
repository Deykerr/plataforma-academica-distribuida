'use client';

import { useEffect } from 'react';
import { AlertCircle, CheckCircle2, Plus, Search, X } from 'lucide-react';

export function ModuleTabs<T extends string>({ items, active, onChange }: {
  items: { value: T; label: string; count?: number }[];
  active: T;
  onChange: (value: T) => void;
}) {
  return <div className="module-tabs" role="tablist">{items.map((item) => (
    <button key={item.value} role="tab" aria-selected={active === item.value}
      className={active === item.value ? 'active' : ''} onClick={() => onChange(item.value)}>
      {item.label}{item.count !== undefined && <span>{item.count}</span>}
    </button>
  ))}</div>;
}

export function ModuleToolbar({ search, onSearch, actionLabel, onAction, children }: {
  search: string;
  onSearch: (value: string) => void;
  actionLabel?: string;
  onAction?: () => void;
  children?: React.ReactNode;
}) {
  return <div className="module-toolbar">
    <div className="module-search"><Search size={17} /><input value={search}
      onChange={(event) => onSearch(event.target.value)} placeholder="Buscar por código o nombre…" aria-label="Buscar" /></div>
    <div className="toolbar-actions">{children}{actionLabel && onAction && <button className="primary-small" onClick={onAction}><Plus size={16} />{actionLabel}</button>}</div>
  </div>;
}

export function Modal({ open, title, description, onClose, children, wide = false }: {
  open: boolean; title: string; description?: string; onClose: () => void;
  children: React.ReactNode; wide?: boolean;
}) {
  useEffect(() => {
    if (!open) return;
    const close = (event: KeyboardEvent) => event.key === 'Escape' && onClose();
    document.addEventListener('keydown', close);
    document.body.classList.add('modal-open');
    return () => { document.removeEventListener('keydown', close); document.body.classList.remove('modal-open'); };
  }, [onClose, open]);
  if (!open) return null;
  return <div className="modal-layer" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <section className={`modal-panel ${wide ? 'wide' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <header><div><h2 id="modal-title">{title}</h2>{description && <p>{description}</p>}</div>
        <button className="icon-button" onClick={onClose} aria-label="Cerrar"><X size={19} /></button></header>
      {children}
    </section>
  </div>;
}

export function FormActions({ submitting, onCancel, label = 'Guardar' }: {
  submitting: boolean; onCancel: () => void; label?: string;
}) {
  return <div className="form-actions"><button type="button" className="secondary-button" onClick={onCancel}>Cancelar</button>
    <button type="submit" className="primary-small" disabled={submitting}>{submitting ? 'Guardando…' : label}</button></div>;
}

export function Feedback({ type, message, onClose }: {
  type: 'success' | 'error'; message: string; onClose?: () => void;
}) {
  const Icon = type === 'success' ? CheckCircle2 : AlertCircle;
  return <div className={`feedback ${type}`} role={type === 'error' ? 'alert' : 'status'}><Icon size={18} /><span>{message}</span>
    {onClose && <button onClick={onClose} aria-label="Cerrar"><X size={15} /></button>}</div>;
}

export function RowActions({ children }: { children: React.ReactNode }) {
  return <div className="row-actions">{children}</div>;
}

export function ActionIcon({ label, onClick, children, danger = false }: {
  label: string; onClick: () => void; children: React.ReactNode; danger?: boolean;
}) {
  return <button className={`action-icon ${danger ? 'danger' : ''}`} onClick={onClick} aria-label={label} title={label}>{children}</button>;
}
