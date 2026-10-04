package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;
import consultorio.modelo.Almacenable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda y lee objetos en un archivo de texto plano con formato CSV (UTF-8).
 * La primera línea del archivo es el encabezado con los nombres de las columnas.
 *
 * <ul>
 *   <li>Si la carpeta o el archivo no existen, {@link #inicializar()} los vuelve a crear.</li>
 *   <li>Las líneas dañadas no detienen la carga: se omiten y se registran en
 *       {@link #getAdvertencias()}.</li>
 *   <li>Al guardar se escribe primero un archivo temporal y después se reemplaza el
 *       original, para no dejarlo a medias si algo falla.</li>
 * </ul>
 *
 * @param <T> tipo de objeto que se guarda
 */
public class RepositorioCSV<T extends Almacenable> implements Repositorio<T> {

    private static final String BOM = "﻿";

    private final Path ruta;
    private final String encabezado;
    private final Convertidor<T> convertidor;
    private final List<String> advertencias = new ArrayList<>();

    /**
     * @param ruta        ubicación del archivo CSV
     * @param encabezado  primera línea del archivo, con los nombres de las columnas
     * @param convertidor convierte cada línea del archivo en un objeto
     */
    public RepositorioCSV(Path ruta, String encabezado, Convertidor<T> convertidor) {
        this.ruta = ruta;
        this.encabezado = encabezado;
        this.convertidor = convertidor;
    }

    @Override
    public void inicializar() throws SistemaCitasException {
        try {
            Path carpeta = ruta.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            if (Files.isDirectory(ruta)) {
                throw new SistemaCitasException("La ruta " + ruta + " es una carpeta, no un archivo.");
            }
            if (!Files.exists(ruta)) {
                escribir(Collections.<T>emptyList());
            }
        } catch (IOException e) {
            throw new SistemaCitasException("No se pudo preparar el archivo " + ruta.getFileName() + ".", e);
        }
    }

    @Override
    public void guardarTodos(List<T> lista) throws SistemaCitasException {
        try {
            Path carpeta = ruta.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            escribir(lista);
        } catch (IOException e) {
            throw new SistemaCitasException("No se pudo guardar el archivo " + ruta.getFileName() + ".", e);
        }
    }

    @Override
    public List<T> cargarTodos() throws SistemaCitasException {
        inicializar();
        advertencias.clear();
        List<T> resultado = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (numero == 1 && linea.startsWith(BOM)) {
                    linea = linea.substring(1);
                }
                if (linea.trim().isEmpty() || (numero == 1 && linea.trim().equals(encabezado))) {
                    continue;
                }
                try {
                    resultado.add(convertidor.convertir(linea));
                } catch (SistemaCitasException e) {
                    advertencias.add(ruta.getFileName() + ", línea " + numero + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new SistemaCitasException("No se pudo leer el archivo " + ruta.getFileName() + ".", e);
        }
        return resultado;
    }

    /**
     * @return avisos sobre las líneas que se omitieron en la última carga
     */
    public List<String> getAdvertencias() {
        return Collections.unmodifiableList(new ArrayList<>(advertencias));
    }

    public Path getRuta() {
        return ruta;
    }

    private void escribir(List<T> lista) throws IOException {
        Path temporal = ruta.resolveSibling(ruta.getFileName() + ".tmp");
        try (BufferedWriter escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
            escritor.write(encabezado);
            escritor.newLine();
            for (T elemento : lista) {
                escritor.write(elemento.aCSV());
                escritor.newLine();
            }
        }
        try {
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
