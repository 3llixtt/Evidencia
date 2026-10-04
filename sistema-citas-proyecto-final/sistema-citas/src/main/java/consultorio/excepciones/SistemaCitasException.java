package consultorio.excepciones;

/**
 * Excepción propia del sistema. Representa los errores esperables de la aplicación:
 * datos inválidos, identificadores repetidos, registros inexistentes o fallos al
 * leer y escribir los archivos. El mensaje siempre está pensado para mostrarse al usuario.
 */
public class SistemaCitasException extends Exception {

    private static final long serialVersionUID = 1L;

    public SistemaCitasException(String mensaje) {
        super(mensaje);
    }

    public SistemaCitasException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
