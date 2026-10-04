package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CitaTest {

    private Doctor doctor;
    private Paciente paciente;

    @Before
    public void preparar() throws SistemaCitasException {
        doctor = new Doctor("D1", "Ana López", "Pediatría");
        paciente = new Paciente("P1", "María García");
    }

    @Test
    public void aCSVGuardaSoloLosIdentificadoresDeDoctorYPaciente() throws SistemaCitasException {
        Cita cita = new Cita("C1", LocalDateTime.of(2030, 5, 20, 9, 30), "Revisión, general", doctor, paciente);
        assertEquals("C1,2030-05-20 09:30,\"Revisión, general\",D1,P1", cita.aCSV());
    }

    @Test
    public void desdeCSVRecuperaLaCitaYSusRelaciones() throws SistemaCitasException {
        Cita original = new Cita("C1", LocalDateTime.of(2030, 5, 20, 9, 30), "Revisión, general", doctor, paciente);
        Cita copia = Cita.desdeCSV(original.aCSV(), id -> doctor, id -> paciente);
        assertEquals("C1", copia.getId());
        assertEquals(LocalDateTime.of(2030, 5, 20, 9, 30), copia.getFechaHora());
        assertEquals("Revisión, general", copia.getMotivo());
        assertSame(doctor, copia.getDoctor());
        assertSame(paciente, copia.getPaciente());
    }

    @Test(expected = SistemaCitasException.class)
    public void desdeCSVConDoctorInexistenteLanzaExcepcion() throws SistemaCitasException {
        Cita.desdeCSV("C1,2030-05-20 09:30,Revisión,D9,P1", id -> null, id -> paciente);
    }

    @Test(expected = SistemaCitasException.class)
    public void desdeCSVConPacienteInexistenteLanzaExcepcion() throws SistemaCitasException {
        Cita.desdeCSV("C1,2030-05-20 09:30,Revisión,D1,P9", id -> doctor, id -> null);
    }

    @Test(expected = SistemaCitasException.class)
    public void desdeCSVConFechaInvalidaLanzaExcepcion() throws SistemaCitasException {
        Cita.desdeCSV("C1,ayer,Revisión,D1,P1", id -> doctor, id -> paciente);
    }

    @Test(expected = SistemaCitasException.class)
    public void desdeCSVConColumnasDeMenosLanzaExcepcion() throws SistemaCitasException {
        Cita.desdeCSV("C1,2030-05-20 09:30,Revisión", id -> doctor, id -> paciente);
    }

    @Test
    public void parsearFechaHoraAceptaElFormatoDelUsuario() throws SistemaCitasException {
        assertEquals(LocalDateTime.of(2030, 10, 15, 9, 30), Cita.parsearFechaHora(" 15/10/2030 09:30 "));
    }

    @Test(expected = SistemaCitasException.class)
    public void parsearFechaHoraRechazaUnDiaQueNoExiste() throws SistemaCitasException {
        Cita.parsearFechaHora("31/02/2030 09:30");
    }

    @Test(expected = SistemaCitasException.class)
    public void parsearFechaHoraRechazaOtroFormato() throws SistemaCitasException {
        Cita.parsearFechaHora("2030-10-15 09:30");
    }

    @Test(expected = SistemaCitasException.class)
    public void parsearFechaHoraRechazaTextoVacio() throws SistemaCitasException {
        Cita.parsearFechaHora("");
    }

    @Test(expected = SistemaCitasException.class)
    public void laCitaExigeMotivo() throws SistemaCitasException {
        new Cita("C1", LocalDateTime.of(2030, 5, 20, 9, 30), " ", doctor, paciente);
    }

    @Test(expected = SistemaCitasException.class)
    public void laCitaExigeDoctor() throws SistemaCitasException {
        new Cita("C1", LocalDateTime.of(2030, 5, 20, 9, 30), "Revisión", null, paciente);
    }

    @Test(expected = SistemaCitasException.class)
    public void laCitaExigePaciente() throws SistemaCitasException {
        new Cita("C1", LocalDateTime.of(2030, 5, 20, 9, 30), "Revisión", doctor, null);
    }

    @Test(expected = SistemaCitasException.class)
    public void laCitaExigeFecha() throws SistemaCitasException {
        new Cita("C1", null, "Revisión", doctor, paciente);
    }
}
