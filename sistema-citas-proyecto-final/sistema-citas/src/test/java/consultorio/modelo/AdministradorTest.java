package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AdministradorTest {

    @Test
    public void autenticaConLaContrasenaCorrecta() throws SistemaCitasException {
        Administrador admin = Administrador.crear("admin", "Administrador General", "clave123");
        assertTrue(admin.autenticar("admin", "clave123"));
    }

    @Test
    public void rechazaUnaContrasenaIncorrecta() throws SistemaCitasException {
        Administrador admin = Administrador.crear("admin", "Administrador General", "clave123");
        assertFalse(admin.autenticar("admin", "clave124"));
        assertFalse(admin.autenticar("admin", ""));
    }

    @Test
    public void rechazaUnIdentificadorIncorrecto() throws SistemaCitasException {
        Administrador admin = Administrador.crear("admin", "Administrador General", "clave123");
        assertFalse(admin.autenticar("otro", "clave123"));
    }

    @Test
    public void elIdentificadorNoDistingueMayusculas() throws SistemaCitasException {
        Administrador admin = Administrador.crear("Admin", "Administrador General", "clave123");
        assertTrue(admin.autenticar(" ADMIN ", "clave123"));
    }

    @Test
    public void laContrasenaSiDistingueMayusculas() throws SistemaCitasException {
        Administrador admin = Administrador.crear("admin", "Administrador General", "Clave123");
        assertFalse(admin.autenticar("admin", "clave123"));
    }

    @Test
    public void autenticarConNulosDevuelveFalso() throws SistemaCitasException {
        Administrador admin = Administrador.crear("admin", "Administrador General", "clave123");
        assertFalse(admin.autenticar(null, "clave123"));
        assertFalse(admin.autenticar("admin", null));
    }

    @Test
    public void laContrasenaNoApareceEnElCSV() throws SistemaCitasException {
        String csv = Administrador.crear("admin", "Administrador General", "clave123").aCSV();
        assertFalse(csv.contains("clave123"));
    }

    @Test
    public void despuesDeGuardarYCargarSigueAutenticando() throws SistemaCitasException {
        Administrador original = Administrador.crear("admin", "Administrador General", "clave123");
        Administrador copia = Administrador.desdeCSV(original.aCSV());
        assertEquals("Administrador General", copia.getNombreCompleto());
        assertTrue(copia.autenticar("admin", "clave123"));
        assertFalse(copia.autenticar("admin", "otra-clave"));
    }

    @Test
    public void dosAdministradoresConLaMismaContrasenaTienenDistintoHash() throws SistemaCitasException {
        String a = Administrador.crear("a1", "Uno", "clave123").aCSV();
        String b = Administrador.crear("a2", "Dos", "clave123").aCSV();
        assertNotEquals(a.substring(a.lastIndexOf(',')), b.substring(b.lastIndexOf(',')));
    }

    @Test(expected = SistemaCitasException.class)
    public void exigeUnaContrasenaDeAlMenosSeisCaracteres() throws SistemaCitasException {
        Administrador.crear("admin", "Administrador General", "12345");
    }

    @Test(expected = SistemaCitasException.class)
    public void rechazaUnaContrasenaNula() throws SistemaCitasException {
        Administrador.crear("admin", "Administrador General", null);
    }

    @Test(expected = SistemaCitasException.class)
    public void lineaSinHashEsInvalida() throws SistemaCitasException {
        Administrador.desdeCSV("admin,Administrador General,,");
    }

    @Test(expected = SistemaCitasException.class)
    public void lineaConColumnasDeMenosEsInvalida() throws SistemaCitasException {
        Administrador.desdeCSV("admin,Administrador General");
    }
}
