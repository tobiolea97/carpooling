package utn.frgp.edu.ar.carpooling.utils;

import android.widget.EditText;

public class Validadores {

    private static String regExpNoNumbers = "^([^0-9]*)$";
    private static String regExpHasNonNumericChar = "[^0-9]";
    private static String regExpEmail = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+";
    private static String regExpDate = "[0-9]{1,2}-/[0-9]{1,2}-/[0-9]{4}";

    public static boolean validarNombre(boolean flag, EditText nombre) {

        if(nombre.getText().toString().equals("")) {
            nombre.setError("Campo obligatorio");
            return false;
        }
        if(!nombre.getText().toString().matches(regExpNoNumbers)) {
            nombre.setError("Este campo no admite números");
            return false;
        }
        if(nombre.getText().toString().length() >= 20) {
            nombre.setError("Este campo admite un maximo de 20 characteres");
            return false;
        }
        nombre.setError(null);
        return flag;
    }

    public static boolean validarApellido(boolean flag, EditText apellido) {

        if(apellido.getText().toString().equals("")) {
            apellido.setError("Campo obligatorio");
            return false;
        }
        if(!apellido.getText().toString().matches(regExpNoNumbers)) {
            apellido.setError("Este campo no admite números");
            return false;
        }
        if(apellido.getText().toString().length() >= 20) {
            apellido.setError("Este campo admite un maximo de 20 characteres");
            return false;
        }
        apellido.setError(null);
        return flag;
    }

    public static boolean validarEmail(boolean flag, EditText mail) {
        if(mail.getText().toString().equals("")){
            mail.setError("Campo obligatorio");
            return false;
        }
        if(!mail.getText().toString().matches(regExpEmail)) {
            mail.setError("Fromato requerido: ejemplo@dominio.com");
            return false;
        }
        if(mail.getText().toString().length() >= 30) {
            mail.setError("Este campo admite un maximo de 30 characteres");
            return false;
        }
        mail.setError(null);
        return flag;
    }

    public static boolean validarTelefono(boolean flag, EditText telefono) {
        if(telefono.getText().toString().equals("")) {
            telefono.setError("Campo obligatorio");
            return false;
        }
        if(telefono.getText().toString().matches(regExpHasNonNumericChar)) {
            telefono.setError("Este campo solo admite números");
            return false;
        }
        if(telefono.getText().toString().length() >= 15) {
            telefono.setError("Este campo admite un máximo de 15 caracteres");
            return false;
        }
        telefono.setError(null);
        return flag;
    }

    public static boolean validarDireccion(boolean flag, EditText direccion) {
        if(direccion.getText().toString().equals(""))
            return flag;
        if(direccion.getText().toString().length() >= 30) {
            direccion.setError("Este campo admite un máximo de 30 caracteres");
            return false;
        }
        direccion.setError(null);
        return flag;
    }

    public static boolean validarDNI(boolean flag, EditText dni) {
        if(dni.getText().toString().equals("")) {
            dni.setError("Campo obligatorio");
            return false;
        }
        if(dni.getText().toString().length() > 8 || dni.getText().toString().length() < 7) {
            dni.setError("Ingrese un número de 7 u 8 caracteres");
            return false;
        }
        dni.setError(null);
        return flag;
    }

}
