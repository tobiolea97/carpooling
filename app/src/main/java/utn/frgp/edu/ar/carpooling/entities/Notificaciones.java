package utn.frgp.edu.ar.carpooling.entities;

public class Notificaciones {


    private int id;
    private String UsuarioEmail;
    private String UsuarioRolId;
    private String Mensaje;
    private boolean estado;

    public Notificaciones() {
    }

    public Notificaciones(int id, String usuarioEmail, String usuarioRolId, String mensaje, boolean estado) {
        this.id = id;
        UsuarioEmail = usuarioEmail;
        UsuarioRolId = usuarioRolId;
        Mensaje = mensaje;
        this.estado = estado;
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

    public boolean getEstado() {
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

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
