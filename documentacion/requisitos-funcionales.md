1. Servicio de Usuarios (Gestión de Identidad)
RF-USU-01 (Mantenimiento de Credenciales): El sistema debe permitir el registro, actualización y eliminación lógica de accesos y roles (Administrador, Docente, Estudiante).

RF-USU-02 (Gestión de Perfiles): El sistema debe administrar la información personal, académica y de contacto asociada a cada tipo de perfil registrado.

RF-USU-03 (Validación de Identidad): El servicio debe exponer endpoints REST para confirmar a otros módulos el estado activo y la existencia de un perfil específico.

2. Servicio de Cursos (Catálogo e Infraestructura)
RF-CUR-01 (Mantenimiento del Catálogo): El sistema debe gestionar la creación y configuración de carreras, ciclos y materias, incluyendo sus prerrequisitos.

RF-CUR-02 (Gestión de Infraestructura): El sistema debe permitir el registro y control de aforo de los espacios físicos disponibles (aulas y laboratorios).

RF-CUR-03 (Consulta de Oferta): El servicio debe listar el catálogo consolidado de materias vigentes y su estructura para ser consultado por módulos externos.

3. Servicio de Matrículas (Gestión Operativa)
RF-MAT-01 (Apertura de Secciones): El sistema debe permitir la creación de secciones asociando un período académico, materia, aula y el identificador del responsable a cargo.

RF-MAT-02 (Procesamiento de Inscripción): El sistema debe registrar inscripciones validando previamente la identidad del solicitante y la disponibilidad de cupos mediante servicios externos.

RF-MAT-03 (Control de Vacantes): El sistema debe decrementar dinámicamente los cupos disponibles de una sección y rechazar automáticamente las solicitudes que excedan el aforo permitido.

4. Servicio de Evaluaciones (Calificaciones)
RF-EVA-01 (Registro de Calificaciones): El sistema debe permitir el ingreso y modificación de notas, asociándolas obligatoriamente a un registro de inscripción válido.

RF-EVA-02 (Cálculo de Resultados): El sistema debe calcular automáticamente los promedios consolidados y determinar el estado final del alumno (Aprobado/Desaprobado).

RF-EVA-03 (Historial Académico): El servicio debe generar y restringir la consulta del historial de notas exclusivamente al titular correspondiente y a la administración.