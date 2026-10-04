package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;
import consultorio.modelo.Almacenable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RepositorioCSVTest {

    @Rule
    public TemporaryFolder temporal = new TemporaryFolder();

    /** Objeto sencillo que sirve solo para probar el repositorio. */
    private static class Elemento implements Almacenable {
        final String id;
        final String nombre;

        Elemento(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String aCSV() {
            return UtilCSV.unir(id, nombre);
        }
    }

    private static final Convertidor<Elemento> CONVERTIDOR = linea -> {
        List<String> campos = UtilCSV.dividir(linea);
        if (campos.size() != 2) {
            throw new SistemaCitasException("Se esperaban 2 columnas y hay " + campos.size() + ".");
        }
        return new Elemento(campos.get(0), campos.get(1));
    };

    private RepositorioCSV<Elemento> crear(Path ruta) {
        return new RepositorioCSV<>(ruta, "id,nombre", CONVERTIDOR);
    }

    @Test
    public void inicializarCreaCarpetaYArchivoConEncabezado() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("db").resolve("datos.csv");
        crear(ruta).inicializar();
        assertTrue(Files.exists(ruta));
        assertEquals(Collections.singletonList("id,nombre"), Files.readAllLines(ruta, StandardCharsets.UTF_8));
    }

    @Test
    public void guardarYCargarConservaLosDatos() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("datos.csv");
        RepositorioCSV<Elemento> repositorio = crear(ruta);
        repositorio.guardarTodos(Arrays.asList(new Elemento("1", "Ana"), new Elemento("2", "López, Luis")));

        List<Elemento> cargados = repositorio.cargarTodos();
        assertEquals(2, cargados.size());
        assertEquals("López, Luis", cargados.get(1).nombre);
    }

    @Test
    public void cargarRegeneraElArchivoSiFalta() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("db").resolve("faltante.csv");
        List<Elemento> cargados = crear(ruta).cargarTodos();
        assertTrue(cargados.isEmpty());
        assertTrue(Files.exists(ruta));
    }

    @Test
    public void lineasDanadasSeOmitenYSeRegistranComoAdvertencia() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("datos.csv");
        Files.write(ruta, Arrays.asList("id,nombre", "1,Ana", "linea-con-una-sola-columna", "", "3,Carlos"),
                StandardCharsets.UTF_8);
        RepositorioCSV<Elemento> repositorio = crear(ruta);

        List<Elemento> cargados = repositorio.cargarTodos();
        assertEquals(2, cargados.size());
        assertEquals(1, repositorio.getAdvertencias().size());
        assertTrue(repositorio.getAdvertencias().get(0).contains("línea 3"));
    }

    @Test
    public void ignoraLaMarcaBOMDeLosEditoresDeWindows() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("datos.csv");
        Files.write(ruta, "﻿id,nombre\n1,Ana\n".getBytes(StandardCharsets.UTF_8));
        List<Elemento> cargados = crear(ruta).cargarTodos();
        assertEquals(1, cargados.size());
    }

    @Test
    public void guardarNoDejaArchivosTemporales() throws Exception {
        Path ruta = temporal.getRoot().toPath().resolve("datos.csv");
        crear(ruta).guardarTodos(Collections.singletonList(new Elemento("1", "Ana")));
        File[] archivos = temporal.getRoot().listFiles();
        assertEquals(1, archivos.length);
    }

    @Test(expected = SistemaCitasException.class)
    public void siLaRutaEsUnaCarpetaLanzaExcepcion() throws Exception {
        Path carpeta = temporal.newFolder("carpeta").toPath();
        crear(carpeta).cargarTodos();
    }

    @Test
    public void guardarCreaLaCarpetaSiNoExiste() throws IOException, SistemaCitasException {
        Path ruta = temporal.getRoot().toPath().resolve("nueva").resolve("datos.csv");
        crear(ruta).guardarTodos(Collections.<Elemento>emptyList());
        assertTrue(Files.exists(ruta));
    }
}
