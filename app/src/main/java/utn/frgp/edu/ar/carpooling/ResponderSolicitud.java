package utn.frgp.edu.ar.carpooling;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.concurrent.ExecutionException;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.entities.Notificaciones;
import utn.frgp.edu.ar.carpooling.entities.Rol;
import utn.frgp.edu.ar.carpooling.entities.Usuario;
import utn.frgp.edu.ar.carpooling.negocioImpl.NotificacionesNegImpl;

public class ResponderSolicitud extends AppCompatActivity {
    Context contexto;
    TextView Nombre,viajocon, Numero, tvNoFreeSeats;
    String Email,NroViaje,Asientos,Pasajeros;
    RatingBar Rating;
    Button botoncancelar,botonaceptar;
    GridView grillaVerViaje;
    Usuario usuarioACalificar;
    String nombreUsuario,apellidoUsuario,emailUsuario,rolUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_solicitud);
        contexto = this;

        NroViaje=getIntent().getStringExtra("NroViaje");
        Email=getIntent().getStringExtra("Email");
        Nombre=findViewById(R.id.TxtNombreRespSol);
        Rating=findViewById(R.id.ratingBarResponderSoli);
        viajocon=findViewById(R.id.TxtViajoRespSol);
        grillaVerViaje=(GridView) findViewById(R.id.GrResponderSoli);
        botoncancelar=findViewById(R.id.btnResponderSoliRechazar);
        botonaceptar=findViewById(R.id.btnRespSoliAceptar);
        Numero=findViewById(R.id.textView17);
        tvNoFreeSeats=findViewById(R.id.textView20);


        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);

        botoncancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RechazarPasajero().execute();
               // Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
                //pagVerViaje.putExtra("NroViaje",NroViaje);
                //pagVerViaje.putExtra("EstadoViaje", EstadoViaje);
               // startActivity(pagVerViaje);
            }
        });

        botonaceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CargarAsientosDisponibles().execute();


                // Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
                //pagVerViaje.putExtra("NroViaje",NroViaje);
                //pagVerViaje.putExtra("EstadoViaje", EstadoViaje);
                // startActivity(pagVerViaje);
            }
        });


        new CargarDatos().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu miMenu) {

        getMenuInflater().inflate(R.menu.menu_conductor, miMenu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem opcionMenu) {
        int id = opcionMenu.getItemId();

        if(id == R.id.miperfil) {
            finish();
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }
        if(id == R.id.misViajes) {
            finish();
            Intent intent = new Intent(this, MisViajes.class);
            startActivity(intent);
        }

        if(id == R.id.crearViaje) {
            finish();
            Intent intent = new Intent(this, NuevoViaje.class);
            startActivity(intent);
        }

        if(id == R.id.cerrarSesion) {

            SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spSesion.edit();
            editor.clear();
            editor.commit();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(opcionMenu);
    }

    private class CargarDatos extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " CALL InfoParaResponderSolicitud('" + Email + "'," + NroViaje + ");";

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
                Float promedio = 0f;
                Integer cantidad = null;
                Integer asientosLibres = 0;
                List<Map<String, String>> itemsGrilla = new ArrayList<Map<String, String>>();
                while (resultados.next()) {

                    Nombre.setText(resultados.getString("Nombre") + " " + resultados.getString("Apellido"));
                    Numero.setText(resultados.getString("Telefono"));

                    Rol r = new Rol();
                    r.setId(resultados.getString("IdRol"));
                    r.setNombre(resultados.getString("NombreRol"));
                    usuarioACalificar.setRol(r);
                    usuarioACalificar.setDni(resultados.getString("Dni"));
                    promedio = resultados.getFloat("Promedio");
                    cantidad = resultados.getInt("Cantidad");
                    asientosLibres = resultados.getInt("EspaciosDisponibles");

                    if(asientosLibres <= 0) {
                        botonaceptar.setEnabled(false);
                        tvNoFreeSeats.setVisibility(View.VISIBLE);
                    }

                    viajocon.setText(cantidad > 0 ? cantidad.toString()  + " calificaciones recibidas" : "No Viajo con ningun conductor");
                    Rating.setRating(promedio);

                    Rating.setIsIndicator(true);

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
                grillaVerViaje.setAdapter(simpleAdapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarDatos2 extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	usu.Nombre,";
                query += "  	    usu.Apellido,";
                query += " 		    usu.Telefono";
                query += " FROM Usuarios usu";
                query += " 	Where	usu.Email='" + Email + "'";
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


                while (resultados.next()) {
                    Nombre.setText(resultados.getString("Nombre")+" "+resultados.getString("Apellido"));
                    Numero.setText(resultados.getString("Telefono"));


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
                query += "SELECT AVG(cal.Calificacion) as promedio FROM Calificaciones cal inner join Usuarios usu on usu.Email=cal.UsuarioEmail  Where	usu.Email='" + Email + "'";


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
                query += "SELECT COUNT(cal.Calificacion) as cantidad FROM Calificaciones cal inner join Usuarios usu on usu.Email=cal.UsuarioEmail  Where	usu.Email='" + Email + "'";


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

                viajocon.setText(cantidad > 0 ? cantidad.toString()  + " Conductores lo calificaron" : "No Viajo con ningun conductor");

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
                grillaVerViaje.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class RechazarPasajero extends AsyncTask<Void,Integer,Boolean> {

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
                query += " 	Where	vj.UsuarioEmail='" + Email + "' and vj.ViajeId='" + NroViaje + "'";


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

                NotificacionesNegImpl NotiNeg = new NotificacionesNegImpl();
                Notificaciones Noti = new Notificaciones();
                Noti.setUsuarioEmail(Email);
                Noti.setUsuarioRolId("PAS");
                Noti.setMensaje(" El nro de viaje "+NroViaje+" rechazo tu solicitud");
                Noti.setEstadoNotificacion("P");
                Noti.setEstado(1);
                try {
                    NotiNeg.AñadirNotificacion(Noti);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Toast.makeText(contexto, "La solicitud fue rechazada correctamente.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(contexto, "No se pudo rechazar la solicitud  intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AceptarPasajero extends AsyncTask<Void,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	PasajerosPorViaje vj";
                query += "  	    SET";
                query += " 		    EstadoPasajero='Aceptado'";
                query += " 	Where	vj.UsuarioEmail='" + Email + "' and vj.ViajeId='" + NroViaje + "'";


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
                NotificacionesNegImpl NotiNeg = new NotificacionesNegImpl();
                Notificaciones Noti = new Notificaciones();
                Noti.setUsuarioEmail(Email);
                Noti.setUsuarioRolId("PAS");
                Noti.setMensaje(" El nro de viaje "+NroViaje+" ha aceptado tu solicitud");
                Noti.setEstadoNotificacion("P");
                Noti.setEstado(1);
                try {
                    NotiNeg.AñadirNotificacion(Noti);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(contexto, "La solicitud fue Aceptada correctamente.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(contexto, "No se pudo Aceptar la solicitud  intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CargarAsientosDisponibles extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	vj.CantidadPasajeros,";
                query += "  	  Count(pv.UsuarioEmail) as Pasajeros";
                query += " FROM Viajes vj";
                query += " Inner join PasajerosPorViaje pv";
                query += " ON pv.ViajeId=vj.Id";
                query += " 	Where	pv.EstadoPasajero='Aceptado' and vj.Id='" + NroViaje + "'";
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


                while (resultados.next()) {

                    Asientos=resultados.getString("CantidadPasajeros");
                    Pasajeros=resultados.getString("Pasajeros");



                }
            int resultado=(Integer.parseInt(Asientos)-Integer.parseInt(Pasajeros))-1;

              if(resultado>=0&&resultado<=Integer.parseInt(Asientos)){
                  new AceptarPasajero().execute();

              }
              else{
                  Toast.makeText(contexto, "No se pudo Aceptar la solicitud  ya que la cantidad de asiento .", Toast.LENGTH_SHORT).show();

              }




            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}