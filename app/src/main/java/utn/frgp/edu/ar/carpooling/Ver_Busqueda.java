package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.entities.Viaje;

public class Ver_Busqueda extends AppCompatActivity {
    Context contexto;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    String NroViaje;
    String EstadoViaje;
    GridView grillaverbusqueda;
    RatingBar RbVerbusqueda;
    Button AceptarViaje;
    Spinner CantAsientos;
    TextView Nombre,Celular,ViajoCon;
    Viaje viaj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_busqueda);
        contexto = this;
        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);

        NroViaje=getIntent().getStringExtra("NroViaje");
        EstadoViaje=getIntent().getStringExtra("EstadoViaje");


        grillaverbusqueda=findViewById(R.id.GrVerBusqueda);
        RbVerbusqueda=findViewById(R.id.RatBarVer_Busq);
        AceptarViaje=findViewById(R.id.BtnVerBusqAcept);
        CantAsientos=findViewById(R.id.spinnerVerbusqueda);
        Nombre=findViewById(R.id.TxtNombreVerBusq);
        Celular=findViewById(R.id.TxtCelVerBusque);
        ViajoCon=findViewById(R.id.TxtViajoVerBusq);


        new CargarViajeSeleccionado().execute();
        new CargarDatos().execute();
        new CargarCalificacion().execute();
        new ContarCalificacion().execute();

    }

    private class CargarViajeSeleccionado extends AsyncTask<String,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(String... queries) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += " SELECT vj.Id, vj.PasajeroEmail,po.Nombre ProvinciaOrigen, co.Nombre CiudadOrigen, pd.Nombre ProvinciaDestino, cd.Nombre CiudadDestino, vj.FechaHoraInicio, vj.EstadoSolicitud ";
                query += " FROM Solicitudes vj ";
                query += " LEFT JOIN Provincias po ";
                query += " 	ON vj.ProvinciaOrigenId = po.Id ";
                query += " LEFT JOIN Ciudades co ";
                query += " 	ON vj.CiudadOrigenId = co.Id ";
                query += " LEFT JOIN Provincias pd ";
                query += " 	ON vj.ProvinciaDestinoId = pd.Id ";
                query += " LEFT JOIN Ciudades cd  ";
                query += " 	ON vj.CiudadDestinoId = cd.Id ";
                query += " 	Where	vj.Id='" + NroViaje + "'";



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
                    item.put("NroViaje", resultados.getString("Id"));
                    item.put("origen", resultados.getString("CiudadOrigen") + ", " + resultados.getString("ProvinciaOrigen"));
                    item.put("destino", resultados.getString("CiudadDestino") + ", " + resultados.getString("ProvinciaDestino"));
                    item.put("fecha", resultados.getString("FechaHoraInicio").substring(8,10) + "/" + resultados.getString("FechaHoraInicio").substring(5,7) + "/" + resultados.getString("FechaHoraInicio").substring(2,4));
                    item.put("hora", resultados.getString("FechaHoraInicio").substring(11,13) + ":" + resultados.getString("FechaHoraInicio").substring(14,16));
                    item.put("estado",resultados.getString("EstadoSolicitud"));
                    itemsGrilla.add(item);
                }

                String[] from = {"NroViaje","origen", "destino", "fecha", "hora","estado"};
                int[] to = {R.id.tvGridItemViajeNroViaje,R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora,R.id.tvGridItemEstadoViaje};
                SimpleAdapter simpleAdapter = new SimpleAdapter(contexto, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaverbusqueda.setAdapter(simpleAdapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class CargarDatos extends AsyncTask<Void,Integer,ResultSet> {
        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	usu.Nombre,";
                query += "  	    usu.Apellido,";
                query += " 		    usu.Telefono,";
                query += " 		    vj.CantidadAcompaniantes";
                query += " FROM Solicitudes vj";
                query += " Inner join Usuarios usu";
                query += " ON usu.Email=vj.PasajeroEmail";
                query += " 	Where	vj.Id='" + NroViaje + "'";

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
            int cantidadAcompañantes=0;
                while (resultados.next()) {
            Nombre.setText(resultados.getString("Nombre")+" "+resultados.getString("Apellido"));
            Celular.setText(resultados.getString("Telefono"));
            cantidadAcompañantes=resultados.getInt("CantidadAcompaniantes");
                }
                ArrayList<String> listaCantPasajeros = new ArrayList<String>();
                for(int i = cantidadAcompañantes; i<=4; i++){
                    listaCantPasajeros.add(String.valueOf(i));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaCantPasajeros);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                CantAsientos.setAdapter(adapter);


            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarCalificacion extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT AVG(cal.Calificacion) as promedio FROM Calificaciones cal inner join Usuarios usu on usu.Email=cal.UsuarioEmail inner join Solicitudes vj on usu.Email=vj.PasajeroEmail where vj.Id='" + NroViaje + "' and cal.UsuarioRol='PAS' ";


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

                //Le agrego el promedio al rating para que pueda mostrarlo
                RbVerbusqueda.setRating(promedio);
                RbVerbusqueda.setIsIndicator(true);




            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class ContarCalificacion extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT COUNT(cal.Calificacion) as cantidad FROM Calificaciones cal inner join Usuarios usu on usu.Email=cal.UsuarioEmail inner join Solicitudes vj on usu.Email=vj.PasajeroEmail where vj.Id='" + NroViaje + "' and cal.UsuarioRol='PAS' ";


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

                ViajoCon.setText(cantidad > 0 ? cantidad.toString()  + " Conductores lo calificaron" : "No Viajo con ningun conductor");

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}