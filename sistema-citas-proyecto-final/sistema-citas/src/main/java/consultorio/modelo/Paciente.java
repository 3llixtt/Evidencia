package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import consultorio.persistencia.UtilCSV;

import java.util.List;

/**
 * Paciente del consultorio: una persona con identificador y nombre completo.
 */
public class Paciente extends Persona {

    public static final String ENCABEZADO = "id,nombreCompleto";

    public Paciente(String id, String nombreCompleto) throws SistemaCitasException {
        super(id, nombreCompleto);
    }

    @Override
    public String aCSV() {
        return UtilCSV.unir(id, nombreCompleto);
    }

    /**
     * Construye un paciente a partir de una línea del archivo pacientes.csv.
     */
    public static Paciente desdeCSV(String linea) throws SistemaCitasException {
        List<String> campos = UtilCSV.dividir(linea);
        if (campos.size() != 2) {
            throw new SistemaCitasException("Se esperaban 2 columnas y hay " + campos.size() + ".");
        }
        return new Paciente(campos.get(0), campos.get(1));
    }

    @Override
    public String toString() {
        return id + " - " + nombreCompleto;
    }
}
