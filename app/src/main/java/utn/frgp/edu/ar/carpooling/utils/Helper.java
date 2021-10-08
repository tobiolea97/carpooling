package utn.frgp.edu.ar.carpooling.utils;

public class Helper {

    public static String RemoverCaracteresSQLInjection (String text) {
        return text.replace("\'","").replace("\"","");
    }
}
