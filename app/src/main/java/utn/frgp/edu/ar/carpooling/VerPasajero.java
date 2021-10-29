package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
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
import utn.frgp.edu.ar.carpooling.entities.Rol;
import utn.frgp.edu.ar.carpooling.entities.Usuario;
import utn.frgp.edu.ar.carpooling.utils.Helper;

public class VerPasajero extends AppCompatActivity {
    Context contexto;
    String NroViaje,EmailVerUsuario,RolVerUsuario,EstadoViaje;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    TextView Nombre,Telefono,CantidadCalificaciones;
    RatingBar Rating;
    GridView grillaVerPasajero;
    Button botoncancelar;

    boolean calificacionInicial;

    Usuario usuarioACalificar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pasajero);
        contexto = this;

        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);


        NroViaje=getIntent().getStringExtra("NroViaje");
        EmailVerUsuario=getIntent().getStringExtra("EmailVerUsuario");
        RolVerUsuario=getIntent().getStringExtra("RolVerUsuario");
        EstadoViaje = getIntent().getStringExtra("EstadoViaje");
        Nombre=findViewById(R.id.TxtVpNombre);
        Telefono=findViewById(R.id.TxtVpNumero);
        Rating=findViewById(R.id.RBVpPasajero);
        CantidadCalificaciones=findViewById(R.id.TxtVPViajocon);
        grillaVerPasajero=(GridView) findViewById(R.id.GrVpViaje);
        botoncancelar=findViewById(R.id.BtnVpCancelar);

        calificacionInicial = true;

        botoncancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CancelarPasajero().execute();
                Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
                pagVerViaje.putExtra("NroViaje",NroViaje);
                pagVerViaje.putExtra("EstadoViaje", EstadoViaje);
                startActivity(pagVerViaje);
            }
        });


        Rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                float nroEstrellas=0;
                String calificacion= String.valueOf(rating);

                if(calificacion.substring(1).equals(".0")){
                    calificacion = calificacion.substring(0,1);
                }

                AlertDialog.Builder vtnConfirmacion = new AlertDialog.Builder(contexto);
                vtnConfirmacion.setMessage("Esta seguro que quiere calificar al pasajero con "+ calificacion + " estrellas?");
                vtnConfirmacion.setCancelable(false);
                vtnConfirmacion.setTitle("Confirmacion de Calificacion");

                vtnConfirmacion.setPositiveButton("Si",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String calificacion= String.valueOf(rating);
                        if(calificacion.substring(1).equals(".0")){
                            calificacion = calificacion.substring(0,1);
                        }

                        Toast.makeText(contexto,"Califico con: " + calificacion + " estrellas.",Toast.LENGTH_LONG).show();
                    }
                });

                vtnConfirmacion.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                if(calificacionInicial == false){
                    AlertDialog alerta = vtnConfirmacion.create();
                    alerta.show();
                }

            }
        });


        new CargarDatos().execute();
        new CargarCalificaciones().execute();
        new ContarCalificaciones().execute();
        new CargarViajeSeleccionado().execute();

        if(EstadoViaje.equals("En Espera")){
            Rating.setIsIndicator(true);
        }
    }

    private class CargarDatos extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT Usuarios.nombre,";
                query += "Usuarios.Apellido,";
                query += "Usuarios.Telefono,";
                query += "Usuarios.Dni,";
                query += "Roles.Id as IdRol,";
                query += "Roles.Nombre as NombreRol";
                query += " FROM `Usuarios`";
                query += " INNER JOIN Roles";
                query += " ON Usuarios.Rol = Roles.Id";
                query += " Where Usuarios.Email='" + EmailVerUsuario + "' AND Usuarios.Rol = '" + RolVerUsuario + "'" ;
                return st.executeQuery(query);

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            usuarioACalificar = new Usuario();
            try {
                while (resultados.next()) {

                    usuarioACalificar.setNombre(resultados.getString("Nombre"));
                    usuarioACalificar.setApellido(resultados.getString("Apellido"));
                    usuarioACalificar.setTelefono(resultados.getString("Telefono"));
                    Rol r = new Rol();
                    r.setId(resultados.getString("IdRol"));
                    r.setNombre(resultados.getString("NombreRol"));
                    usuarioACalificar.setRol(r);
                    usuarioACalificar.setDni(resultados.getString("Dni"));

                    Nombre.setText(usuarioACalificar.getNombre() + " " + usuarioACalificar.getNombre());
                    Telefono.setText(usuarioACalificar.getTelefono());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarCalificaciones extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT AVG(cal.Calificacion) as promedio FROM Calificaciones cal Where cal.UsuarioEmail='" + EmailVerUsuario + "' AND UsuarioRol = '" + RolVerUsuario + "'";


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
                Rating.setRating(promedio);

                //funciona pero cuando lo deshabilito la puntuacion son todas las mitad de las estrellas
                //ratingBarconductor.setEnabled(false);
                calificacionInicial=false;

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
                query += "SELECT COUNT(cal.Calificacion) as cantidad FROM Calificaciones cal Where cal.UsuarioEmail='" + EmailVerUsuario + "' AND UsuarioRol = '" + RolVerUsuario + "'";


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

                CantidadCalificaciones.setText(cantidad > 0 ? cantidad.toString()  + " Conductores lo calificaron" : "No Viajo con ningun conductor");

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarViajeSeleccionado extends AsyncTask<Void,Integer,ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	vj.FechaHoraInicio,";
                query += "  	vj.Id,";
                query += " 		    pr1.Nombre ProvinciaOrigen,";
                query += "          ci1.Nombre CiudadOrigen,";
                query += "          pr2.Nombre ProvinciaDestino,";
                query += "          ci2.Nombre CiudadDestino";
                query += " FROM Viajes vj";
                query += " LEFT JOIN Provincias pr1";
                query += " 	ON pr1.Id = vj.ProvinciaOrigenId";
                query += " LEFT JOIN Provincias pr2";
                query += " 	ON pr2.Id = vj.ProvinciaDestinoId";
                query += " LEFT JOIN Ciudades ci1";
                query += " 	ON ci1.Id = vj.CiudadOrigenId";
                query += " LEFT JOIN Ciudades ci2";
                query += " 	ON ci2.Id = vj.CiudadDestinoId";
                query += " 	Where	vj.Id='" + NroViaje + "'";
                query += " ORDER BY FechaHoraInicio ASC";


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
                    itemsGrilla.add(item);
                }

                String[] from = {"NroViaje","origen", "destino", "fecha", "hora"};
                int[] to = {R.id.tvGridItemViajeNroViaje,R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora};
                SimpleAdapter simpleAdapter = new SimpleAdapter(contexto, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaVerPasajero.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class CancelarPasajero extends AsyncTask<Void,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	PasajerosPorViaje vj";
                query += "  	    SET";
                query += " 		    EstadoPasajero='Rechazado'";
                query += " 	Where	vj.UsuarioEmail='" + EmailVerUsuario + "' and vj.ViajeId='" + NroViaje + "'";


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

        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){
                Toast.makeText(contexto, "El  Pasajero a sido destituido de este viaje!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo destituir el pasajero  intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CalificarUsuario extends AsyncTask<Void,Integer,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	PasajerosPorViaje vj";
                query += "  	    SET";
                query += " 		    EstadoPasajero='Rechazado'";
                query += " 	Where	vj.UsuarioEmail='" + EmailVerUsuario + "' and vj.ViajeId='" + NroViaje + "'";


                int resultado = st.executeUpdate(query);


                if(resultado>0){
                    return true;
                }
                else {return false;}

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }


}