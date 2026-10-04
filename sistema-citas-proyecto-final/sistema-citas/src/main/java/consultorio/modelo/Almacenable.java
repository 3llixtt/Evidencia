package consultorio.modelo;

/**
 * Contrato de todo objeto que se guarda en un archivo: sabe convertirse
 * en una línea de texto con formato CSV.
 */
public interface Almacenable {

    /**
     * @return la representación del objeto como una línea CSV (sin salto de línea)
     */
    String aCSV();
}
