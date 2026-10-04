# Guías

## Configurar el ambiente de desarrollo

1. **Instala JDK 11** (o superior). Comprueba la instalación:
   ```bash
   java -version
   javac -version
   ```
2. **Instala Git** y comprueba la instalación con `git --version`. Configura tu nombre y correo:
   ```bash
   git config --global user.name "Tu Nombre"
   git config --global user.email "tu-correo@ejemplo.com"
   ```
3. **Instala IntelliJ IDEA** (Community es suficiente).
4. **Clona el repositorio** y cambia a la rama de desarrollo:
   ```bash
   git clone [URL del repositorio]
   cd [nombre-del-repositorio]
   git checkout develop
   ```
5. **Abre el proyecto** en IntelliJ con **File → Open** y elige la carpeta (o el archivo `pom.xml`). IntelliJ importará el proyecto Maven y descargará lo necesario.
6. **Configura el SDK** en **File → Project Structure → Project → SDK**: selecciona JDK 11 o superior.

## Ejecutar el programa

### Desde IntelliJ IDEA

1. Abre `src/main/java/consultorio/Main.java`.
2. Haz clic en el triángulo verde junto a `main` y elige **Run 'Main.main()'**.
3. La carpeta `db` se crea en la carpeta del proyecto la primera vez.

En la ventana *Run* de IntelliJ la contraseña se ve mientras se escribe, porque no es una terminal real. En una terminal se oculta.

### Desde el JAR

```bash
java -jar target/sistema-citas-1.0.jar
```

La carpeta `db` se crea en la carpeta desde donde ejecutas el comando. Para usar otra:

```bash
java -jar target/sistema-citas-1.0.jar /ruta/a/mis-datos
```

### Primera ejecución

1. El programa avisa que no hay administradores y pide crear el primero (identificador, nombre completo y contraseña de al menos 6 caracteres, repetida).
2. Inicia sesión con ese identificador y contraseña.
3. Registra al menos un doctor y un paciente (opciones 1 y 2) antes de crear citas (opción 3).

## Crear el JAR ejecutable (FAT JAR)

El FAT JAR es un solo archivo con el programa y todas sus dependencias. Funciona en cualquier sistema operativo que tenga Java.

### Con Maven

```bash
mvn clean package
```

El plugin `maven-shade-plugin`, configurado en `pom.xml`, genera `target/sistema-citas-1.0.jar` con el atributo `Main-Class` ya definido. En IntelliJ también puedes abrir la ventana **Maven** y ejecutar `package` en **Lifecycle**.

### Sin Maven

Solo se necesita el JDK:

```bash
./compilar.sh     # Linux y macOS
compilar.bat      # Windows
```

El script compila para Java 11, incluye las dependencias que haya en una carpeta `lib/` (si existe) y genera el mismo archivo `target/sistema-citas-1.0.jar`.

## Ejecutar las pruebas

```bash
mvn test
```

Las pruebas usan carpetas temporales: no tocan la carpeta `db` real.

## Flujo de trabajo con Git

1. Parte de `develop`: `git checkout develop`.
2. Crea una rama por funcionalidad: `git checkout -b nombre_funcionalidad`.
3. Haz commits pequeños con mensajes claros.
4. Fusiona a `develop` **sin borrar** la rama de origen:
   ```bash
   git checkout develop
   git merge --no-ff nombre_funcionalidad
   ```
5. Cuando `develop` esté estable, fusiónala a `master` y crea la etiqueta de versión:
   ```bash
   git checkout master
   git merge --no-ff develop
   git tag -a v1.0 -m "Versión estable 1.0"
   git push origin master --tags
   ```

## Solución de problemas

| Problema | Qué hacer |
|---|---|
| `java: command not found` | Instala el JDK y agrega su carpeta `bin` a la variable de entorno `PATH` |
| `UnsupportedClassVersionError` | Se está usando una versión de Java anterior a 11. Instala JDK 11 o superior |
| Los acentos se ven como `?` o símbolos raros en Windows | Ejecuta con `java -Dfile.encoding=UTF-8 -jar target/sistema-citas-1.0.jar` o, en `cmd`, escribe antes `chcp 65001`. Los archivos CSV siempre se guardan en UTF-8 |
| `No se pudieron cargar los datos` | Revisa que la carpeta `db` exista o se pueda crear y que tengas permisos de lectura y escritura |
| Un `[AVISO]` al iniciar | Alguna línea de un CSV estaba dañada o repetida y se omitió. Revisa el archivo que indica el aviso |
| Olvidaste la contraseña del único administrador | Borra `db/administradores.csv`: el programa lo regenera y te pedirá crear un administrador nuevo |
