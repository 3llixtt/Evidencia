package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;
import consultorio.persistencia.UtilCSV;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.Function;

/**
 * Cita médica. Cada cita tiene su propio identificador, una fecha y hora, un motivo
 * y está relacionada con exactamente un doctor y un paciente.
 */
public class Cita implements Almacenable {

    public static final String ENCABEZADO = "id,fechaHora,motivo,idDoctor,idPaciente";
    public static final int LONGITUD_MAXIMA_MOTIVO = 120;

    /** Formato con el que se escribe la fecha en el archivo. */
    private static final DateTimeFormatter FORMATO_ARCHIVO =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    /** Formato con el que el usuario escribe y lee la fecha. */
    private static final DateTimeFormatter FORMATO_PANTALLA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final String id;
    private final LocalDateTime fechaHora;
    private final String motivo;
    private final Doctor doctor;
    private final Paciente paciente;

    public Cita(String id, LocalDateTime fechaHora, String motivo, Doctor doctor, Paciente paciente)
            throws SistemaCitasException {
        this.id = Validaciones.identificador(id);
        if (fechaHora == null) {
            throw new SistemaCitasException("La fecha y hora de la cita es obligatoria.");
        }
        this.fechaHora = fechaHora;
        this.motivo = Validaciones.textoObligatorio(motivo, "motivo", LONGITUD_MAXIMA_MOTIVO);
        if (doctor == null) {
            throw new SistemaCitasException("La cita debe estar relacionada con un doctor.");
        }
        if (paciente == null) {
            throw new SistemaCitasException("La cita debe estar relacionada con un paciente.");
        }
        this.doctor = doctor;
        this.paciente = paciente;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    /**
     * En el archivo la cita guarda solo los identificadores del doctor y del paciente.
     */
    @Override
    public String aCSV() {
        return UtilCSV.unir(id, fechaHora.format(FORMATO_ARCHIVO), motivo, doctor.getId(), paciente.getId());
    }

    /**
     * Construye una cita a partir de una línea del archivo citas.csv.
     *
     * @param buscarDoctor   devuelve el doctor con ese identificador, o {@code null} si no existe
     * @param buscarPaciente devuelve el paciente con ese identificador, o {@code null} si no existe
     */
    public static Cita desdeCSV(String linea, Function<String, Doctor> buscarDoctor,
                                Function<String, Paciente> buscarPaciente) throws SistemaCitasException {
        List<String> campos = UtilCSV.dividir(linea);
        if (campos.size() != 5) {
            throw new SistemaCitasException("Se esperaban 5 columnas y hay " + campos.size() + ".");
        }
        LocalDateTime fechaHora;
        try {
            fechaHora = LocalDateTime.parse(campos.get(1).trim(), FORMATO_ARCHIVO);
        } catch (DateTimeParseException e) {
            throw new SistemaCitasException("La fecha \"" + campos.get(1) + "\" no es válida.");
        }
        Doctor doctor = buscarDoctor.apply(campos.get(3));
        if (doctor == null) {
            throw new SistemaCitasException("La cita " + campos.get(0) + " hace referencia a un doctor que no existe ("
                    + campos.get(3) + ").");
        }
        Paciente paciente = buscarPaciente.apply(campos.get(4));
        if (paciente == null) {
            throw new SistemaCitasException("La cita " + campos.get(0) + " hace referencia a un paciente que no existe ("
                    + campos.get(4) + ").");
        }
        return new Cita(campos.get(0), fechaHora, campos.get(2), doctor, paciente);
    }

    /**
     * Convierte el texto escrito por el usuario (dd/MM/aaaa HH:mm) en una fecha y hora.
     */
    public static LocalDateTime parsearFechaHora(String texto) throws SistemaCitasException {
        try {
            return LocalDateTime.parse(texto == null ? "" : texto.trim(), FORMATO_PANTALLA);
        } catch (DateTimeParseException e) {
            throw new SistemaCitasException(
                    "Fecha y hora no válidas. Usa el formato dd/MM/aaaa HH:mm, por ejemplo 15/10/2026 09:30.");
        }
    }

    /**
     * @return la fecha y hora como se muestra al usuario (dd/MM/aaaa HH:mm)
     */
    public String fechaHoraParaMostrar() {
        return fechaHora.format(FORMATO_PANTALLA);
    }

    @Override
    public String toString() {
        return id + " - " + fechaHoraParaMostrar() + " - " + doctor.getNombreCompleto()
                + " / " + paciente.getNombreCompleto() + " (" + motivo + ")";
    }
}
