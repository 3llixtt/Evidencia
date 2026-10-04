# Acerca de

El **Sistema de Administración de Citas Médicas** es un programa de consola, escrito en Java 11, que simula la administración de citas de un consultorio médico.

## Qué permite hacer

- **Dar de alta doctores:** identificador único, nombre completo y especialidad.
- **Dar de alta pacientes:** identificador único y nombre completo.
- **Crear citas:** identificador único, fecha y hora, y motivo. Se pueden crear varias, y cada una queda relacionada con un doctor y un paciente.
- **Controlar el acceso:** solo los administradores registrados, con identificador y contraseña, pueden usar el sistema.
- **Guardar la información** en archivos de texto plano con formato CSV, dentro de la carpeta `db`.

## Características técnicas

- **Portable:** se empaqueta como un FAT JAR y se ejecuta en cualquier sistema operativo con Java instalado.
- **Tolerante a errores:** si ocurre una excepción, el programa muestra el mensaje en pantalla y sigue ejecutándose.
- **Manejo de recursos:** los archivos se abren y se cierran de forma segura, y se escriben primero en un archivo temporal para no dejar datos a medias.
- **Carpeta `db` autorreparable:** si la carpeta o algún archivo no existe, el programa lo detecta al iniciar y lo vuelve a crear. Los archivos de datos nunca se suben al repositorio.
- **Contraseñas protegidas:** no se guardan en texto plano, sino como una sal aleatoria y un hash PBKDF2.
- **Probado:** incluye pruebas automatizadas con JUnit 4 para el modelo, la persistencia, las reglas de negocio y el recorrido completo del menú.

Para ver cómo está construido, consulta [Proyecto](Proyecto). Para instalarlo y usarlo, consulta [Guías](Guias).
