package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PacienteTest {

    @Test
    public void aCSVYDesdeCSVSonInversos() throws SistemaCitasException {
        Paciente original = new Paciente("P1", "García, María");
        Paciente copia = Paciente.desdeCSV(original.aCSV());
        assertEquals("P1", copia.getId());
        assertEquals("García, María", copia.getNombreCompleto());
    }

    @Test(expected = SistemaCitasException.class)
    public void identificadorVacioEsInvalido() throws SistemaCitasException {
        new Paciente(" ", "María");
    }

    @Test(expected = SistemaCitasException.class)
    public void nombreMuyLargoEsInvalido() throws SistemaCitasException {
        new Paciente("P1", new String(new char[81]).replace('\0', 'a'));
    }

    @Test(expected = SistemaCitasException.class)
    public void lineaConUnaSolaColumnaEsInvalida() throws SistemaCitasException {
        Paciente.desdeCSV("P1");
    }
}
