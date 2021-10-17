package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.sql.*;
import java.util.*;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.utils.Helper;

public class Home extends AppCompatActivity {

    TextView Info, cantidadCalificaciones;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    Context context;
    ImageView st1, st2, st3, st4, st5;
    RatingBar ratingBarconductor;
    GridView grillaViajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;
        st1 = (ImageView) findViewById(R.id.ivHomeStar1);
        st2 = (ImageView) findViewById(R.id.ivHomeStar2);
        st3 = (ImageView) findViewById(R.id.ivHomeStar3);
        st4 = (ImageView) findViewById(R.id.ivHomeStar4);
        st5 = (ImageView) findViewById(R.id.ivHomeStar5);
        grillaViajes = (GridView) findViewById(R.id.gvHomeProximosVIajes);
        cantidadCalificaciones = (TextView)findViewById(R.id.ivHomeCalificaciones);
        cantidadCalificaciones.setText("");

        Info = findViewById(R.id.tvPreRegistroTitulo);
        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);

        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");

        Info.setText(nombreUsuario + " " + apellidoUsuario);

        new Home.CargarCalificaciones().execute();
        new Home.ContarCalificaciones().execute();
        new Home.CargarProximosViajes().execute();
    }

    public void onClickMisViajes (View view) {
        Intent pagMisViajes= new Intent(context, MisViajes.class);
        startActivity(pagMisViajes);
        finish();
    }

    private class CargarCalificaciones extends AsyncTask<Void,Integer, ResultSet> {

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
                //Le agrego el promedio al rating para que pueda mostrarlo
                //ratingBarconductor.setRating(promedio);
                //funciona pero cuando lo deshabilito la puntuacion son todas las mitad de las estrellas
                //ratingBarconductor.setEnabled(false);

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

    private class CargarProximosViajes extends AsyncTask<Void,Integer,ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += " SELECT 	vj.FechaHoraInicio,";
                query += " 		    pr1.Nombre ProvinciaOrigen,";
                query += "          ci1.Nombre CiudadOrigen,";
                query += "          pr2.Nombre ProvinciaDestino,";
                query += "          ci2.Nombre CiudadDestino";
                query += " FROM Viajes vj";
                query += rolUsuario.equals("PAS") ? " INNER JOIN PasajerosPorViaje ppv ON ppv.ViajeId = vj.Id" : "";
                query += " LEFT JOIN Provincias pr1";
                query += " 	ON pr1.Id = vj.ProvinciaOrigenId";
                query += " LEFT JOIN Provincias pr2";
                query += " 	ON pr2.Id = vj.ProvinciaDestinoId";
                query += " LEFT JOIN Ciudades ci1";
                query += " 	ON ci1.Id = vj.CiudadOrigenId";
                query += " LEFT JOIN Ciudades ci2";
                query += " 	ON ci2.Id = vj.CiudadDestinoId";
                query += rolUsuario.equals("PAS") ? " WHERE ppv.UsuarioEmail = '" + emailUsuario + "' AND" : "";
                query += rolUsuario.equals("CON") ? " WHERE 	vj.ConductorEmail = '" + emailUsuario + "' AND" : "";
                query += " 		vj.EstadoViaje IN ('1','En Espera')";
                query += " ORDER BY FechaHoraInicio ASC";
                query += " LIMIT 3";

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
                List<Map<String, String>> itemsGrilla = new ArrayList<Map<String, String>>();

                while (resultados.next()) {
                    Map<String, String> item = new HashMap<String, String>();
                    item.put("origen", resultados.getString("CiudadOrigen") + ", " + resultados.getString("ProvinciaOrigen"));
                    item.put("destino", resultados.getString("CiudadDestino") + ", " + resultados.getString("ProvinciaDestino"));
                    item.put("fecha", resultados.getString("FechaHoraInicio").substring(8,10) + "/" + resultados.getString("FechaHoraInicio").substring(5,7) + "/" + resultados.getString("FechaHoraInicio").substring(2,4));
                    item.put("hora", resultados.getString("FechaHoraInicio").substring(11,13) + ":" + resultados.getString("FechaHoraInicio").substring(14,16));
                    itemsGrilla.add(item);
                }

                String[] from = {"origen", "destino", "fecha", "hora"};
                int[] to = {R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora};
                SimpleAdapter simpleAdapter = new SimpleAdapter(context, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaViajes.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}