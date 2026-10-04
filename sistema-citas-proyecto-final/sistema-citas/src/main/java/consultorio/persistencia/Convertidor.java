package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;

/**
 * Convierte una línea de un archivo CSV en un objeto.
 *
 * @param <T> tipo de objeto que se construye
 */
@FunctionalInterface
public interface Convertidor<T> {

    /**
     * @param linea línea del archivo, sin salto de línea
     * @return el objeto construido
     * @throws SistemaCitasException si la línea no tiene un formato válido
     */
    T convertir(String linea) throws SistemaCitasException;
}
