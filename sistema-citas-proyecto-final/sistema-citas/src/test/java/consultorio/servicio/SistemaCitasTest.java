package consultorio.servicio;

import consultorio.excepciones.SistemaCitasException;
import consultorio.modelo.Cita;
import consultorio.modelo.Doctor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SistemaCitasTest {

    @Rule
    public TemporaryFolder temporal = new TemporaryFolder();

    private Path carpetaDb;
    private SistemaCitas sistema;

    @Before
    public void preparar() throws Exception {
        carpetaDb = temporal.getRoot().toPath().resolve("db");
        sistema = new SistemaCitas(carpetaDb);
        sistema.cargarDatos();
    }

    // ---------------------------------------------------------------- doctores

    @Test
    public void cargarDatosCreaLaCarpetaYLosArchivosFaltantes() {
        assertTrue(Files.exists(carpetaDb.resolve(SistemaCitas.ARCHIVO_DOCTORES)));
        assertTrue(Files.exists(carpetaDb.resolve(SistemaCitas.ARCHIVO_PACIENTES)));
        assertTrue(Files.exists(carpetaDb.resolve(SistemaCitas.ARCHIVO_CITAS)));
        assertTrue(Files.exists(carpetaDb.resolve(SistemaCitas.ARCHIVO_ADMINISTRADORES)));
    }

    // ----------------------------------------------------------- administradores

    @Test
    public void alIniciarSinArchivosNoHayAdministradores() {
        assertFalse(sistema.hayAdministradores());
    }

    @Test
    public void iniciaSesionConCredencialesCorrectas() throws Exception {
        sistema.altaAdministrador("admin", "Administrador General", "clave123");
        assertNotNull(sistema.iniciarSesion("admin", "clave123"));
    }

    @Test
    public void noIniciaSesionConCredencialesIncorrectas() throws Exception {
        sistema.altaAdministrador("admin", "Administrador General", "clave123");
        assertNull(sistema.iniciarSesion("admin", "mala"));
        assertNull(sistema.iniciarSesion("nadie", "clave123"));
        assertNull(sistema.iniciarSesion(null, null));
    }

    @Test
    public void losAdministradoresSobrevivenAUnReinicio() throws Exception {
        sistema.altaAdministrador("admin", "Administrador General", "clave123");

        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertTrue(otraSesion.hayAdministradores());
        assertNotNull(otraSesion.iniciarSesion("admin", "clave123"));
    }

    @Test
    public void laContrasenaNoSeGuardaEnTextoPlano() throws Exception {
        sistema.altaAdministrador("admin", "Administrador General", "clave123");
        String contenido = new String(Files.readAllBytes(carpetaDb.resolve(SistemaCitas.ARCHIVO_ADMINISTRADORES)),
                StandardCharsets.UTF_8);
        assertFalse(contenido.contains("clave123"));
    }

    @Test(expected = SistemaCitasException.class)
    public void noPermiteIdentificadorDeAdministradorRepetido() throws Exception {
        sistema.altaAdministrador("admin", "Administrador General", "clave123");
        sistema.altaAdministrador("ADMIN", "Otro", "clave456");
    }

    @Test
    public void unAdministradorInvalidoNoSeAgrega() {
        try {
            sistema.altaAdministrador("admin", "Administrador General", "123");
        } catch (SistemaCitasException esperada) {
            // se espera el error
        }
        assertFalse(sistema.hayAdministradores());
    }

    @Test
    public void altaDoctorLoGuardaEnElArchivo() throws Exception {
        sistema.altaDoctor("D1", "Ana López", "Pediatría");
        assertEquals(Arrays.asList("id,nombreCompleto,especialidad", "D1,Ana López,Pediatría"),
                Files.readAllLines(carpetaDb.resolve(SistemaCitas.ARCHIVO_DOCTORES), StandardCharsets.UTF_8));
    }

    @Test
    public void losDoctoresSobrevivenAUnReinicio() throws Exception {
        sistema.altaDoctor("D1", "Ana López", "Pediatría");

        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertEquals(1, otraSesion.getDoctores().size());
        assertEquals("Pediatría", otraSesion.buscarDoctor("D1").getEspecialidad());
    }

    @Test(expected = SistemaCitasException.class)
    public void noPermiteIdentificadorDeDoctorRepetido() throws Exception {
        sistema.altaDoctor("D1", "Ana López", "Pediatría");
        sistema.altaDoctor("D1", "Otro Doctor", "Cardiología");
    }

    @Test(expected = SistemaCitasException.class)
    public void elIdentificadorRepetidoNoDistingueMayusculas() throws Exception {
        sistema.altaDoctor("D1", "Ana López", "Pediatría");
        sistema.altaDoctor("d1", "Otro Doctor", "Cardiología");
    }

    @Test
    public void unAltaConDatosInvalidosNoAgregaNada() {
        try {
            sistema.altaDoctor("D1", "", "Pediatría");
        } catch (SistemaCitasException esperada) {
            // se espera el error
        }
        assertTrue(sistema.getDoctores().isEmpty());
    }

    @Test
    public void siNoSePuedeGuardarElDoctorNoQuedaEnMemoria() throws Exception {
        Path archivo = temporal.newFile("no-es-carpeta").toPath();
        SistemaCitas roto = new SistemaCitas(archivo);
        try {
            roto.altaDoctor("D1", "Ana López", "Pediatría");
        } catch (SistemaCitasException esperada) {
            // se espera el error
        }
        assertNull(roto.buscarDoctor("D1"));
    }

    @Test
    public void buscarDoctorNoDistingueMayusculasNiEspacios() throws Exception {
        sistema.altaDoctor("Dr-1", "Ana López", "Pediatría");
        assertNotNull(sistema.buscarDoctor("  dr-1 "));
        assertNull(sistema.buscarDoctor("otro"));
        assertNull(sistema.buscarDoctor(null));
    }

    @Test
    public void unaLineaDanadaSeOmiteYSeAvisa() throws Exception {
        Files.write(carpetaDb.resolve(SistemaCitas.ARCHIVO_DOCTORES),
                Arrays.asList("id,nombreCompleto,especialidad", "D1,Ana López,Pediatría", "D2,solo-dos"),
                StandardCharsets.UTF_8);
        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertEquals(1, otraSesion.getDoctores().size());
        assertEquals(1, otraSesion.getAdvertencias().size());
    }

    @Test
    public void unIdentificadorRepetidoEnElArchivoSeOmiteYSeAvisa() throws Exception {
        Files.write(carpetaDb.resolve(SistemaCitas.ARCHIVO_DOCTORES),
                Arrays.asList("id,nombreCompleto,especialidad", "D1,Ana,Pediatría", "D1,Luis,Cardiología"),
                StandardCharsets.UTF_8);
        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertEquals(1, otraSesion.getDoctores().size());
        assertEquals("Ana", otraSesion.buscarDoctor("D1").getNombreCompleto());
        assertEquals(1, otraSesion.getAdvertencias().size());
    }

    // --------------------------------------------------------------- pacientes

    @Test
    public void altaPacienteLoGuardaEnElArchivo() throws Exception {
        sistema.altaPaciente("P1", "María García");
        assertEquals(Arrays.asList("id,nombreCompleto", "P1,María García"),
                Files.readAllLines(carpetaDb.resolve(SistemaCitas.ARCHIVO_PACIENTES), StandardCharsets.UTF_8));
    }

    @Test
    public void losPacientesSobrevivenAUnReinicio() throws Exception {
        sistema.altaPaciente("P1", "María García");

        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertEquals(1, otraSesion.getPacientes().size());
        assertEquals("María García", otraSesion.buscarPaciente("p1").getNombreCompleto());
    }

    @Test(expected = SistemaCitasException.class)
    public void noPermiteIdentificadorDePacienteRepetido() throws Exception {
        sistema.altaPaciente("P1", "María García");
        sistema.altaPaciente("p1", "Otra Persona");
    }

    @Test
    public void unDoctorYUnPacientePuedenCompartirIdentificador() throws Exception {
        sistema.altaDoctor("1", "Ana López", "Pediatría");
        sistema.altaPaciente("1", "María García");
        assertEquals(1, sistema.getDoctores().size());
        assertEquals(1, sistema.getPacientes().size());
    }

    @Test
    public void siNoSePuedeGuardarElPacienteNoQuedaEnMemoria() throws Exception {
        Path archivo = temporal.newFile("no-es-carpeta").toPath();
        SistemaCitas roto = new SistemaCitas(archivo);
        try {
            roto.altaPaciente("P1", "María García");
        } catch (SistemaCitasException esperada) {
            // se espera el error
        }
        assertNull(roto.buscarPaciente("P1"));
    }

    // ------------------------------------------------------------------- citas

    private static final LocalDateTime FUTURO = LocalDateTime.of(2099, 1, 15, 9, 30);

    private void registrarDosDoctoresYDosPacientes() throws Exception {
        sistema.altaDoctor("D1", "Ana López", "Pediatría");
        sistema.altaDoctor("D2", "Luis Mora", "Cardiología");
        sistema.altaPaciente("P1", "María García");
        sistema.altaPaciente("P2", "Pedro Ruiz");
    }

    @Test
    public void crearCitaLaRelacionaConSuDoctorYSuPaciente() throws Exception {
        registrarDosDoctoresYDosPacientes();
        Cita cita = sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P1");
        assertEquals("Ana López", cita.getDoctor().getNombreCompleto());
        assertEquals("María García", cita.getPaciente().getNombreCompleto());
        assertEquals(Arrays.asList("id,fechaHora,motivo,idDoctor,idPaciente", "C1,2099-01-15 09:30,Revisión,D1,P1"),
                Files.readAllLines(carpetaDb.resolve(SistemaCitas.ARCHIVO_CITAS), StandardCharsets.UTF_8));
    }

    @Test
    public void lasCitasYSusRelacionesSobrevivenAUnReinicio() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D2", "P2");

        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        Cita cita = otraSesion.buscarCita("c1");
        assertNotNull(cita);
        assertEquals("Luis Mora", cita.getDoctor().getNombreCompleto());
        assertEquals("Pedro Ruiz", cita.getPaciente().getNombreCompleto());
        assertTrue(otraSesion.getAdvertencias().isEmpty());
    }

    @Test
    public void sePuedenCrearVariasCitas() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P1");
        sistema.crearCita("C2", FUTURO.plusHours(1), "Control", "D1", "P1");
        sistema.crearCita("C3", FUTURO, "Análisis", "D2", "P2");
        assertEquals(3, sistema.listarCitas().size());
    }

    @Test
    public void listarCitasLasOrdenaPorFechaYHora() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO.plusDays(2), "Tercera", "D1", "P1");
        sistema.crearCita("C2", FUTURO, "Primera", "D1", "P1");
        sistema.crearCita("C3", FUTURO.plusDays(1), "Segunda", "D1", "P1");
        List<Cita> citas = sistema.listarCitas();
        assertEquals("C2", citas.get(0).getId());
        assertEquals("C3", citas.get(1).getId());
        assertEquals("C1", citas.get(2).getId());
    }

    @Test(expected = SistemaCitasException.class)
    public void noCreaCitaConDoctorInexistente() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D9", "P1");
    }

    @Test(expected = SistemaCitasException.class)
    public void noCreaCitaConPacienteInexistente() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P9");
    }

    @Test(expected = SistemaCitasException.class)
    public void noPermiteIdentificadorDeCitaRepetido() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P1");
        sistema.crearCita("c1", FUTURO.plusDays(1), "Otra", "D2", "P2");
    }

    @Test(expected = SistemaCitasException.class)
    public void noCreaCitasEnUnaFechaPasada() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", LocalDateTime.of(2000, 1, 1, 8, 0), "Revisión", "D1", "P1");
    }

    @Test
    public void unDoctorNoPuedeTenerDosCitasALaMismaHora() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P1");
        try {
            sistema.crearCita("C2", FUTURO, "Control", "D1", "P2");
            throw new AssertionError("Debió rechazar la cita");
        } catch (SistemaCitasException esperada) {
            assertTrue(esperada.getMessage().contains("El doctor Ana López ya tiene la cita C1"));
        }
        assertEquals(1, sistema.listarCitas().size());
    }

    @Test
    public void unPacienteNoPuedeTenerDosCitasALaMismaHora() throws Exception {
        registrarDosDoctoresYDosPacientes();
        sistema.crearCita("C1", FUTURO, "Revisión", "D1", "P1");
        try {
            sistema.crearCita("C2", FUTURO, "Control", "D2", "P1");
            throw new AssertionError("Debió rechazar la cita");
        } catch (SistemaCitasException esperada) {
            assertTrue(esperada.getMessage().contains("El paciente María García ya tiene la cita C1"));
        }
    }

    @Test
    public void unaCitaRechazadaNoSeGuardaEnElArchivo() throws Exception {
        registrarDosDoctoresYDosPacientes();
        try {
            sistema.crearCita("C1", FUTURO, "", "D1", "P1");
        } catch (SistemaCitasException esperada) {
            // se espera el error
        }
        assertEquals(1, Files.readAllLines(carpetaDb.resolve(SistemaCitas.ARCHIVO_CITAS)).size());
    }

    @Test
    public void unaCitaConDoctorInexistenteEnElArchivoSeOmiteYSeAvisa() throws Exception {
        registrarDosDoctoresYDosPacientes();
        Files.write(carpetaDb.resolve(SistemaCitas.ARCHIVO_CITAS),
                Arrays.asList("id,fechaHora,motivo,idDoctor,idPaciente",
                        "C1,2099-01-15 09:30,Revisión,D1,P1",
                        "C2,2099-01-15 10:30,Revisión,D77,P1"),
                StandardCharsets.UTF_8);
        SistemaCitas otraSesion = new SistemaCitas(carpetaDb);
        otraSesion.cargarDatos();
        assertEquals(1, otraSesion.listarCitas().size());
        assertEquals(1, otraSesion.getAdvertencias().size());
        assertTrue(otraSesion.getAdvertencias().get(0).contains("doctor que no existe"));
    }

    @Test
    public void getDoctoresNoSePuedeModificarDesdeFuera() throws Exception {
        Doctor doctor = sistema.altaDoctor("D1", "Ana López", "Pediatría");
        try {
            sistema.getDoctores().add(doctor);
        } catch (UnsupportedOperationException esperada) {
            return;
        }
        throw new AssertionError("La lista debería ser de solo lectura");
    }
}
