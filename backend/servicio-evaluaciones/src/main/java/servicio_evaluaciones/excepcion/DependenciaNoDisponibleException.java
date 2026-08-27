package servicio_evaluaciones.excepcion;
public class DependenciaNoDisponibleException extends RuntimeException {
    public DependenciaNoDisponibleException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
