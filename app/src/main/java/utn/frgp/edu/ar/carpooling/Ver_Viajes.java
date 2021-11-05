package utn.frgp.edu.ar.carpooling;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.entities.Notificaciones;
import utn.frgp.edu.ar.carpooling.negocioImpl.NotificacionesNegImpl;

public class Ver_Viajes extends AppCompatActivity {
    Context contexto;
    GridView grillaverViaje;
    String NroViaje;
    String EstadoViaje;
    TextView TituloPasajeros;
    ListView Pasajeros,Solicitudes;
    ArrayList<String> EmailPasajeros;
    ArrayList<String> IdSolicitudes;
    ImageButton cancelar,finalizar,editar;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    TextView tituloCancelar,tituloFinalizar,tituloEditar;
    String localDateviaje;
    View dialogFragmentView, dialogFragmentView2;
    AlertDialog confirmarCancelacion, confirmarFinalizacion;
    String estadoViaje;
    boolean shouldExecuteOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_viajes);
        contexto = this;
        shouldExecuteOnResume = false;

        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);

        NroViaje=getIntent().getStringExtra("NroViaje");
        EstadoViaje=getIntent().getStringExtra("EstadoViaje");
        grillaverViaje= (GridView) findViewById(R.id.GrVerviaje);
        Pasajeros=findViewById(R.id.LVPasajeros);
        Solicitudes=findViewById(R.id.LvSolicitudes);
        TituloPasajeros=findViewById(R.id.textView10);
        cancelar=findViewById(R.id.imageButton4);
        finalizar=findViewById(R.id.imageButton5);
        editar=findViewById(R.id.imageButton3);
        tituloCancelar=findViewById(R.id.textView12);
        tituloFinalizar=findViewById(R.id.textView13);
        tituloEditar=findViewById(R.id.textView9);

        LayoutInflater inflater = this.getLayoutInflater();
        dialogFragmentView = inflater.inflate(R.layout.fragment_confirmar_accion, null);

        LayoutInflater inflater2 = this.getLayoutInflater();
        dialogFragmentView2 = inflater2.inflate(R.layout.fragment_confirmar_accion, null);

        new CargarViajeSeleccionado().execute();
        new CargarPasajeros().execute();

        if(EstadoViaje.equals("Finalizado") || EstadoViaje.equals("Cancelado")){
            Solicitudes.setVisibility(View.INVISIBLE);
            TextView txtSolicitudes = findViewById(R.id.TxtSolicitudes);
            txtSolicitudes.setVisibility(View.INVISIBLE);
            cancelar.setVisibility(View.INVISIBLE);
            finalizar.setVisibility(View.INVISIBLE);
            editar.setVisibility(View.INVISIBLE);
            tituloCancelar.setVisibility(View.INVISIBLE);
            tituloFinalizar.setVisibility(View.INVISIBLE);
            tituloEditar.setVisibility(View.INVISIBLE);
        } else {
            new CargarSolicitudes().execute();
        }

        Pasajeros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(EstadoViaje.equals("Cancelado")) return;


                    if(Pasajeros.getItemAtPosition(i).equals("Libre")){
                        return;

                    }
                    else{
                        if(Pasajeros.getItemAtPosition(i).equals("Acompañante")){
                           return;
                        }else{
                            String Email = "";
                            String Rol = "";
                            String[] parts = Pasajeros.getItemAtPosition(i).toString().split("-");
                            Email = EmailPasajeros.get(i).split("-")[0];
                            Rol = EmailPasajeros.get(i).split("-")[1];

                            Intent pagVerPasajero= new Intent(contexto,VerPasajero.class);
                            pagVerPasajero.putExtra("NroViaje",NroViaje);
                            pagVerPasajero.putExtra("EmailVerUsuario",Email);
                            pagVerPasajero.putExtra("RolVerUsuario",Rol);
                            pagVerPasajero.putExtra("EstadoViaje", EstadoViaje);
                            startActivity(pagVerPasajero);
                        }

                    }
             //
                //   Entraba igual por eso lo saque 
                //   if(!Pasajeros.getItemAtPosition(i).equals("Libre")||!Pasajeros.getItemAtPosition(i).equals("Acompañante")) {



                    /**/
               // }
            }
        });

        Solicitudes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String Email="";
                Email=IdSolicitudes.get(i);

                Intent pagResponderSoli= new Intent(contexto,ResponderSolicitud.class);
                pagResponderSoli.putExtra("NroViaje",NroViaje);
                pagResponderSoli.putExtra("Email",Email);
                startActivity(pagResponderSoli);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogFragmentView)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new CancelarViaje().execute();
                        finish();
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        confirmarCancelacion = builder.create();


        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setView(dialogFragmentView2)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new FinalizarViaje().execute();
                        finish();
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        confirmarFinalizacion = builder2.create();


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
                query += "          ci2.Nombre CiudadDestino,";
                query += "          vj.FechaHoraFinalizacion,";
                query += "          vj.CantidadPasajeros,";
                query += "          vj.EstadoViaje";
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
                    item.put("estado", resultados.getString("EstadoViaje"));
                    estadoViaje = resultados.getString("EstadoViaje");
                    itemsGrilla.add(item);
                    localDateviaje=resultados.getString("FechaHoraFinalizacion");
                    // CantidadAsientos=resultados.getString("CantidadPasajeros");
                }

                String[] from = {"NroViaje","origen", "destino", "fecha", "hora","estado"};
                int[] to = {R.id.tvGridItemViajeNroViaje,R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora, R.id.tvGridItemEstadoViaje};
                SimpleAdapter simpleAdapter = new SimpleAdapter(contexto, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaverViaje.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarPasajeros extends AsyncTask<Void,Integer,ResultSet> {
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
                query += " 		    usu.Email,";
                query += " 		    usu.Rol,";
                query += " 		    vj.CantidadPasajeros,";
                query += " 		    pv.cantAcompañantes";
                query += " FROM Viajes vj";
                query += " Inner join PasajerosPorViaje pv";
                query += " ON pv.ViajeId=vj.Id";
                query += " Inner join Usuarios usu";
                query += " ON usu.Email=pv.UsuarioEmail";
                query += " 	Where	pv.ViajeId='" + NroViaje + "'";
                query += " 	And	 pv.EstadoRegistro=1";
                query += " 	And	 usu.Rol='PAS'";
                query += " 	And	 pv.EstadoPasajero='Aceptado'";

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
                ArrayList<String> pasajeros= new ArrayList<String>();
                EmailPasajeros= new ArrayList<>();
                int cantidadDeAsientos=0;
                int PasajerosABordo=0;
                while (resultados.next()) {
                    PasajerosABordo++;
                    pasajeros.add(resultados.getString("Nombre")+" "+ resultados.getString("Apellido")+" - "+resultados.getString("Telefono"));
                    int cantidadAcompañantes=resultados.getInt("cantAcompañantes");
                    if(cantidadAcompañantes > 0){
                        for(int i=0; i < cantidadAcompañantes; i++){
                            PasajerosABordo++;
                            pasajeros.add("Acompañante de " + resultados.getString("Nombre") + " " + resultados.getString("Apellido"));
                        }
                    }

                    EmailPasajeros.add(resultados.getString("Email") + "-" + resultados.getString("Rol"));
                    cantidadDeAsientos=resultados.getInt("CantidadPasajeros");
                }

                ArrayList<String> asientosLibres = agregarAsientosLibres(cantidadDeAsientos, PasajerosABordo);
                if (asientosLibres.size() > 0) pasajeros.addAll(asientosLibres);

                ArrayAdapter<String>adapter= new ArrayAdapter<>(contexto,R.layout.list_item_viajes,pasajeros);
                Pasajeros.setAdapter(adapter);
                TituloPasajeros.setText("Pasajeros (" + PasajerosABordo + "/" + cantidadDeAsientos + ")");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> agregarAsientosLibres(int cantAsientos, int asientosOcupados) {
        int asientosLibres = cantAsientos - asientosOcupados;

        ArrayList<String> arrAsientosLibres = new ArrayList<String>();
        for (int i = 0; i < asientosLibres; i++) {
            arrAsientosLibres.add("Libre");
        }
        return arrAsientosLibres;
    }

    private class CargarSolicitudes extends AsyncTask<Void,Integer,ResultSet> {

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
                query += " 		    usu.Email";
                query += " FROM Usuarios usu";
                query += " Inner join PasajerosPorViaje pv";
                query += " ON usu.Email=pv.UsuarioEmail";
                query += " 	Where	pv.ViajeId='" + NroViaje + "'";
                query += " 	And	 pv.EstadoPasajero = 'Pendiente'";
                query += " 	And	 usu.Rol = 'PAS'";

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
                ArrayList<String> Solicitudess= new ArrayList<String>();
                IdSolicitudes=new ArrayList<>();
                while (resultados.next()) {
                    Solicitudess.add(resultados.getString("Nombre")+" "+ resultados.getString("Apellido")+" - "+resultados.getString("Telefono"));
                    IdSolicitudes.add(resultados.getString("Email"));
                }

                ArrayAdapter<String>adapter= new ArrayAdapter<>(contexto,R.layout.list_item_viajes,Solicitudess);
                Solicitudes.setAdapter(adapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void CancelarViaje(View view){
        confirmarCancelacion.show();
    }

    private class CancelarViaje extends AsyncTask<Void,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	Viajes vj";
                query += "  	    SET";
                query += " 		    EstadoViaje='Cancelado'";
                query += " 	Where	vj.Id='" + NroViaje + "'";

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

                for (String Email: EmailPasajeros) {
                    String[] parts = Email.split("-");
                    String emailpasajero = parts[0];
                    String RolPasajero = parts[1];
                    NotificacionesNegImpl NotiNeg = new NotificacionesNegImpl();
                    Notificaciones Noti = new Notificaciones();
                    Noti.setUsuarioEmail(emailpasajero);
                    Noti.setUsuarioRolId(RolPasajero);
                    Noti.setMensaje(" El nro de viaje "+NroViaje+" fue cancelado");
                    Noti.setEstadoNotificacion("P");
                    Noti.setEstado(1);
                    try {
                        NotiNeg.AñadirNotificacion(Noti);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

                Toast.makeText(contexto, "El  viaje a sido Cancelado!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo cancelar el  viaje, intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void FinalizarViaje(View view) throws ParseException {
        
        String fechaInicio = ((TextView)findViewById(R.id.tvGridItemViajeOrigenFecha)).getText().toString();
        String horaInicio = ((TextView)findViewById(R.id.tvGridItemViajeOrigenHora)).getText().toString();
        fechaInicio = fechaInicio.replace("/21", "/2021");
        LocalDateTime hoy= LocalDateTime.now();
        LocalDateTime inicioViaje = LocalDateTime.parse(fechaInicio + " " + horaInicio, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if (hoy.compareTo(inicioViaje) > 0) {
            confirmarFinalizacion.show();
        }
        else Toast.makeText(contexto, "El viaje no puede finalizarse antes de comenzar", Toast.LENGTH_SHORT).show();
    }

    public void onEditarViaje(View view) {
        String[] origen = ((TextView)findViewById(R.id.tvGridItemViajeOrigen)).getText().toString().split(",");
        String[] destino = ((TextView)findViewById(R.id.tvGridItemViajeDestino)).getText().toString().split(",");
        SharedPreferences sharedPreference = getSharedPreferences("DatosEdicion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString("fechaInicio", ((TextView)findViewById(R.id.tvGridItemViajeOrigenFecha)).getText().toString());
        editor.putString("horaInicio", ((TextView)findViewById(R.id.tvGridItemViajeOrigenHora)).getText().toString());
        editor.putString("ciudadOrigen", origen[0].trim());
        editor.putString("provinciaOrigen", origen[1].trim());
        editor.putString("ciudadDestino", destino[0].trim());
        editor.putString("provinciaDestino", destino[1].trim());
        editor.putString("idViaje", NroViaje);
        editor.putString("modoEdicion", "true");
        editor.commit();

        Intent pagNuevoViaje = new Intent(contexto, NuevoViaje.class);
        startActivity(pagNuevoViaje);
    }

    private class FinalizarViaje extends AsyncTask<Void,Integer,Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " UPDATE 	Viajes vj";
                query += "  	    SET";
                query += " 		    EstadoViaje='Finalizado'";
                query += " 	Where	vj.Id='" + NroViaje + "'";

                int resultado = st.executeUpdate(query);

                return resultado > 0;
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){
                Toast.makeText(contexto, "El viaje a sido Finalizado!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo finalizar el  viaje, intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onResume() {

        super.onResume();
        if(shouldExecuteOnResume){
            finish();
            Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
            pagVerViaje.putExtra("NroViaje",NroViaje);
            pagVerViaje.putExtra("EstadoViaje", estadoViaje);
            startActivity(pagVerViaje);
        } else{
            shouldExecuteOnResume = true;
        }



    }
}