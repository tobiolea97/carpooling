package utn.frgp.edu.ar.carpooling;

import static java.time.temporal.ChronoUnit.DAYS;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import java.time.LocalDate;
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
import utn.frgp.edu.ar.carpooling.negocioImpl.viajeNegImpl;

public class VerPasajero extends AppCompatActivity {
    Context contexto;
    String NroViaje,EmailVerUsuario,RolVerUsuario,EstadoViaje;
    String nombreUsuarioLog, apellidoUsuarioLog, emailUsuarioLog, rolUsuarioLog;
    TextView Nombre,Telefono,CantidadCalificaciones;
    RatingBar Rating;
    GridView grillaVerPasajero;
    Button botonDesAsignarUsuario;
    Button botonVolver;

    float CalificacionDada;
    boolean calificacionInicial;

    Usuario usuarioACalificar;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pasajero);
        contexto = this;

        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuarioLog = spSesion.getString("Nombre","No hay datos");
        apellidoUsuarioLog = spSesion.getString("Apellido","No hay datos");
        emailUsuarioLog = spSesion.getString("Email","No hay datos");
        rolUsuarioLog = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuarioLog+" "+ apellidoUsuarioLog +" Rol: "+ rolUsuarioLog);


        NroViaje=getIntent().getStringExtra("NroViaje");
        EmailVerUsuario=getIntent().getStringExtra("EmailVerUsuario");
        RolVerUsuario=getIntent().getStringExtra("RolVerUsuario");
        EstadoViaje = getIntent().getStringExtra("EstadoViaje");
        Nombre=findViewById(R.id.TxtVpNombre);
        Telefono=findViewById(R.id.TxtVpNumero);
        Rating=findViewById(R.id.RBVpPasajero);
        CantidadCalificaciones=findViewById(R.id.TxtVPViajocon);
        grillaVerPasajero=(GridView) findViewById(R.id.GrVpViaje);
        botonDesAsignarUsuario =findViewById(R.id.BtnVpCancelar);
        botonVolver = findViewById(R.id.btnVpVolver);

        botonVolver.setVisibility(View.VISIBLE);
        calificacionInicial = true;
        CalificacionDada = 0;

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
                        CalificacionDada = Float.valueOf(calificacion);

                        new CalificarUsuario().execute();

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

        new VerificarCalificacion().execute(); //VERIFICA SI YA FUE DADA UNA CALIFICACION PARA EL USUARIO

        if(EstadoViaje.equals("En Espera") || EstadoViaje.equals("Cancelado")){
            Rating.setIsIndicator(true);
        }

        if(EstadoViaje.equals("Cancelado") || rolUsuarioLog.equals("PAS")){
            botonDesAsignarUsuario.setVisibility(View.INVISIBLE);
        }

        if(EstadoViaje.equals("Finalizado")){

            botonDesAsignarUsuario.setVisibility(View.INVISIBLE);
            /*viajeNegImpl vNegImpl = new viajeNegImpl();
            LocalDate fechaFinalizacionViaje =  LocalDate.of(2020,10,01);
            try {
                fechaFinalizacionViaje  = vNegImpl.ObtenerFechaFinalizacionViaje(Integer.parseInt(NroViaje));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            LocalDate fechaActual = LocalDate.now();

            long numberOFDays = DAYS.between(fechaActual,fechaFinalizacionViaje );

            if(numberOFDays >= 1){
                Rating.setIsIndicator(true);
            }*/
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ClickDesAsignarPasajero(View view) throws ExecutionException, InterruptedException {


        new CancelarPasajero().execute();
        Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
        pagVerViaje.putExtra("NroViaje",NroViaje);
        pagVerViaje.putExtra("EstadoViaje", EstadoViaje);
        startActivity(pagVerViaje);


    }

    public void ClickVolver(View view){
        finish();
    }

    private class CargarDatos extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " CALL InfoParaResponderSolicitud('" + EmailVerUsuario + "'," + NroViaje + ");";

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
                List<Map<String, String>> itemsGrilla = new ArrayList<Map<String, String>>();
                while (resultados.next()) {

                    usuarioACalificar.setNombre(resultados.getString("Nombre"));
                    usuarioACalificar.setApellido(resultados.getString("Apellido"));
                    usuarioACalificar.setTelefono(resultados.getString("Telefono"));
                    Rol r = new Rol();
                    r.setId(resultados.getString("IdRol"));
                    r.setNombre(resultados.getString("NombreRol"));
                    usuarioACalificar.setRol(r);
                    usuarioACalificar.setDni(resultados.getString("Dni"));
                    promedio = resultados.getFloat("Promedio");
                    cantidad = resultados.getInt("cantidad");
                    CantidadCalificaciones.setText(cantidad > 0 ? cantidad.toString()  + " calificaciones recibidas" : "No Viajo con ningun conductor");

                    Nombre.setText(usuarioACalificar.getNombre() + " " + usuarioACalificar.getApellido());
                    Telefono.setText(usuarioACalificar.getTelefono());
                    Rating.setRating(promedio);

                    calificacionInicial=false;

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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){
                NotificacionesNegImpl NotiNeg = new NotificacionesNegImpl();
                Notificaciones Noti = new Notificaciones();
                Noti.setUsuarioEmail(EmailVerUsuario);
                Noti.setUsuarioRolId(RolVerUsuario);
                Noti.setMensaje("Has sido desasignado del  nro de viaje "+NroViaje);
                Noti.setEstadoNotificacion("P");
                Noti.setEstado(1);
                try {
                    NotiNeg.AñadirNotificacion(Noti);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(contexto, "El  Pasajero a sido desasignado de este viaje!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo desasignar el pasajero  intente nuevamente.", Toast.LENGTH_SHORT).show();
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
                query += "INSERT INTO `Calificaciones` ";
                query += "(UsuarioEmail, ";
                query += "UsuarioRol, ";
                query += "CalificadorEmail, ";
                query += "CalificadorRol, ";
                query += "ViajeId, ";
                query += "Calificacion) ";
                query += "VALUES";
                query += "(";
                query +=  "'" + EmailVerUsuario+ "',";
                query +=  "'" + RolVerUsuario+ "',";
                query +=  "'" + emailUsuarioLog+ "',";
                query +=  "'" + rolUsuarioLog+ "',";
                query +=  "'" + NroViaje+ "',";
                query +=  "'" + CalificacionDada+ "'";
                query += ")";

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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){

                String calificacion= String.valueOf(CalificacionDada);
                if(calificacion.substring(1).equals(".0")){
                    calificacion = calificacion.substring(0,1);
                }

                NotificacionesNegImpl NotiNeg = new NotificacionesNegImpl();
                Notificaciones Noti = new Notificaciones();
                Noti.setUsuarioEmail(EmailVerUsuario);
                Noti.setUsuarioRolId(RolVerUsuario);
                Noti.setMensaje("El usuario " + nombreUsuarioLog + " " + apellidoUsuarioLog + "te ha calificado con " + calificacion + "estrellas. Por el viaje: " + NroViaje);
                Noti.setEstadoNotificacion("P");
                Noti.setEstado(1);

                try {
                    NotiNeg.AñadirNotificacion(Noti);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Rating.setIsIndicator(true);
                Toast.makeText(contexto,"Califico con: " + calificacion + " estrellas.",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(contexto, "No se pudo calificar al usuario intente nuevamente.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class VerificarCalificacion extends AsyncTask<Void,Integer,ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT Id FROM Calificaciones WHERE UsuarioEmail = '" + EmailVerUsuario + "' AND UsuarioRol = '" + RolVerUsuario + "' AND CalificadorEmail = '" + emailUsuarioLog +"' AND CalificadorRol = '" + rolUsuarioLog + "'";

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
                    Rating.setIsIndicator(true);
                }

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}