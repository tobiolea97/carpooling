package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.utils.Helper;

public class HomeConductor extends AppCompatActivity {

    TextView Info, cantidadCalificaciones;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    Context context;
    ImageView st1, st2, st3, st4, st5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_conductor);

        context = this;
        st1 = (ImageView) findViewById(R.id.ivHomeConductorStar1);
        st2 = (ImageView) findViewById(R.id.ivHomeConductorStar2);
        st3 = (ImageView) findViewById(R.id.ivHomeConductorStar3);
        st4 = (ImageView) findViewById(R.id.ivHomeConductorStar4);
        st5 = (ImageView) findViewById(R.id.ivHomeConductorStar5);
        cantidadCalificaciones = (TextView)findViewById(R.id.ivHomeConductorCalificaciones);
        cantidadCalificaciones.setText("");

        Info = findViewById(R.id.tvPreRegistroTitulo);
        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);

        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");

        Info.setText(nombreUsuario + " " + apellidoUsuario);

        new CargarCalificaciones().execute();
        new ContarCalificaciones().execute();
    }

    private class CargarCalificaciones extends AsyncTask<Void,Integer,ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT AVG(Calificacion) as promedio FROM Calificaciones WHERE UsuarioEmail = '";
                query += emailUsuario;
                query += " ' AND UsuarioRol = '";
                query += rolUsuario + "'";

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
                Float promedio = null;
                while (resultados.next()) {
                    promedio = resultados.getFloat("promedio");
                }

                if(promedio == 0 ) return;
                Helper.MostrarEstrellas(st1,st2,st3,st4,st5,promedio);

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class ContarCalificaciones extends AsyncTask<Void,Integer,ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT COUNT(Calificacion) as cantidad FROM Calificaciones WHERE UsuarioEmail = '";
                query += emailUsuario;
                query += " ' AND UsuarioRol = '";
                query += rolUsuario + "'";

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
                Integer cantidad = null;
                while (resultados.next()) {
                    cantidad = resultados.getInt("cantidad");
                }

                cantidadCalificaciones.setText(cantidad > 0 ? cantidad.toString()  + " calificaciones recibidas" : "Sin calificacion");

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}