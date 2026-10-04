#!/usr/bin/env bash
# Genera el FAT JAR usando solo las herramientas del JDK (sin Maven).
# Uso: ./compilar.sh      Resultado: target/sistema-citas-1.0.jar
set -e
cd "$(dirname "$0")"

NOMBRE="sistema-citas-1.0"
CLASE_PRINCIPAL="consultorio.Main"

rm -rf build target
mkdir -p build/classes target

echo "Compilando para Java 11..."
find src/main/java -name "*.java" > build/fuentes.txt
javac --release 11 -encoding UTF-8 -d build/classes @build/fuentes.txt

# FAT JAR: si existen dependencias en lib/, se descomprimen dentro del JAR final.
if [ -d lib ]; then
    for dependencia in lib/*.jar; do
        [ -e "$dependencia" ] || continue
        echo "Incluyendo dependencia: $dependencia"
        (cd build/classes && jar xf "../../$dependencia")
    done
fi

printf 'Main-Class: %s\n' "$CLASE_PRINCIPAL" > build/manifest.txt
jar --create --file "target/$NOMBRE.jar" --manifest build/manifest.txt -C build/classes .

echo "Listo: target/$NOMBRE.jar"
echo "Ejecutar con: java -jar target/$NOMBRE.jar"
