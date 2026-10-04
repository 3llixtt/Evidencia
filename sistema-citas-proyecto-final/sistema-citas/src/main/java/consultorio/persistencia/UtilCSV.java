package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;

import java.util.ArrayList;
import java.util.List;

/**
 * Funciones para escribir y leer líneas CSV. Si un campo contiene comas o comillas,
 * se encierra entre comillas dobles y las comillas internas se duplican, por lo que
 * un nombre como {@code Pérez, Juan} no rompe el formato del archivo.
 */
public final class UtilCSV {

    private UtilCSV() {
    }

    /**
     * Prepara un campo para escribirlo en una línea CSV.
     */
    public static String escapar(String campo) {
        if (campo == null) {
            return "";
        }
        String limpio = campo.replace("\r", " ").replace("\n", " ");
        if (limpio.contains(",") || limpio.contains("\"")) {
            return "\"" + limpio.replace("\"", "\"\"") + "\"";
        }
        return limpio;
    }

    /**
     * Une varios campos en una sola línea CSV, escapando cada uno.
     */
    public static String unir(String... campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(',');
            }
            linea.append(escapar(campos[i]));
        }
        return linea.toString();
    }

    /**
     * Divide una línea CSV en sus campos, respetando las comillas.
     *
     * @throws SistemaCitasException si hay comillas sin cerrar o texto después de cerrarlas
     */
    public static List<String> dividir(String linea) throws SistemaCitasException {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        boolean comillasCerradas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                        comillasCerradas = true;
                    }
                } else {
                    actual.append(c);
                }
            } else if (c == ',') {
                campos.add(actual.toString());
                actual.setLength(0);
                comillasCerradas = false;
            } else if (c == '"' && actual.length() == 0 && !comillasCerradas) {
                entreComillas = true;
            } else if (comillasCerradas) {
                throw new SistemaCitasException("Formato CSV inválido: hay texto después de cerrar las comillas.");
            } else {
                actual.append(c);
            }
        }
        if (entreComillas) {
            throw new SistemaCitasException("Formato CSV inválido: comillas sin cerrar.");
        }
        campos.add(actual.toString());
        return campos;
    }
}
