@echo off
REM Genera el FAT JAR usando solo las herramientas del JDK (sin Maven).
REM Uso: compilar.bat      Resultado: target\sistema-citas-1.0.jar
setlocal
cd /d "%~dp0"

set NOMBRE=sistema-citas-1.0
set CLASE_PRINCIPAL=consultorio.Main

if exist build rmdir /s /q build
if exist target rmdir /s /q target
mkdir build\classes
mkdir target

echo Compilando para Java 11...
dir /s /b src\main\java\*.java > build\fuentes.txt
javac --release 11 -encoding UTF-8 -d build\classes @build\fuentes.txt
if errorlevel 1 exit /b 1

REM FAT JAR: si existen dependencias en lib\, se descomprimen dentro del JAR final.
if exist lib (
    for %%J in (lib\*.jar) do (
        echo Incluyendo dependencia: %%J
        pushd build\classes
        jar xf "..\..\%%J"
        popd
    )
)

echo Main-Class: %CLASE_PRINCIPAL%> build\manifest.txt
jar --create --file target\%NOMBRE%.jar --manifest build\manifest.txt -C build\classes .
if errorlevel 1 exit /b 1

echo Listo: target\%NOMBRE%.jar
echo Ejecutar con: java -jar target\%NOMBRE%.jar
endlocal
