package utn.frgp.edu.ar.carpooling;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import static utn.frgp.edu.ar.carpooling.Home.NOTIFICACION_ID;

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
    ListView LvNotificacionLeidos;
    ListView LvNotificacionNoLeidos;
    Context contexto;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario, idUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        contexto = this;
        LvNotificacionLeidos=findViewById(R.id.LvNotificacionesLeidos);
        LvNotificacionNoLeidos=findViewById(R.id.LvNotificaciones);
        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        idUsuario = spSesion.getString("Id","No hay datos");

        String Rol="";
        if(rolUsuario.equals("CON")){
            Rol="Conductor";
        }else{
            Rol="Pasajero";
        }

        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+Rol);

        NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.cancel(NOTIFICACION_ID);



  ArrayList<String> itemListt= new ArrayList<String>();
        itemListt.add("Item1"+"-P");
        itemListt.add("Item2"+"-L");
        itemListt.add("Item33"+"-P");
        itemListt.add("Item333"+"-P");
String [] itemList= new String[]{
"Item1",
"Item2",
"Item3",

};


ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(contexto,R.layout.list_item_viajes,itemListt){

    @NonNull
    @Override
   public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view=super.getView(position, convertView, parent);
        for (String o : itemListt){
            String Texto="";
            Texto=itemListt.get(position);
            String[] parts = Texto.split("-");
            String part2 = parts[1];

            if(itemListt.get(position).contains("L")){
System.out.println("pasa una vez");
                view.setBackgroundColor(getResources().getColor(
                        android.R.color.holo_blue_dark
                ));
            }
            System.out.println("2 pasa una vez");
        }
      /* if(){


        }else{

            view.setBackgroundColor(getResources().getColor(
                    android.R.color.black
            ));
        }*/



return view;
    }
};


        LvNotificacionLeidos.setAdapter(arrayAdapter);

     //  new CargarNotificaciones().execute();
       // new CargarNotificacionesnoLeidos().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu miMenu) {
        SharedPreferences sp = getSharedPreferences("Sesion", Context.MODE_PRIVATE);

        if(sp.getString("Rol","No hay datos").equals("CON")) {
            getMenuInflater().inflate(R.menu.menu_conductor, miMenu);
        }

        if(sp.getString("Rol","No hay datos").equals("PAS")) {
            getMenuInflater().inflate(R.menu.menu_pasajero, miMenu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem opcionMenu) {
        int id = opcionMenu.getItemId();

        SharedPreferences sp = getSharedPreferences("Sesion", Context.MODE_PRIVATE);

        if(sp.getString("Rol","No hay datos").equals("CON")) {

            if (id == R.id.misViajes) {
                Intent intent = new Intent(this, MisViajes.class);
                startActivity(intent);
            }

            if (id == R.id.crearViaje) {
                Intent intent = new Intent(this, NuevoViaje.class);
                startActivity(intent);
            }

        }

        if(sp.getString("Rol","No hay datos").equals("PAS")) {
            if (id == R.id.misSolicitudes) {
                Intent intent = new Intent(this, MisViajesModoPasajero.class);
                startActivity(intent);
            }

            if (id == R.id.crearSolicitud) {
                Intent intent = new Intent(this, NuevaSolicitud.class);
                startActivity(intent);
            }
        }

        if (id == R.id.miperfil) {
            finish();
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }

        if (id == R.id.notificaciones) {
            Intent intent = new Intent(this, utn.frgp.edu.ar.carpooling.Notificaciones.class);
            startActivity(intent);
        }

        if (id == R.id.editarPerfil) {
            Intent intent = new Intent(this, EditarPerfil.class);
            startActivity(intent);
        }

        if (id == R.id.cerrarSesion) {

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

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem currentOption = menu.findItem(R.id.notificaciones);
        currentOption.setVisible(false);

        return true;
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
                query += " 	Where	noti.EstadoNotificacion='L' and noti.UsuarioId=" + idUsuario;

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
                LvNotificacionLeidos.setAdapter(adapter);



            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class CargarNotificacionesnoLeidos extends AsyncTask<Void,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT 	noti.Mensaje";
                query += " FROM Notificaciones noti";
                query += " 	Where	noti.EstadoNotificacion='P' and noti.UsuarioId=" + idUsuario;

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
                LvNotificacionNoLeidos.setAdapter(adapter);

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
                query += " 	Where	noti.UsuarioId=" + idUsuario;


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