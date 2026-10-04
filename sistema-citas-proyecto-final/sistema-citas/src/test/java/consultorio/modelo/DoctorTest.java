package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DoctorTest {

    @Test
    public void aCSVYDesdeCSVSonInversos() throws SistemaCitasException {
        Doctor original = new Doctor("D1", "Pérez, Juan", "Cardiología");
        Doctor copia = Doctor.desdeCSV(original.aCSV());
        assertEquals("D1", copia.getId());
        assertEquals("Pérez, Juan", copia.getNombreCompleto());
        assertEquals("Cardiología", copia.getEspecialidad());
    }

    @Test
    public void elConstructorQuitaEspaciosSobrantes() throws SistemaCitasException {
        Doctor doctor = new Doctor("  D1 ", "  Ana López ", " Pediatría ");
        assertEquals("D1", doctor.getId());
        assertEquals("Ana López", doctor.getNombreCompleto());
        assertEquals("Pediatría", doctor.getEspecialidad());
    }

    @Test(expected = SistemaCitasException.class)
    public void identificadorVacioEsInvalido() throws SistemaCitasException {
        new Doctor("", "Ana", "Pediatría");
    }

    @Test(expected = SistemaCitasException.class)
    public void identificadorConCaracteresRarosEsInvalido() throws SistemaCitasException {
        new Doctor("D 1;", "Ana", "Pediatría");
    }

    @Test(expected = SistemaCitasException.class)
    public void nombreVacioEsInvalido() throws SistemaCitasException {
        new Doctor("D1", "   ", "Pediatría");
    }

    @Test(expected = SistemaCitasException.class)
    public void especialidadVaciaEsInvalida() throws SistemaCitasException {
        new Doctor("D1", "Ana", "");
    }

    @Test(expected = SistemaCitasException.class)
    public void lineaConColumnasDeMasEsInvalida() throws SistemaCitasException {
        Doctor.desdeCSV("D1,Ana,Pediatría,extra");
    }
}
