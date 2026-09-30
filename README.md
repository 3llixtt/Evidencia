# Sistema de Administración de Citas Médicas

Programa en Java que simula un sistema de administración de citas para un consultorio médico. Permite dar de alta doctores y pacientes, crear citas con fecha y hora, relacionar cada cita con un doctor y un paciente, y controlar el acceso mediante administradores con identificador y contraseña. Toda la información se almacena en archivos de texto plano con formato CSV.

> **Estado del proyecto:** Avance 1 (ambiente de desarrollo, diagrama de flujo, diagrama de clases y pseudocódigo). La implementación se realizará en las siguientes entregas.

## Instalación y configuración

### Requisitos

- JDK 11 (versión más reciente de la rama 11)
- IntelliJ IDEA (Community o Ultimate)
- Git

### Pasos

1. Clona el repositorio:
   ```bash
   git clone [URL del repositorio]
   cd [nombre-del-repositorio]
   ```
2. Cambia a la rama de desarrollo:
   ```bash
   git checkout develop
   ```
3. Abre la carpeta del proyecto en IntelliJ IDEA (**File → Open**).
4. Configura el SDK del proyecto en **File → Project Structure → Project → SDK** y selecciona JDK 11.
5. Verifica la instalación desde una terminal:
   ```bash
   java -version
   git --version
   ```

### Generar el FAT JAR (portabilidad)

El programa se empaquetará como un archivo FAT JAR con todas sus dependencias incluidas, de modo que pueda ejecutarse en cualquier sistema operativo que tenga Java instalado. Las instrucciones exactas se agregarán cuando se implemente la configuración de compilación.

## Uso del programa

Ejecución del programa (una vez generado el JAR):

```bash
java -jar [nombre-del-archivo].jar
```

Funcionalidades previstas:

- Acceso al sistema con identificador y contraseña de administrador.
- Alta de doctores (identificador único, nombre completo y especialidad).
- Alta de pacientes (identificador único y nombre completo).
- Creación de múltiples citas (identificador único, fecha y hora, y motivo).
- Relación de cada cita con un doctor y un paciente.
- Almacenamiento de la información en archivos CSV.
- Manejo de excepciones: si ocurre un error, el programa muestra el mensaje en pantalla y sigue ejecutándose.

## Créditos

- [Tu nombre completo], estudiante.
- [Nombre de la materia y del profesor].

## Licencia

Este proyecto se distribuye bajo la licencia MIT. Consulta el archivo `LICENSE` para más información.
