'use client';

import { FormEvent, useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, CheckCircle2 } from 'lucide-react';
import { Brand } from '@/components/brand';
import { Feedback } from '@/components/module-ui';
import { API, apiFetch } from '@/lib/api';

export default function RegistroPage() {
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  async function register(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    const form = new FormData(event.currentTarget);
    if (form.get('clave') !== form.get('confirmarClave')) {
      setError('Las contraseñas no coinciden.');
      setSubmitting(false);
      return;
    }
    try {
      await apiFetch(API.usuarios, '/api/v1/estudiantes', {
        method: 'POST',
        body: JSON.stringify({
          correo: form.get('correo'), clave: form.get('clave'), codigo: form.get('codigo'),
          nombres: form.get('nombres'), apellidos: form.get('apellidos'),
          documentoIdentidad: form.get('documentoIdentidad'), fechaNacimiento: form.get('fechaNacimiento'),
          telefono: form.get('telefono') || null, direccion: form.get('direccion') || null, carreraId: null,
        }),
      });
      setSuccess(true);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo crear la cuenta.');
    } finally {
      setSubmitting(false);
    }
  }

  return <main className="register-page">
    <header><Brand /><Link href="/login"><ArrowLeft size={16} />Volver al ingreso</Link></header>
    <section className="register-content">{success ? <div className="registration-success">
      <CheckCircle2 size={44} /><p className="eyebrow">Cuenta creada</p><h1>Ya puedes ingresar</h1>
      <p>Tu perfil de estudiante fue registrado. Un administrador podrá asociar tu carrera posteriormente.</p>
      <Link className="primary-button" href="/login">Ir al inicio de sesión</Link>
    </div> : <>
      <div className="register-heading"><p className="eyebrow">Nuevo estudiante</p><h1>Crea tu cuenta académica</h1>
        <p>Completa tus datos personales. Todos los campos marcados son necesarios para validar tu identidad.</p></div>
      {error && <Feedback type="error" message={error} onClose={() => setError('')} />}
      <form className="register-form" onSubmit={register}><div className="form-grid">
        <div className="form-field"><label>Correo</label><input name="correo" type="email" required /></div>
        <div className="form-field"><label>Código de estudiante</label><input name="codigo" minLength={3} maxLength={20} required /></div>
        <div className="form-field"><label>Contraseña</label><input name="clave" type="password" minLength={8} required /></div>
        <div className="form-field"><label>Confirmar contraseña</label><input name="confirmarClave" type="password" minLength={8} required /></div>
        <div className="form-field"><label>Documento</label><input name="documentoIdentidad" minLength={6} maxLength={20} required /></div>
        <div className="form-field"><label>Fecha de nacimiento</label><input name="fechaNacimiento" type="date" required /></div>
        <div className="form-field"><label>Nombres</label><input name="nombres" maxLength={100} required /></div>
        <div className="form-field"><label>Apellidos</label><input name="apellidos" maxLength={100} required /></div>
        <div className="form-field"><label>Teléfono</label><input name="telefono" /></div>
        <div className="form-field full"><label>Dirección</label><input name="direccion" maxLength={200} /></div>
      </div><button className="primary-button" disabled={submitting}>{submitting ? 'Creando cuenta…' : 'Crear cuenta de estudiante'}</button></form>
    </>}</section>
  </main>;
}
