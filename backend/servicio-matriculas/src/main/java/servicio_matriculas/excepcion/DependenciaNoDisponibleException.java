package servicio_matriculas.excepcion;

public class DependenciaNoDisponibleException extends RuntimeException {
    public DependenciaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
