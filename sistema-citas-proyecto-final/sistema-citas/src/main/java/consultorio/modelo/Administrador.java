package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import consultorio.persistencia.UtilCSV;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Usuario con permiso para entrar al sistema. Además de los datos de {@link Persona}
 * tiene una contraseña, que nunca se guarda tal cual: en el archivo solo aparecen una
 * "sal" aleatoria y el resultado de aplicar PBKDF2 a la contraseña con esa sal.
 */
public class Administrador extends Persona {

    public static final String ENCABEZADO = "id,nombreCompleto,sal,hashContrasena";
    public static final int LONGITUD_MINIMA_CONTRASENA = 6;
    public static final int LONGITUD_MAXIMA_CONTRASENA = 64;

    private static final int ITERACIONES = 65_536;
    private static final int BITS_DE_CLAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final String sal;
    private final String hashContrasena;

    private Administrador(String id, String nombreCompleto, String sal, String hashContrasena)
            throws SistemaCitasException {
        super(id, nombreCompleto);
        if (sal == null || sal.isEmpty() || hashContrasena == null || hashContrasena.isEmpty()) {
            throw new SistemaCitasException("El administrador " + id + " no tiene contraseña guardada.");
        }
        this.sal = sal;
        this.hashContrasena = hashContrasena;
    }

    /**
     * Crea un administrador nuevo a partir de su contraseña en texto.
     *
     * @throws SistemaCitasException si algún dato no es válido
     */
    public static Administrador crear(String id, String nombreCompleto, String contrasena)
            throws SistemaCitasException {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new SistemaCitasException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }
        if (contrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            throw new SistemaCitasException(
                    "La contraseña no puede tener más de " + LONGITUD_MAXIMA_CONTRASENA + " caracteres.");
        }
        byte[] bytesSal = new byte[16];
        ALEATORIO.nextBytes(bytesSal);
        String sal = Base64.getEncoder().encodeToString(bytesSal);
        return new Administrador(id, nombreCompleto, sal, calcularHash(contrasena, sal));
    }

    /**
     * Verifica un identificador y una contraseña.
     *
     * @return {@code true} solo si ambos corresponden a este administrador
     */
    public boolean autenticar(String idIngresado, String contrasenaIngresada) {
        if (idIngresado == null || contrasenaIngresada == null || !id.equalsIgnoreCase(idIngresado.trim())) {
            return false;
        }
        try {
            byte[] esperado = hashContrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] calculado = calcularHash(contrasenaIngresada, sal).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (SistemaCitasException e) {
            return false;
        }
    }

    @Override
    public String aCSV() {
        return UtilCSV.unir(id, nombreCompleto, sal, hashContrasena);
    }

    /**
     * Construye un administrador a partir de una línea del archivo administradores.csv.
     */
    public static Administrador desdeCSV(String linea) throws SistemaCitasException {
        List<String> campos = UtilCSV.dividir(linea);
        if (campos.size() != 4) {
            throw new SistemaCitasException("Se esperaban 4 columnas y hay " + campos.size() + ".");
        }
        return new Administrador(campos.get(0), campos.get(1), campos.get(2), campos.get(3));
    }

    private static String calcularHash(String contrasena, String sal) throws SistemaCitasException {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(
                    contrasena.toCharArray(), Base64.getDecoder().decode(sal), ITERACIONES, BITS_DE_CLAVE);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new SistemaCitasException("No se pudo procesar la contraseña.", e);
        }
    }

    @Override
    public String toString() {
        return id + " - " + nombreCompleto;
    }
}
