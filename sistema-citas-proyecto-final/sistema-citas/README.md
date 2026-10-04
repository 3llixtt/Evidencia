# Sistema de Administración de Citas Médicas

Programa en Java que simula un sistema de administración de citas para un consultorio médico. Funciona por consola y permite:

- dar de alta **doctores** (identificador único, nombre completo y especialidad);
- dar de alta **pacientes** (identificador único y nombre completo);
- crear **múltiples citas** (identificador único, fecha y hora, y motivo), cada una relacionada con un doctor y un paciente;
- controlar el acceso mediante **administradores** con identificador y contraseña;
- guardar toda la información en archivos de texto plano **CSV**, dentro de la carpeta `db`.

El programa no se cierra cuando ocurre un error: muestra el mensaje en pantalla y regresa al menú.

## Instalación y configuración

### Requisitos

| Herramienta | Versión | Para qué se usa |
|---|---|---|
| JDK | 11 o superior (el proyecto se compila para Java 11) | Compilar y ejecutar |
| Git | cualquiera reciente | Clonar el repositorio |
| IntelliJ IDEA | Community o Ultimate (opcional) | Editar y ejecutar el proyecto |
| Maven | 3.6 o superior (opcional; IntelliJ ya incluye uno) | Compilar, probar y generar el FAT JAR |

### Pasos

1. Clona el repositorio y cambia a la rama de desarrollo (la versión estable está en `master`, con la etiqueta `v1.0`):
   ```bash
   git clone [URL del repositorio]
   cd [nombre-del-repositorio]
   git checkout develop
   ```
2. Abre la carpeta en IntelliJ IDEA (**File → Open**) y selecciona el archivo `pom.xml` si te lo pide; IntelliJ importará el proyecto Maven.
3. Verifica que el SDK sea JDK 11 o superior en **File → Project Structure → Project → SDK**.
4. Comprueba la instalación desde una terminal:
   ```bash
   java -version
   git --version
   ```

### Generar el FAT JAR

El programa se empaqueta como un **FAT JAR**: un solo archivo ejecutable que incluye todas las dependencias, por lo que se puede ejecutar en cualquier sistema operativo que tenga Java instalado.

**Con Maven** (usa `maven-shade-plugin`, configurado en `pom.xml`):
```bash
mvn clean package
```

**Sin Maven** (solo con las herramientas del JDK):
```bash
./compilar.sh        # Linux y macOS
compilar.bat         # Windows
```

En ambos casos el resultado es `target/sistema-citas-1.0.jar`.

### Pruebas automatizadas

```bash
mvn test
```

## Uso del programa

Ejecuta el JAR desde la carpeta donde quieras que se guarde la carpeta de datos `db`:

```bash
java -jar target/sistema-citas-1.0.jar
```

Opcionalmente puedes indicar otra carpeta de datos:

```bash
java -jar target/sistema-citas-1.0.jar /ruta/a/mis-datos
```

Desde IntelliJ IDEA, ejecuta la clase `consultorio.Main`.

### Primer uso

La primera vez no existe ningún administrador, así que el programa pide crear el primero (identificador, nombre completo y contraseña de al menos 6 caracteres). Después se solicita iniciar sesión. En una terminal la contraseña no se muestra al escribirla; en la ventana *Run* de un IDE sí se ve.

### Menú

| Opción | Acción |
|---|---|
| 1 | Alta de doctor |
| 2 | Alta de paciente |
| 3 | Crear cita (se muestran los doctores y pacientes registrados) |
| 4 | Listar citas, ordenadas por fecha y hora |
| 5 | Alta de administrador |
| 0 | Guardar y salir |

Reglas principales:

- Los identificadores solo pueden contener letras, números, guion y guion bajo (máximo 20 caracteres) y no se pueden repetir dentro del mismo tipo (no distinguen mayúsculas).
- La fecha de una cita se escribe como `dd/MM/aaaa HH:mm`, por ejemplo `15/10/2026 09:30`, y no puede estar en el pasado.
- Un doctor o un paciente no puede tener dos citas a la misma hora.

### Carpeta `db`

Los datos se guardan en archivos CSV (UTF-8) dentro de la carpeta `db`:

| Archivo | Contenido |
|---|---|
| `administradores.csv` | Administradores. La contraseña se guarda protegida (sal + PBKDF2), nunca en texto plano |
| `doctores.csv` | Doctores |
| `pacientes.csv` | Pacientes |
| `citas.csv` | Citas, con los identificadores del doctor y del paciente |

Estos archivos **no se suben al repositorio**: la carpeta `db` contiene su propio `.gitignore`. Si la carpeta o alguno de los archivos no existe, el programa lo detecta al iniciar y lo vuelve a crear vacío. Si una línea de un archivo está dañada, se omite y se muestra un aviso.

## Estructura del proyecto

```
.
├── pom.xml                  Configuración de Maven (Java 11, FAT JAR)
├── compilar.sh / .bat       Generan el FAT JAR sin Maven
├── db/.gitignore            Evita subir los archivos de datos
├── docs/                    Diagramas y pseudocódigo del diseño
└── src
    ├── main/java/consultorio
    │   ├── Main.java        Punto de entrada y menú
    │   ├── excepciones/     SistemaCitasException
    │   ├── modelo/          Persona, Doctor, Paciente, Administrador, Cita, Almacenable, Validaciones
    │   ├── persistencia/    Repositorio, RepositorioCSV, Convertidor, UtilCSV
    │   ├── servicio/        SistemaCitas (reglas de negocio)
    │   └── ui/              Consola, EntradaTerminadaException
    └── test/java/consultorio   Pruebas con JUnit 4
```

## Control de versiones

- `master`: código final y estable, con la etiqueta `v1.0`.
- `develop`: integra todos los cambios del desarrollo.
- Una rama por funcionalidad, fusionadas a `develop` sin borrarlas: `configuracion_proyecto`, `persistencia_csv`, `alta_doctor`, `alta_paciente`, `crear_cita`, `control_acceso` y `documentacion`.

## Documentación

La documentación completa (acerca de, descripción de las clases y guías) está en el **Wiki** del repositorio.

## Créditos

- [Tu nombre completo], estudiante.
- [Nombre de la materia y del profesor].

## Licencia

Este proyecto se distribuye bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE).
