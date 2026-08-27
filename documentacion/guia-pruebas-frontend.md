# Guía de pruebas integrales desde el frontend

Esta guía valida el flujo completo de la plataforma usando únicamente la interfaz web. Swagger queda disponible para diagnóstico, pero no es necesario para ejecutar el escenario.

## 1. Iniciar el sistema

Desde la raíz del proyecto, abra una terminal de VS Code y ejecute:

```powershell
cd infraestructura
docker compose up -d --build
docker compose ps
```

Los cuatro servicios y las cuatro bases PostgreSQL deben aparecer en estado `Up`. En una segunda terminal:

```powershell
cd frontend\app-web
npm install
npm run dev
```

Abra `http://localhost:5173` e inicie sesión como administrador:

```text
Correo: admin@academica.local
Contraseña: Admin123*
```

## 2. Preparar el catálogo como administrador

En **Catálogo académico**:

1. Cree una carrera, por ejemplo `ING-SIS`.
2. Cree al menos dos ciclos asociados a la carrera.
3. Cree un curso básico sin prerrequisitos, por ejemplo `MAT-101`.
4. Cree un segundo curso y seleccione `MAT-101` como prerrequisito.
5. Cree un aula disponible con capacidad suficiente.
6. Use el botón de lápiz para comprobar que carreras, ciclos, cursos y aulas se pueden editar.

En **Usuarios y perfiles**:

1. Cree un docente y guarde su correo y contraseña inicial.
2. Cree un estudiante y guarde sus credenciales.
3. Asigne el estudiante a la carrera creada.
4. Compruebe la edición de ambos perfiles con el botón de lápiz.

## 3. Crear una matrícula válida

En **Operación académica**:

1. Cree un periodo `PLANIFICADO`. El rango de matrícula debe incluir el día de la prueba y el inicio de clases debe ser posterior al inicio de matrícula.
2. Cree una sección del curso `MAT-101`, asigne el docente y el aula, y configure su horario.
3. Edite el periodo o la sección mientras aún lo permitan sus estados.
4. Cambie el periodo a `MATRICULA_ABIERTA` y después la sección a `ABIERTA`.
5. Registre la matrícula del estudiante. La misma operación también puede hacerla el estudiante desde **Oferta académica**.
6. Intente repetir la matrícula: el sistema debe rechazar el duplicado.

## 4. Registrar y publicar notas como docente

1. Cierre sesión e ingrese con la cuenta docente.
2. Abra **Mis secciones** y verifique la sección asignada.
3. Entre a **Evaluaciones y notas**.
4. Cree componentes cuya ponderación total sea exactamente `100%`; por ejemplo, parcial `40%` y final `60%`.
5. Edite una evaluación mientras esté en `BORRADOR`.
6. Registre una nota aprobatoria para el estudiante en cada componente.
7. Cambie las evaluaciones a `PUBLICADA`.
8. Abra **Reportes**, seleccione la sección, descargue el CSV y pruebe **Imprimir / PDF**.

El resultado del curso se considera completo cuando las evaluaciones oficiales suman `100%`. Con promedio de `11` o más aparecerá como `APROBADO`.

## 5. Probar los prerrequisitos

1. Regrese como administrador y finalice el flujo académico anterior si desea reflejar el cierre operativo.
2. Cree un nuevo periodo y una sección para el segundo curso, abra su matrícula y sección.
3. Ingrese como el estudiante y matricúlese desde **Oferta académica**. Debe aceptar la inscripción porque `MAT-101` está aprobado.
4. Para probar el rechazo, use otro estudiante sin historial aprobado e intente matricularlo en la misma sección. Debe mostrarse el mensaje con los cursos prerrequisito pendientes.

La validación consulta en tiempo real al servicio de Evaluaciones; no se basa solamente en el estado de la matrícula.

## 6. Verificar la experiencia del estudiante

Con la cuenta estudiante:

1. Revise **Mis matrículas** y pruebe el retiro solo durante una matrícula abierta.
2. Abra **Mi horario**, compruebe los bloques de curso, aula y sección, y navegue entre semanas o cambie a la vista de lista.
3. Revise **Mis notas** y abra el detalle de cada evaluación publicada.
4. Actualice teléfono o dirección en **Mi perfil**.
5. Abra **Mi reporte académico**, descargue el CSV y guarde la vista como PDF.
6. Intente escribir manualmente `/panel/administrador` en el navegador: debe volver al panel de estudiante.

## 7. Verificar reportes del administrador

Con la cuenta administradora, abra **Reportes**:

1. Seleccione el periodo y compruebe capacidad, matrículas, vacantes y porcentaje de ocupación.
2. Seleccione una sección y compruebe promedio, aprobados, desaprobados y avance de evaluación.
3. Exporte ambos reportes a CSV.
4. Use **Imprimir / PDF** y confirme que el menú lateral y los controles no aparecen en el documento.

## 8. Resultado esperado

La prueba es satisfactoria si:

- cada rol solo puede acceder a sus rutas;
- los registros se crean, editan y cambian de estado respetando las reglas del backend;
- no se permiten duplicados, choques, falta de vacantes ni prerrequisitos pendientes;
- las notas publicadas generan promedios e historial;
- los reportes cargan datos reales y se pueden exportar e imprimir;
- al recargar el navegador la sesión se conserva hasta que el JWT venza o se cierre sesión.

Para detener el entorno sin borrar los datos:

```powershell
cd infraestructura
docker compose down
```

No use `docker compose down -v` salvo que quiera eliminar todas las bases locales y comenzar desde cero.
