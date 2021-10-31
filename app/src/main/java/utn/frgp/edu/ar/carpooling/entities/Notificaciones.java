package utn.frgp.edu.ar.carpooling.entities;

public class Notificaciones {


    private int id;
    private String UsuarioEmail;
    private String UsuarioRolId;
    private String Mensaje;
    private String EstadoNotificacion;
    private int estado;

    public Notificaciones() {
    }

    public Notificaciones(int id, String usuarioEmail, String usuarioRolId, String mensaje, String estadoNotificacion, int estado) {
        this.id = id;
        UsuarioEmail = usuarioEmail;
        UsuarioRolId = usuarioRolId;
        Mensaje = mensaje;
        EstadoNotificacion = estadoNotificacion;
        this.estado = estado;
    }

    public String getEstadoNotificacion() {
        return EstadoNotificacion;
    }

    public void setEstadoNotificacion(String estadoNotificacion) {
        EstadoNotificacion = estadoNotificacion;
    }

    public int getId() {
        return id;
    }

    public String getUsuarioEmail() {
        return UsuarioEmail;
    }

    public String getUsuarioRolId() {
        return UsuarioRolId;
    }

    public String getMensaje() {
        return Mensaje;
    }

    public int getEstado() {
        return estado;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        UsuarioEmail = usuarioEmail;
    }

    public void setUsuarioRolId(String usuarioRolId) {
        UsuarioRolId = usuarioRolId;
    }

    public void setMensaje(String mensaje) {
        Mensaje = mensaje;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
