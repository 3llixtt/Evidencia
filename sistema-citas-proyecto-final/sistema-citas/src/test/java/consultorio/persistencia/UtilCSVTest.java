package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilCSVTest {

    @Test
    public void camposSimplesNoLlevanComillas() {
        assertEquals("a,b,c", UtilCSV.unir("a", "b", "c"));
    }

    @Test
    public void camposConComasYComillasSeEscapanYSeRecuperan() throws SistemaCitasException {
        String linea = UtilCSV.unir("D-1", "Pérez, Juan \"el doctor\"", "Cardiología");
        List<String> campos = UtilCSV.dividir(linea);
        assertEquals(Arrays.asList("D-1", "Pérez, Juan \"el doctor\"", "Cardiología"), campos);
    }

    @Test
    public void campoVacioSeConservaEnSuPosicion() throws SistemaCitasException {
        assertEquals(Arrays.asList("a", "", "c"), UtilCSV.dividir("a,,c"));
    }

    @Test
    public void nuloSeEscribeVacio() {
        assertEquals("", UtilCSV.escapar(null));
    }

    @Test
    public void saltosDeLineaSeReemplazanPorEspacios() {
        assertEquals("uno dos", UtilCSV.escapar("uno\ndos"));
    }

    @Test(expected = SistemaCitasException.class)
    public void comillasSinCerrarLanzanExcepcion() throws SistemaCitasException {
        UtilCSV.dividir("a,\"b,c");
    }

    @Test(expected = SistemaCitasException.class)
    public void textoDespuesDeCerrarComillasLanzaExcepcion() throws SistemaCitasException {
        UtilCSV.dividir("\"a\"x,b");
    }
}
