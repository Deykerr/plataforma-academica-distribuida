import { Brand } from './brand';

export function LoadingScreen({ message }: { message: string }) {
  return (
    <main className="loading-screen" role="status" aria-live="polite">
      <Brand />
      <span className="spinner" aria-hidden="true" />
      <p>{message}</p>
    </main>
  );
}
