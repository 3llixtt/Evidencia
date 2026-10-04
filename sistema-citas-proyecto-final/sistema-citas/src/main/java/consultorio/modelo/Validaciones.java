package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;

import java.util.regex.Pattern;

/**
 * Reglas de validación compartidas por las clases del modelo.
 */
public final class Validaciones {

    private static final Pattern IDENTIFICADOR = Pattern.compile("[A-Za-z0-9_-]{1,20}");

    private Validaciones() {
    }

    /**
     * Valida un identificador: solo letras, números, guion y guion bajo (máximo 20 caracteres).
     *
     * @return el identificador sin espacios al inicio ni al final
     */
    public static String identificador(String valor) throws SistemaCitasException {
        String limpio = valor == null ? "" : valor.trim();
        if (!IDENTIFICADOR.matcher(limpio).matches()) {
            throw new SistemaCitasException(
                    "El identificador no es válido: usa solo letras, números, guion y guion bajo (máximo 20 caracteres).");
        }
        return limpio;
    }

    /**
     * Valida un texto obligatorio.
     *
     * @param campo  nombre del campo, para el mensaje de error
     * @param maximo longitud máxima permitida
     * @return el texto sin espacios al inicio ni al final
     */
    public static String textoObligatorio(String valor, String campo, int maximo) throws SistemaCitasException {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new SistemaCitasException("El campo \"" + campo + "\" es obligatorio.");
        }
        if (limpio.length() > maximo) {
            throw new SistemaCitasException("El campo \"" + campo + "\" no puede tener más de " + maximo + " caracteres.");
        }
        return limpio;
    }
}
