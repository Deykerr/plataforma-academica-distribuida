'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { LoadingScreen } from '@/components/loading-screen';
import { dashboardFor } from '@/lib/auth';
import { useAuth } from '@/context/auth-context';

export default function Home() {
  const router = useRouter();
  const { session, ready } = useAuth();

  useEffect(() => {
    if (!ready) return;
    router.replace(session ? dashboardFor(session.roles) : '/login');
  }, [ready, router, session]);

  return <LoadingScreen message="Preparando tu espacio académico" />;
}
