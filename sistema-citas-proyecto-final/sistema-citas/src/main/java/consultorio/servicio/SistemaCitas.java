package consultorio.servicio;

import consultorio.excepciones.SistemaCitasException;
import consultorio.modelo.Administrador;
import consultorio.modelo.Cita;
import consultorio.modelo.Doctor;
import consultorio.modelo.Paciente;
import consultorio.persistencia.RepositorioCSV;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Lógica del sistema de citas: mantiene en memoria los datos, aplica las reglas
 * de negocio y los guarda en archivos CSV dentro de la carpeta de datos (db).
 */
public final class SistemaCitas {

    public static final String ARCHIVO_DOCTORES = "doctores.csv";
    public static final String ARCHIVO_PACIENTES = "pacientes.csv";
    public static final String ARCHIVO_CITAS = "citas.csv";
    public static final String ARCHIVO_ADMINISTRADORES = "administradores.csv";

    private final RepositorioCSV<Doctor> repositorioDoctores;
    private final RepositorioCSV<Paciente> repositorioPacientes;
    private final RepositorioCSV<Cita> repositorioCitas;
    private final RepositorioCSV<Administrador> repositorioAdministradores;

    private final List<Administrador> administradores = new ArrayList<>();
    private final List<Doctor> doctores = new ArrayList<>();
    private final List<Paciente> pacientes = new ArrayList<>();
    private final List<Cita> citas = new ArrayList<>();
    private final List<String> advertencias = new ArrayList<>();

    /**
     * @param carpetaDb carpeta donde se guardan los archivos de datos
     */
    public SistemaCitas(Path carpetaDb) {
        this.repositorioDoctores = new RepositorioCSV<>(
                carpetaDb.resolve(ARCHIVO_DOCTORES), Doctor.ENCABEZADO, Doctor::desdeCSV);
        this.repositorioPacientes = new RepositorioCSV<>(
                carpetaDb.resolve(ARCHIVO_PACIENTES), Paciente.ENCABEZADO, Paciente::desdeCSV);
        this.repositorioCitas = new RepositorioCSV<>(
                carpetaDb.resolve(ARCHIVO_CITAS), Cita.ENCABEZADO,
                linea -> Cita.desdeCSV(linea, this::buscarDoctor, this::buscarPaciente));
        this.repositorioAdministradores = new RepositorioCSV<>(
                carpetaDb.resolve(ARCHIVO_ADMINISTRADORES), Administrador.ENCABEZADO, Administrador::desdeCSV);
    }

    // ------------------------------------------------------------------ datos

    /**
     * Carga todos los datos desde la carpeta db. Los archivos que falten se regeneran vacíos.
     */
    public void cargarDatos() throws SistemaCitasException {
        advertencias.clear();

        administradores.clear();
        for (Administrador administrador : repositorioAdministradores.cargarTodos()) {
            if (buscarAdministrador(administrador.getId()) != null) {
                advertencias.add(ARCHIVO_ADMINISTRADORES + ": se omitió el identificador repetido "
                        + administrador.getId() + ".");
            } else {
                administradores.add(administrador);
            }
        }
        advertencias.addAll(repositorioAdministradores.getAdvertencias());

        doctores.clear();
        for (Doctor doctor : repositorioDoctores.cargarTodos()) {
            if (buscarDoctor(doctor.getId()) != null) {
                advertencias.add(ARCHIVO_DOCTORES + ": se omitió el identificador repetido " + doctor.getId() + ".");
            } else {
                doctores.add(doctor);
            }
        }
        advertencias.addAll(repositorioDoctores.getAdvertencias());

        pacientes.clear();
        for (Paciente paciente : repositorioPacientes.cargarTodos()) {
            if (buscarPaciente(paciente.getId()) != null) {
                advertencias.add(ARCHIVO_PACIENTES + ": se omitió el identificador repetido " + paciente.getId() + ".");
            } else {
                pacientes.add(paciente);
            }
        }
        advertencias.addAll(repositorioPacientes.getAdvertencias());

        // Las citas se cargan al final porque necesitan encontrar a su doctor y a su paciente.
        citas.clear();
        for (Cita cita : repositorioCitas.cargarTodos()) {
            if (buscarCita(cita.getId()) != null) {
                advertencias.add(ARCHIVO_CITAS + ": se omitió el identificador repetido " + cita.getId() + ".");
            } else {
                citas.add(cita);
            }
        }
        advertencias.addAll(repositorioCitas.getAdvertencias());
    }

    /**
     * Guarda todos los datos en sus archivos.
     */
    public void guardarDatos() throws SistemaCitasException {
        repositorioAdministradores.guardarTodos(administradores);
        repositorioDoctores.guardarTodos(doctores);
        repositorioPacientes.guardarTodos(pacientes);
        repositorioCitas.guardarTodos(citas);
    }

    /**
     * @return avisos de la última carga (líneas dañadas o repetidas que se omitieron)
     */
    public List<String> getAdvertencias() {
        return Collections.unmodifiableList(new ArrayList<>(advertencias));
    }

    // ----------------------------------------------------------- administradores

    /**
     * Da de alta un administrador (usuario con acceso al sistema) y lo guarda en el archivo.
     *
     * @throws SistemaCitasException si los datos no son válidos, el identificador ya existe
     *                               o no se puede guardar el archivo
     */
    public Administrador altaAdministrador(String id, String nombreCompleto, String contrasena)
            throws SistemaCitasException {
        Administrador nuevo = Administrador.crear(id, nombreCompleto, contrasena);
        if (buscarAdministrador(nuevo.getId()) != null) {
            throw new SistemaCitasException("Ya existe un administrador con el identificador " + nuevo.getId() + ".");
        }
        administradores.add(nuevo);
        try {
            repositorioAdministradores.guardarTodos(administradores);
        } catch (SistemaCitasException e) {
            administradores.remove(nuevo);
            throw e;
        }
        return nuevo;
    }

    /**
     * Verifica las credenciales de un administrador.
     *
     * @return el administrador si el identificador y la contraseña son correctos, o {@code null} si no
     */
    public Administrador iniciarSesion(String id, String contrasena) {
        for (Administrador administrador : administradores) {
            if (administrador.autenticar(id, contrasena)) {
                return administrador;
            }
        }
        return null;
    }

    public boolean hayAdministradores() {
        return !administradores.isEmpty();
    }

    /**
     * @return el administrador con ese identificador (sin distinguir mayúsculas) o {@code null} si no existe
     */
    public Administrador buscarAdministrador(String id) {
        if (id == null) {
            return null;
        }
        for (Administrador administrador : administradores) {
            if (administrador.getId().equalsIgnoreCase(id.trim())) {
                return administrador;
            }
        }
        return null;
    }

    public List<Administrador> getAdministradores() {
        return Collections.unmodifiableList(administradores);
    }

    // ---------------------------------------------------------------- doctores

    /**
     * Da de alta un doctor y lo guarda en el archivo.
     *
     * @throws SistemaCitasException si los datos no son válidos, el identificador ya existe
     *                               o no se puede guardar el archivo
     */
    public Doctor altaDoctor(String id, String nombreCompleto, String especialidad) throws SistemaCitasException {
        Doctor nuevo = new Doctor(id, nombreCompleto, especialidad);
        if (buscarDoctor(nuevo.getId()) != null) {
            throw new SistemaCitasException("Ya existe un doctor con el identificador " + nuevo.getId() + ".");
        }
        doctores.add(nuevo);
        try {
            repositorioDoctores.guardarTodos(doctores);
        } catch (SistemaCitasException e) {
            doctores.remove(nuevo);
            throw e;
        }
        return nuevo;
    }

    /**
     * @return el doctor con ese identificador (sin distinguir mayúsculas) o {@code null} si no existe
     */
    public Doctor buscarDoctor(String id) {
        if (id == null) {
            return null;
        }
        for (Doctor doctor : doctores) {
            if (doctor.getId().equalsIgnoreCase(id.trim())) {
                return doctor;
            }
        }
        return null;
    }

    public List<Doctor> getDoctores() {
        return Collections.unmodifiableList(doctores);
    }

    // --------------------------------------------------------------- pacientes

    /**
     * Da de alta un paciente y lo guarda en el archivo.
     *
     * @throws SistemaCitasException si los datos no son válidos, el identificador ya existe
     *                               o no se puede guardar el archivo
     */
    public Paciente altaPaciente(String id, String nombreCompleto) throws SistemaCitasException {
        Paciente nuevo = new Paciente(id, nombreCompleto);
        if (buscarPaciente(nuevo.getId()) != null) {
            throw new SistemaCitasException("Ya existe un paciente con el identificador " + nuevo.getId() + ".");
        }
        pacientes.add(nuevo);
        try {
            repositorioPacientes.guardarTodos(pacientes);
        } catch (SistemaCitasException e) {
            pacientes.remove(nuevo);
            throw e;
        }
        return nuevo;
    }

    /**
     * @return el paciente con ese identificador (sin distinguir mayúsculas) o {@code null} si no existe
     */
    public Paciente buscarPaciente(String id) {
        if (id == null) {
            return null;
        }
        for (Paciente paciente : pacientes) {
            if (paciente.getId().equalsIgnoreCase(id.trim())) {
                return paciente;
            }
        }
        return null;
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    //------------------------------------------------------------------- citas

    /**
     * Crea una cita relacionada con un doctor y un paciente que ya existen.
     *
     * <p>Reglas: el identificador no puede repetirse, la fecha no puede ser anterior al
     * momento actual y ni el doctor ni el paciente pueden tener otra cita a la misma hora.</p>
     *
     * @throws SistemaCitasException si alguna regla no se cumple o no se puede guardar el archivo
     */
    public Cita crearCita(String id, LocalDateTime fechaHora, String motivo, String idDoctor, String idPaciente)
            throws SistemaCitasException {
        Doctor doctor = buscarDoctor(idDoctor);
        if (doctor == null) {
            throw new SistemaCitasException("No existe un doctor con el identificador " + idDoctor + ".");
        }
        Paciente paciente = buscarPaciente(idPaciente);
        if (paciente == null) {
            throw new SistemaCitasException("No existe un paciente con el identificador " + idPaciente + ".");
        }

        Cita nueva = new Cita(id, fechaHora, motivo, doctor, paciente);

        if (buscarCita(nueva.getId()) != null) {
            throw new SistemaCitasException("Ya existe una cita con el identificador " + nueva.getId() + ".");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new SistemaCitasException("La fecha y hora de la cita ya pasó.");
        }
        for (Cita existente : citas) {
            if (!existente.getFechaHora().equals(fechaHora)) {
                continue;
            }
            if (existente.getDoctor().getId().equals(doctor.getId())) {
                throw new SistemaCitasException("El doctor " + doctor.getNombreCompleto()
                        + " ya tiene la cita " + existente.getId() + " a esa hora.");
            }
            if (existente.getPaciente().getId().equals(paciente.getId())) {
                throw new SistemaCitasException("El paciente " + paciente.getNombreCompleto()
                        + " ya tiene la cita " + existente.getId() + " a esa hora.");
            }
        }

        citas.add(nueva);
        try {
            repositorioCitas.guardarTodos(citas);
        } catch (SistemaCitasException e) {
            citas.remove(nueva);
            throw e;
        }
        return nueva;
    }

    /**
     * @return las citas ordenadas por fecha y hora (y por identificador si coinciden)
     */
    public List<Cita> listarCitas() {
        List<Cita> ordenadas = new ArrayList<>(citas);
        ordenadas.sort(Comparator.comparing(Cita::getFechaHora).thenComparing(Cita::getId));
        return ordenadas;
    }

    /**
     * @return la cita con ese identificador (sin distinguir mayúsculas) o {@code null} si no existe
     */
    public Cita buscarCita(String id) {
        if (id == null) {
            return null;
        }
        for (Cita cita : citas) {
            if (cita.getId().equalsIgnoreCase(id.trim())) {
                return cita;
            }
        }
        return null;
    }
}
