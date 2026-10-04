package consultorio;

import consultorio.servicio.SistemaCitas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Recorre el programa completo con una entrada de teclado simulada.
 */
public class MainTest {

    @Rule
    public TemporaryFolder temporal = new TemporaryFolder();

    @Before
    public void crearAdministrador() throws Exception {
        SistemaCitas sistema = new SistemaCitas(db());
        sistema.cargarDatos();
        sistema.altaAdministrador("admin", "Administrador General", "clave123");
    }

    private Path db() {
        return temporal.getRoot().toPath().resolve("db");
    }

    /** Ejecuta el programa tal cual, sin agregar nada a la entrada. */
    private String ejecutarSinAcceso(Path carpetaDb, String... lineas) throws Exception {
        String entrada = String.join("\n", lineas) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Main.ejecutar(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, "UTF-8"), carpetaDb);
        return buffer.toString("UTF-8");
    }

    /** Ejecuta el programa iniciando sesión antes de las líneas del menú. */
    private String ejecutar(String... lineasDelMenu) throws Exception {
        List<String> lineas = new ArrayList<>(Arrays.asList("admin", "clave123"));
        lineas.addAll(Arrays.asList(lineasDelMenu));
        return ejecutarSinAcceso(db(), lineas.toArray(new String[0]));
    }

    // ------------------------------------------------------- control de acceso

    @Test
    public void conCredencialesCorrectasSeEntraAlMenu() throws Exception {
        String salida = ejecutar("0");
        assertTrue(salida.contains("Sesión iniciada como Administrador General."));
        assertTrue(salida.contains("1. Alta de doctor"));
    }

    @Test
    public void conCredencialesIncorrectasSeVuelveAPedirElAcceso() throws Exception {
        String salida = ejecutarSinAcceso(db(), "admin", "mala", "otro", "clave123", "admin", "clave123", "0");
        assertEquals(2, contar(salida, "[ERROR] Identificador o contraseña incorrectos."));
        assertTrue(salida.contains("Sesión iniciada como Administrador General."));
    }

    @Test
    public void sinIniciarSesionNoSeMuestraElMenu() throws Exception {
        String salida = ejecutarSinAcceso(db(), "admin", "mala");
        assertFalse(salida.contains("1. Alta de doctor"));
        assertTrue(salida.contains("Entrada terminada. Saliendo del programa."));
    }

    @Test
    public void laPrimeraVezSePideCrearUnAdministrador() throws Exception {
        Path nueva = temporal.getRoot().toPath().resolve("otra-db");
        String salida = ejecutarSinAcceso(nueva,
                "root", "Jefe de Sistemas", "secreto1", "secreto1",   // alta del primer administrador
                "root", "secreto1",                                   // inicio de sesión
                "0");
        assertTrue(salida.contains("No hay administradores registrados."));
        assertTrue(salida.contains("Administrador registrado correctamente."));
        assertTrue(salida.contains("Sesión iniciada como Jefe de Sistemas."));
    }

    @Test
    public void laCreacionDelPrimerAdministradorRepiteLaPreguntaSiHayError() throws Exception {
        Path nueva = temporal.getRoot().toPath().resolve("otra-db");
        String salida = ejecutarSinAcceso(nueva,
                "root", "Jefe", "123", "123",                 // contraseña muy corta
                "root", "Jefe", "secreto1", "distinta",       // no coinciden
                "root", "Jefe", "secreto1", "secreto1",       // correcto
                "root", "secreto1", "0");
        assertTrue(salida.contains("[ERROR] La contraseña debe tener al menos 6 caracteres."));
        assertTrue(salida.contains("[ERROR] Las contraseñas no coinciden."));
        assertTrue(salida.contains("Sesión iniciada como Jefe."));
    }

    @Test
    public void unAdministradorNuevoPuedeEntrarEnLaSiguienteEjecucion() throws Exception {
        String primera = ejecutar("5", "gerente", "Gerente Uno", "otraclave", "otraclave", "0");
        assertTrue(primera.contains("Administrador registrado correctamente."));
        String segunda = ejecutarSinAcceso(db(), "gerente", "otraclave", "0");
        assertTrue(segunda.contains("Sesión iniciada como Gerente Uno."));
    }

    @Test
    public void laContrasenaNoSeGuardaEnElArchivo() throws Exception {
        ejecutar("0");
        String contenido = new String(Files.readAllBytes(db().resolve("administradores.csv")), StandardCharsets.UTF_8);
        assertFalse(contenido.contains("clave123"));
    }

    // ----------------------------------------------------------------- menú

    @Test
    public void altaDeDoctorDesdeElMenu() throws Exception {
        String salida = ejecutar("1", "D1", "Ana Lopez", "Pediatria", "0");
        assertTrue(salida.contains("Doctor registrado correctamente."));
        assertTrue(Files.readAllLines(db().resolve("doctores.csv")).contains("D1,Ana Lopez,Pediatria"));
    }

    @Test
    public void altaDePacienteDesdeElMenu() throws Exception {
        String salida = ejecutar("2", "P1", "Maria Garcia", "0");
        assertTrue(salida.contains("Paciente registrado correctamente."));
        assertTrue(Files.readAllLines(db().resolve("pacientes.csv")).contains("P1,Maria Garcia"));
    }

    @Test
    public void crearYListarCitasDesdeElMenu() throws Exception {
        String salida = ejecutar(
                "1", "D1", "Ana Lopez", "Pediatria",
                "2", "P1", "Maria Garcia",
                "3", "C1", "15/01/2099 09:30", "Revision general", "D1", "P1",
                "4",
                "0");
        assertTrue(salida.contains("Cita registrada correctamente."));
        assertTrue(salida.contains("Cita C1 | 15/01/2099 09:30"));
        assertTrue(salida.contains("Doctor:   D1 - Ana Lopez (Pediatria)"));
        assertTrue(salida.contains("Paciente: P1 - Maria Garcia"));
        assertTrue(salida.contains("Total de citas: 1"));
    }

    @Test
    public void noSePuedeCrearUnaCitaSinDoctoresNiPacientes() throws Exception {
        String salida = ejecutar("3", "0");
        assertTrue(salida.contains("[ERROR] Para crear una cita primero debes registrar al menos un doctor y un paciente."));
    }

    @Test
    public void unaFechaInvalidaSeRechazaSinCerrarElPrograma() throws Exception {
        String salida = ejecutar(
                "1", "D1", "Ana", "Pediatria",
                "2", "P1", "Maria",
                "3", "C1", "31/02/2099 09:30",
                "4",
                "0");
        assertTrue(salida.contains("[ERROR] Fecha y hora no válidas."));
        assertTrue(salida.contains("No hay citas registradas."));
    }

    @Test
    public void unDobleAgendadoSeRechazaConUnMensaje() throws Exception {
        String salida = ejecutar(
                "1", "D1", "Ana", "Pediatria",
                "2", "P1", "Maria",
                "2", "P2", "Pedro",
                "3", "C1", "15/01/2099 09:30", "Revision", "D1", "P1",
                "3", "C2", "15/01/2099 09:30", "Control", "D1", "P2",
                "0");
        assertTrue(salida.contains("[ERROR] El doctor Ana ya tiene la cita C1 a esa hora."));
    }

    @Test
    public void lasCitasSeConservanEntreEjecuciones() throws Exception {
        ejecutar("1", "D1", "Ana", "Pediatria", "2", "P1", "Maria",
                "3", "C1", "15/01/2099 09:30", "Revision", "D1", "P1", "0");
        String segunda = ejecutar("4", "0");
        assertTrue(segunda.contains("Cita C1 | 15/01/2099 09:30"));
    }

    // ------------------------------------------------------ manejo de errores

    @Test
    public void unErrorNoCierraElProgramaYSeRegresaAlMenu() throws Exception {
        String salida = ejecutar("1", "D1", "Ana", "Pediatria", "1", "D1", "Otra", "Cardio", "0");
        assertTrue(salida.contains("[ERROR] Ya existe un doctor con el identificador D1."));
        assertTrue(salida.contains("Datos guardados. Hasta pronto."));
    }

    @Test
    public void opcionNoNumericaOFueraDeRangoSeRechaza() throws Exception {
        String salida = ejecutar("abc", "9", "0");
        assertTrue(salida.contains("[ERROR] Escribe un número entero"));
        assertTrue(salida.contains("[ERROR] Opción no válida: 9."));
        assertTrue(salida.contains("Datos guardados. Hasta pronto."));
    }

    @Test
    public void siSeTerminaLaEntradaElProgramaSaleSinCiclarse() throws Exception {
        String salida = ejecutar("1", "D1");
        assertTrue(salida.contains("Entrada terminada. Saliendo del programa."));
    }

    @Test
    public void siLaCarpetaDeDatosNoSePuedeUsarElProgramaExplicaElProblemaYTermina() throws Exception {
        Path archivo = temporal.newFile("esto-es-un-archivo").toPath();
        String salida = ejecutarSinAcceso(archivo, "0");
        assertTrue(salida.contains("[ERROR] No se pudieron cargar los datos"));
        assertTrue(salida.contains("El programa no puede continuar sin acceso a la carpeta de datos"));
    }

    // --------------------------------------------------- regeneración de db

    @Test
    public void losArchivosFaltantesSeRegeneranAlIniciar() throws Exception {
        ejecutar("0");
        for (String archivo : new String[]{"doctores.csv", "pacientes.csv", "citas.csv"}) {
            Files.delete(db().resolve(archivo));
        }
        ejecutar("0");
        assertEquals("id,nombreCompleto,especialidad", Files.readAllLines(db().resolve("doctores.csv")).get(0));
        assertEquals("id,nombreCompleto", Files.readAllLines(db().resolve("pacientes.csv")).get(0));
        assertEquals("id,fechaHora,motivo,idDoctor,idPaciente", Files.readAllLines(db().resolve("citas.csv")).get(0));
    }

    @Test
    public void siSeBorraTodaLaCarpetaDbSeRegeneraYSePideCrearAdministrador() throws Exception {
        ejecutar("0");
        for (java.io.File archivo : db().toFile().listFiles()) {
            assertTrue(archivo.delete());
        }
        assertTrue(db().toFile().delete());

        String salida = ejecutarSinAcceso(db(), "admin", "Nuevo Admin", "clave999", "clave999", "admin", "clave999", "0");
        assertTrue(salida.contains("No hay administradores registrados."));
        assertTrue(Files.exists(db().resolve("citas.csv")));
    }

    private static int contar(String texto, String fragmento) {
        int cuenta = 0;
        for (int i = texto.indexOf(fragmento); i >= 0; i = texto.indexOf(fragmento, i + 1)) {
            cuenta++;
        }
        return cuenta;
    }
}
