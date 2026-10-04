package consultorio.ui;

import consultorio.excepciones.SistemaCitasException;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Entrada y salida por consola. Concentra aquí la lectura del teclado para que el
 * resto del programa no dependa de {@code System.in} y pueda probarse con datos simulados.
 */
public class Consola {

    private final Scanner entrada;
    private final PrintStream salida;
    private final boolean terminalReal;

    public Consola(InputStream entrada, PrintStream salida) {
        this(entrada, salida, Charset.defaultCharset());
    }

    public Consola(InputStream entrada, PrintStream salida, Charset codificacion) {
        this.entrada = new Scanner(entrada, codificacion.name());
        this.salida = salida;
        // Solo se puede ocultar lo que se escribe cuando se usa una terminal real.
        this.terminalReal = entrada == System.in && System.console() != null;
    }

    /**
     * Muestra el mensaje y lee una línea.
     *
     * @throws EntradaTerminadaException si ya no hay más datos de entrada
     */
    public String leerTexto(String mensaje) {
        salida.print(mensaje);
        salida.flush();
        if (!entrada.hasNextLine()) {
            salida.println();
            throw new EntradaTerminadaException();
        }
        return entrada.nextLine().trim();
    }

    /**
     * Muestra el mensaje y lee una contraseña. En una terminal real lo escrito no se ve en pantalla;
     * en otros entornos (por ejemplo, la ventana Run de un IDE) se lee como texto normal.
     *
     * @throws EntradaTerminadaException si ya no hay más datos de entrada
     */
    public String leerContrasena(String mensaje) {
        if (terminalReal) {
            char[] caracteres = System.console().readPassword("%s", mensaje);
            if (caracteres == null) {
                throw new EntradaTerminadaException();
            }
            return new String(caracteres);
        }
        return leerTexto(mensaje);
    }

    /**
     * Muestra el mensaje y lee un número entero.
     *
     * @throws SistemaCitasException si lo escrito no es un número entero
     */
    public int leerEntero(String mensaje) throws SistemaCitasException {
        String texto = leerTexto(mensaje);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new SistemaCitasException("Escribe un número entero (recibido: \"" + texto + "\").");
        }
    }

    public void mostrar(String texto) {
        salida.println(texto);
    }

    public void mostrarError(String texto) {
        salida.println("[ERROR] " + texto);
    }
}
