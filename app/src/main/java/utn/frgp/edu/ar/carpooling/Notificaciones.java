package utn.frgp.edu.ar.carpooling;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.negocioImpl.NotificacionesNegImpl;

public class Notificaciones extends AppCompatActivity {
    ListView LvNotificacion;
    Context contexto;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        contexto = this;

        LvNotificacion=findViewById(R.id.LvNotificaciones);
        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);
        new CargarNotificaciones().execute();
    }

    private class CargarNotificaciones extends AsyncTask<Void,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	noti.Mensaje";
                query += " FROM Notificaciones noti";
                query += " 	Where	noti.UsuarioEmail='" + emailUsuario + "'and noti.UsuarioRol='"+rolUsuario+"'and noti.EstadoNotificacion='P'";

                return st.executeQuery(query);

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                ArrayList<String> Mensajes= new ArrayList<String>();


                while (resultados.next()) {

                    Mensajes.add(resultados.getString("Mensaje"));

                }



                ArrayAdapter<String> adapter= new ArrayAdapter<>(contexto,R.layout.list_item_viajes,Mensajes);
                LvNotificacion.setAdapter(adapter);

                new MensajeLeido().execute();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class MensajeLeido extends AsyncTask<Void,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	Notificaciones noti";
                query += "  	    SET";
                query += " 		    EstadoNotificacion='L'";
                query += " 	Where	noti.UsuarioEmail='" + emailUsuario + "' and noti.UsuarioRol='" + rolUsuario + "'";


                int resultado = st.executeUpdate(query);


                if(resultado>0){
                    return true;
                }
                else {return false;}

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){

            }else{

            }
        }
    }

}