package consultorio.modelo;

import consultorio.excepciones.SistemaCitasException;

/**
 * Clase base de las personas del sistema (doctores, pacientes y administradores).
 * Es abstracta porque siempre se trabaja con alguno de sus tipos concretos.
 */
public abstract class Persona implements Almacenable {

    public static final int LONGITUD_MAXIMA_NOMBRE = 80;

    protected final String id;
    protected final String nombreCompleto;

    /**
     * @param id             identificador único
     * @param nombreCompleto nombre completo
     * @throws SistemaCitasException si algún dato no es válido
     */
    protected Persona(String id, String nombreCompleto) throws SistemaCitasException {
        this.id = Validaciones.identificador(id);
        this.nombreCompleto = Validaciones.textoObligatorio(nombreCompleto, "nombre completo", LONGITUD_MAXIMA_NOMBRE);
    }

    public String getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Cada tipo de persona decide qué columnas guarda.
     */
    @Override
    public abstract String aCSV();
}
