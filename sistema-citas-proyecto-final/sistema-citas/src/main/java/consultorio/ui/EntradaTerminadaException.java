package consultorio.ui;

/**
 * Se lanza cuando ya no hay más datos que leer en la entrada (por ejemplo, Ctrl+D o Ctrl+Z).
 * Sirve para cerrar el programa de forma ordenada en lugar de quedarse en un ciclo.
 */
public class EntradaTerminadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaTerminadaException() {
        super("Se terminó la entrada de datos.");
    }
}
