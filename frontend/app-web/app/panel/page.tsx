'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { LoadingScreen } from '@/components/loading-screen';
import { useAuth } from '@/context/auth-context';
import { dashboardFor } from '@/lib/auth';

export default function PanelPage() {
  const { session, ready } = useAuth();
  const router = useRouter();
  useEffect(() => {
    if (ready && session) router.replace(dashboardFor(session.roles));
  }, [ready, router, session]);
  return <LoadingScreen message="Abriendo tu panel" />;
}
