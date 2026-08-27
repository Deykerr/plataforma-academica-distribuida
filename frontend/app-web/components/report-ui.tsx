'use client';

import { Download, Printer } from 'lucide-react';
import { printReport } from '@/lib/reports';

export function ReportActions({ onExport, disabled = false }: { onExport: () => void; disabled?: boolean }) {
  return <div className="report-actions no-print">
    <button className="secondary-button" onClick={onExport} disabled={disabled}><Download size={15} />Exportar CSV</button>
    <button className="primary-small" onClick={printReport} disabled={disabled}><Printer size={15} />Imprimir / PDF</button>
  </div>;
}

export function ReportHeader({ title, subtitle }: { title: string; subtitle: string }) {
  return <header className="report-document-header"><div><span>Aula Nexus · Plataforma académica</span><h2>{title}</h2><p>{subtitle}</p></div><strong>Reporte académico</strong></header>;
}
