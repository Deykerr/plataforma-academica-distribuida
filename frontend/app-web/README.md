# Frontend de la Plataforma Académica

Aplicación React con enrutamiento, sesión JWT y paneles separados para `ADMINISTRADOR`, `DOCENTE` y `ESTUDIANTE`.

## Requisitos

- Node.js 22.13 o superior;
- los cuatro microservicios ejecutándose en los puertos `8081` a `8084`.

## Ejecución local

Desde la raíz del repositorio:

```powershell
cd frontend\app-web
npm install
npm run dev
```

Abra `http://localhost:5173`. Para el primer acceso local puede usar:

```text
Correo: admin@academica.local
Contraseña: Admin123*
```

La aplicación conserva el JWT en el almacenamiento local hasta su vencimiento, lo adjunta a las solicitudes y elimina la sesión cuando el backend responde `401`.

## Rutas protegidas

| Ruta | Rol |
|---|---|
| `/panel/administrador` | `ADMINISTRADOR` |
| `/panel/docente` | `DOCENTE` |
| `/panel/estudiante` | `ESTUDIANTE` |

Una cuenta que intente abrir el panel de otro rol es redirigida a su propia vista.

## Configuración

Los valores predeterminados apuntan al backend local. Para cambiarlos, copie `.env.example` como `.env.local` y modifique las URL. Los archivos `.env*` reales están excluidos de Git.

## Verificación

```powershell
npm run lint
npm run build
```
