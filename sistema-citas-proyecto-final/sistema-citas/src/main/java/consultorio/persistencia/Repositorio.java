package consultorio.persistencia;

import consultorio.excepciones.SistemaCitasException;

import java.util.List;

/**
 * Almacén de objetos. Permite cambiar el formato de archivo (CSV, JSON, XML)
 * sin modificar el resto del programa.
 *
 * @param <T> tipo de objeto que se guarda
 */
public interface Repositorio<T> {

    /**
     * Verifica que el almacén exista y, si falta, lo vuelve a crear vacío.
     */
    void inicializar() throws SistemaCitasException;

    /**
     * Reemplaza el contenido del almacén con la lista recibida.
     */
    void guardarTodos(List<T> lista) throws SistemaCitasException;

    /**
     * @return todos los objetos guardados (lista vacía si no hay ninguno)
     */
    List<T> cargarTodos() throws SistemaCitasException;
}
