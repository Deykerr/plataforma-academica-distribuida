import Link from 'next/link';

export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <Link href="/" className="brand" aria-label="Aula Nexus, inicio">
      <span className="brand-mark" aria-hidden="true">AN</span>
      {!compact && (
        <span className="brand-copy">
          <strong>Aula Nexus</strong>
          <small>Gestión académica</small>
        </span>
      )}
    </Link>
  );
}
