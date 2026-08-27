1. Interoperabilidad y Comunicación
RNF-INT-01 (Protocolo de Comunicación): Todos los microservicios deben comunicarse de manera síncrona a través de APIs REST utilizando el protocolo HTTP/HTTPS.

RNF-INT-02 (Formato de Datos): El intercambio de información entre los servicios y las peticiones del cliente (frontend) debe realizarse estrictamente en formato JSON.

2. Seguridad y Acceso
RNF-SEG-01 (Autenticación sin estado): El sistema debe implementar un mecanismo de seguridad basado en tokens (como JWT) para validar las credenciales de los usuarios sin mantener sesiones activas en el servidor.

RNF-SEG-02 (Autorización por Roles): Cada microservicio debe validar los permisos del token recibido para asegurar que las acciones (como registrar notas o crear secciones) solo sean ejecutadas por los roles autorizados.

RNF-SEG-03 (Aislamiento de Datos): Cada microservicio debe gestionar su propio esquema de base de datos de manera independiente; ningún servicio podrá acceder a la base de datos de otro directamente.

3. Disponibilidad y Tolerancia a Fallos
RNF-DIS-01 (Aislamiento de Fallos): La caída o el mantenimiento de un microservicio (por ejemplo, Evaluaciones) no debe interrumpir el funcionamiento general de los demás servicios (por ejemplo, Usuarios o Cursos).

RNF-DIS-02 (Gestión de Errores): Los microservicios deben capturar los errores de conexión y devolver respuestas HTTP con códigos de estado estándar (400, 404, 500) junto con mensajes descriptivos.

4. Despliegue e Infraestructura
RNF-DES-01 (Contenerización): La infraestructura base (como los motores de bases de datos locales) y eventualmente los microservicios, deben ser empaquetados utilizando Docker para garantizar entornos de ejecución idénticos.

RNF-DES-02 (Despliegue en la Nube): El sistema final debe estar preparado para ser desplegado en plataformas Cloud (como Railway o Render) configurando correctamente las variables de entorno.

5. Mantenibilidad
RNF-MAN-01 (Documentación de API): Todos los endpoints expuestos por los microservicios deben estar documentados interactivamente mediante la especificación OpenAPI (Swagger).

RNF-MAN-02 (Control de Versiones): El código fuente de todos los componentes debe centralizarse en un único repositorio remoto, manteniendo una estructura de carpetas modular por servicio.