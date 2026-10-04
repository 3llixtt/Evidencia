package consultorio;

import consultorio.excepciones.SistemaCitasException;
import consultorio.modelo.Administrador;
import consultorio.modelo.Cita;
import consultorio.modelo.Doctor;
import consultorio.modelo.Paciente;
import consultorio.servicio.SistemaCitas;
import consultorio.ui.Consola;
import consultorio.ui.EntradaTerminadaException;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Punto de entrada del sistema de administración de citas.
 *
 * <p>Uso: {@code java -jar sistema-citas-1.0.jar [carpeta-de-datos]}. Si no se indica
 * una carpeta, los datos se guardan en {@code db}, junto a donde se ejecuta el programa.</p>
 */
public class Main {

    public static final String CARPETA_DB_PREDETERMINADA = "db";

    public static void main(String[] args) {
        Path carpetaDb = Paths.get(args.length > 0 ? args[0] : CARPETA_DB_PREDETERMINADA);
        ejecutar(System.in, System.out, carpetaDb);
    }

    /**
     * Ejecuta el programa completo. Está separado de {@code main} para poder probarlo
     * con una entrada y una salida simuladas.
     */
    public static void ejecutar(InputStream entrada, PrintStream salida, Path carpetaDb) {
        Consola consola = new Consola(entrada, salida);
        SistemaCitas sistema = new SistemaCitas(carpetaDb);

        try {
            sistema.cargarDatos();
        } catch (SistemaCitasException e) {
            consola.mostrarError("No se pudieron cargar los datos: " + e.getMessage());
            consola.mostrar("El programa no puede continuar sin acceso a la carpeta de datos ("
                    + carpetaDb + "). Revisa que exista y que tengas permisos de lectura y escritura.");
            return;
        }
        for (String aviso : sistema.getAdvertencias()) {
            consola.mostrar("[AVISO] " + aviso);
        }

        try {
            if (!sistema.hayAdministradores()) {
                crearPrimerAdministrador(consola, sistema);
            }
            Administrador sesion = iniciarSesion(consola, sistema);
            consola.mostrar("Sesión iniciada como " + sesion.getNombreCompleto() + ".");
            ciclo(consola, sistema);
        } catch (EntradaTerminadaException e) {
            consola.mostrar("Entrada terminada. Saliendo del programa.");
        }
    }

    // ------------------------------------------------------- control de acceso

    /**
     * La primera vez que se usa el programa no existe ningún administrador, así que se pide
     * crear uno. De esta forma no hay una contraseña predeterminada que alguien pueda adivinar.
     */
    private static void crearPrimerAdministrador(Consola consola, SistemaCitas sistema) {
        consola.mostrar("No hay administradores registrados. Crea el primer administrador para continuar.");
        while (true) {
            try {
                altaAdministrador(consola, sistema);
                return;
            } catch (SistemaCitasException e) {
                consola.mostrarError(e.getMessage());
            }
        }
    }

    /**
     * Pide identificador y contraseña hasta que sean correctos.
     */
    private static Administrador iniciarSesion(Consola consola, SistemaCitas sistema) {
        consola.mostrar("");
        consola.mostrar("=== INICIO DE SESIÓN ===");
        while (true) {
            String id = consola.leerTexto("Identificador: ");
            String contrasena = consola.leerContrasena("Contraseña: ");
            Administrador administrador = sistema.iniciarSesion(id, contrasena);
            if (administrador != null) {
                return administrador;
            }
            consola.mostrarError("Identificador o contraseña incorrectos.");
        }
    }

    // ------------------------------------------------------------------- menú

    private static void ciclo(Consola consola, SistemaCitas sistema) {
        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu(consola);
            try {
                opcion = consola.leerEntero("Opción: ");
                ejecutarOpcion(opcion, consola, sistema);
            } catch (SistemaCitasException e) {
                // Un error esperado no cierra el programa: se muestra y se regresa al menú.
                consola.mostrarError(e.getMessage());
            } catch (EntradaTerminadaException e) {
                throw e;
            } catch (RuntimeException e) {
                consola.mostrarError("Ocurrió un error inesperado: " + e);
            }
        }
    }

    private static void mostrarMenu(Consola consola) {
        consola.mostrar("");
        consola.mostrar("=== SISTEMA DE ADMINISTRACIÓN DE CITAS ===");
        consola.mostrar("1. Alta de doctor");
        consola.mostrar("2. Alta de paciente");
        consola.mostrar("3. Crear cita");
        consola.mostrar("4. Listar citas");
        consola.mostrar("5. Alta de administrador");
        consola.mostrar("0. Salir");
    }

    private static void ejecutarOpcion(int opcion, Consola consola, SistemaCitas sistema)
            throws SistemaCitasException {
        switch (opcion) {
            case 1:
                altaDoctor(consola, sistema);
                break;
            case 2:
                altaPaciente(consola, sistema);
                break;
            case 3:
                crearCita(consola, sistema);
                break;
            case 4:
                listarCitas(consola, sistema);
                break;
            case 5:
                altaAdministrador(consola, sistema);
                break;
            case 0:
                sistema.guardarDatos();
                consola.mostrar("Datos guardados. Hasta pronto.");
                break;
            default:
                throw new SistemaCitasException("Opción no válida: " + opcion + ".");
        }
    }

    private static void altaAdministrador(Consola consola, SistemaCitas sistema) throws SistemaCitasException {
        consola.mostrar("--- Alta de administrador ---");
        String id = consola.leerTexto("Identificador: ");
        String nombre = consola.leerTexto("Nombre completo: ");
        String contrasena = consola.leerContrasena("Contraseña (mínimo " + Administrador.LONGITUD_MINIMA_CONTRASENA
                + " caracteres): ");
        String confirmacion = consola.leerContrasena("Repite la contraseña: ");
        if (!contrasena.equals(confirmacion)) {
            throw new SistemaCitasException("Las contraseñas no coinciden.");
        }
        sistema.altaAdministrador(id, nombre, contrasena);
        consola.mostrar("Administrador registrado correctamente.");
    }

    private static void altaDoctor(Consola consola, SistemaCitas sistema) throws SistemaCitasException {
        consola.mostrar("--- Alta de doctor ---");
        String id = consola.leerTexto("Identificador: ");
        String nombre = consola.leerTexto("Nombre completo: ");
        String especialidad = consola.leerTexto("Especialidad: ");
        sistema.altaDoctor(id, nombre, especialidad);
        consola.mostrar("Doctor registrado correctamente.");
    }

    private static void altaPaciente(Consola consola, SistemaCitas sistema) throws SistemaCitasException {
        consola.mostrar("--- Alta de paciente ---");
        String id = consola.leerTexto("Identificador: ");
        String nombre = consola.leerTexto("Nombre completo: ");
        sistema.altaPaciente(id, nombre);
        consola.mostrar("Paciente registrado correctamente.");
    }

    private static void crearCita(Consola consola, SistemaCitas sistema) throws SistemaCitasException {
        consola.mostrar("--- Crear cita ---");
        if (sistema.getDoctores().isEmpty() || sistema.getPacientes().isEmpty()) {
            throw new SistemaCitasException(
                    "Para crear una cita primero debes registrar al menos un doctor y un paciente.");
        }
        consola.mostrar("Doctores registrados:");
        for (Doctor doctor : sistema.getDoctores()) {
            consola.mostrar("  " + doctor);
        }
        consola.mostrar("Pacientes registrados:");
        for (Paciente paciente : sistema.getPacientes()) {
            consola.mostrar("  " + paciente);
        }

        String id = consola.leerTexto("Identificador de la cita: ");
        LocalDateTime fechaHora = Cita.parsearFechaHora(consola.leerTexto("Fecha y hora (dd/MM/aaaa HH:mm): "));
        String motivo = consola.leerTexto("Motivo de la cita: ");
        String idDoctor = consola.leerTexto("Identificador del doctor: ");
        String idPaciente = consola.leerTexto("Identificador del paciente: ");

        sistema.crearCita(id, fechaHora, motivo, idDoctor, idPaciente);
        consola.mostrar("Cita registrada correctamente.");
    }

    private static void listarCitas(Consola consola, SistemaCitas sistema) {
        consola.mostrar("--- Citas registradas ---");
        List<Cita> citas = sistema.listarCitas();
        if (citas.isEmpty()) {
            consola.mostrar("No hay citas registradas.");
            return;
        }
        for (Cita cita : citas) {
            consola.mostrar("Cita " + cita.getId() + " | " + cita.fechaHoraParaMostrar());
            consola.mostrar("  Doctor:   " + cita.getDoctor());
            consola.mostrar("  Paciente: " + cita.getPaciente());
            consola.mostrar("  Motivo:   " + cita.getMotivo());
        }
        consola.mostrar("Total de citas: " + citas.size());
    }
}
