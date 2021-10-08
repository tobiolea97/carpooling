package utn.frgp.edu.ar.carpooling.conexion;

public class DataDB {

    //Información de la BD
    public static final String host="sql10.freesqldatabase.com";
    public static final String port="3306";
    public static final String nameBD="sql10441832";
    public static final String user="sql10441832";
    public static final String pass="htqH2SiCQg";

    //Información para la conexion
    public static final String urlMySQL = "jdbc:mysql://" + host + ":" + port + "/"+nameBD;
    public static final String driver = "com.mysql.jdbc.Driver";

}