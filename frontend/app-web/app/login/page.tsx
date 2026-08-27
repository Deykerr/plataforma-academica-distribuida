'use client';

import { FormEvent, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowRight, BookOpenCheck, Boxes, Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react';
import { Brand } from '@/components/brand';
import { LoadingScreen } from '@/components/loading-screen';
import { useAuth } from '@/context/auth-context';
import { dashboardFor } from '@/lib/auth';

export default function LoginPage() {
  const router = useRouter();
  const { login, ready, session } = useAuth();
  const [correo, setCorreo] = useState('');
  const [clave, setClave] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (ready && session) router.replace(dashboardFor(session.roles));
  }, [ready, router, session]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const nextSession = await login(correo.trim(), clave);
      router.replace(dashboardFor(nextSession.roles));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No fue posible iniciar sesión.');
    } finally {
      setSubmitting(false);
    }
  }

  if (!ready || session) return <LoadingScreen message="Verificando tu sesión" />;

  return (
    <main className="login-page">
      <section className="login-form-side">
        <div className="login-form-wrap">
          <Brand />
          <div className="login-heading">
            <p className="eyebrow">Portal institucional</p>
            <h1>Bienvenido de nuevo</h1>
            <p>Ingresa con tu cuenta para acceder a tu espacio académico.</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form">
            <label htmlFor="correo">Correo institucional</label>
            <div className="input-wrap">
              <Mail size={19} aria-hidden="true" />
              <input id="correo" type="email" autoComplete="email" placeholder="nombre@institucion.edu"
                value={correo} onChange={(event) => setCorreo(event.target.value)} required />
            </div>

            <label htmlFor="clave">Contraseña</label>
            <div className="input-wrap">
              <LockKeyhole size={19} aria-hidden="true" />
              <input id="clave" type={showPassword ? 'text' : 'password'} autoComplete="current-password"
                placeholder="Ingresa tu contraseña" value={clave}
                onChange={(event) => setClave(event.target.value)} required />
              <button type="button" className="password-toggle" onClick={() => setShowPassword((value) => !value)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
              </button>
            </div>

            {error && <div className="form-error" role="alert">{error}</div>}

            <button className="primary-button" type="submit" disabled={submitting}>
              <span>{submitting ? 'Ingresando…' : 'Ingresar al portal'}</span>
              {!submitting && <ArrowRight size={19} aria-hidden="true" />}
            </button>
          </form>

          <div className="demo-access">
            <span>Acceso local de prueba</span>
            <code>admin@academica.local</code>
          </div>
          <p className="login-help">¿Tienes problemas para ingresar? Contacta al administrador de tu institución.</p>
        </div>
      </section>

      <aside className="login-visual" aria-label="Descripción de la plataforma">
        <div className="visual-grid" aria-hidden="true" />
        <div className="visual-content">
          <p className="eyebrow light">Plataforma distribuida</p>
          <h2>Todo el recorrido académico, conectado.</h2>
          <p>Una vista clara de cursos, matrículas y evaluaciones para cada integrante de la comunidad.</p>
          <div className="service-flow" aria-hidden="true">
            <span><BookOpenCheck size={22} /> Cursos</span><i />
            <span><Boxes size={22} /> Matrículas</span><i />
            <span><BookOpenCheck size={22} /> Notas</span>
          </div>
          <div className="visual-stat">
            <strong>4</strong>
            <span>microservicios coordinados<br />con una identidad segura</span>
          </div>
        </div>
      </aside>
    </main>
  );
}
