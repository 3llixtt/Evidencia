package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import consultorio.persistencia.UtilCSV;

import java.util.List;

/**
 * Doctor del consultorio: una persona con una especialidad.
 */
public class Doctor extends Persona {

    public static final String ENCABEZADO = "id,nombreCompleto,especialidad";
    public static final int LONGITUD_MAXIMA_ESPECIALIDAD = 60;

    private final String especialidad;

    public Doctor(String id, String nombreCompleto, String especialidad) throws SistemaCitasException {
        super(id, nombreCompleto);
        this.especialidad = Validaciones.textoObligatorio(especialidad, "especialidad", LONGITUD_MAXIMA_ESPECIALIDAD);
    }

    public String getEspecialidad() {
        return especialidad;
    }

    @Override
    public String aCSV() {
        return UtilCSV.unir(id, nombreCompleto, especialidad);
    }

    /**
     * Construye un doctor a partir de una línea del archivo doctores.csv.
     */
    public static Doctor desdeCSV(String linea) throws SistemaCitasException {
        List<String> campos = UtilCSV.dividir(linea);
        if (campos.size() != 3) {
            throw new SistemaCitasException("Se esperaban 3 columnas y hay " + campos.size() + ".");
        }
        return new Doctor(campos.get(0), campos.get(1), campos.get(2));
    }

    @Override
    public String toString() {
        return id + " - " + nombreCompleto + " (" + especialidad + ")";
    }
}
