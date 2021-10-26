package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
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

public class Ver_Viajes extends AppCompatActivity {
    Context contexto;
    GridView grillaverViaje;
    String NroViaje;
    String EstadoViaje;
    TextView TituloPasajeros;
    ListView Pasajeros,Solicitudes;

    ArrayList<String> EmailPasajeros;
    ArrayList<String> IdSolicitudes;

    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_viajes);
        contexto = this;

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

        new CargarViajeSeleccionado().execute();
        new CargarPasajeros().execute();
        new CargarSolicitudes().execute();

        Pasajeros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!Pasajeros.getItemAtPosition(i).equals("Libre")) {
                    String Email = "";
                    String[] parts = Pasajeros.getItemAtPosition(i).toString().split("-");
                    Email=EmailPasajeros.get(i);

                    Intent pagVerPasajero= new Intent(contexto,VerPasajero.class);
                    pagVerPasajero.putExtra("NroViaje",NroViaje);
                    pagVerPasajero.putExtra("Email",Email);

                    startActivity(pagVerPasajero);
                }
            }
        });



        Solicitudes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String Email="";

                Email=IdSolicitudes.get(i);

                System.out.println(Email+" solicitudes");
            }
        });



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
                query += " 		    vj.CantidadPasajeros";
                query += " FROM Viajes vj";
                query += " Inner join PasajerosPorViaje pv";
                query += " ON pv.ViajeId=vj.Id";
                query += " Inner join Usuarios usu";
                query += " ON usu.Email=pv.UsuarioEmail";
                query += " 	Where	pv.ViajeId='" + NroViaje + "'";
                query += " 	And	 pv.EstadoRegistro=1";
                query += " 	And	 usu.Rol='PAS'";

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
                int PasajerosABordo=0;
                String CantidadAsientos="";
                while (resultados.next()) {
                    PasajerosABordo++;
                  pasajeros.add(resultados.getString("Nombre")+" "+ resultados.getString("Apellido")+"-"+resultados.getString("Telefono"));
                    EmailPasajeros.add(resultados.getString("Email"));
                    CantidadAsientos=resultados.getString("CantidadPasajeros");
                }

                int cantidadasientos=Integer.parseInt(CantidadAsientos);

                switch(cantidadasientos){

                    case 4:
                        if(PasajerosABordo==0){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");

                        }
                        if(PasajerosABordo==1){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");

                        }
                        if(PasajerosABordo==2){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                        }
                        if(PasajerosABordo==3){
                            pasajeros.add("Libre");
                        }

                        break;
                    case 3:
                        if(PasajerosABordo==0){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                        }
                        if(PasajerosABordo==1){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                        }
                        if(PasajerosABordo==2){
                            pasajeros.add("Libre");
                        }

                    break;
                    case 2:
                        if(PasajerosABordo==0){
                            pasajeros.add("Libre");
                            pasajeros.add("Libre");
                        }
                        if(PasajerosABordo==1){
                            pasajeros.add("Libre");
                        }

                    break;
                    case 1:
                        if(PasajerosABordo==0){
                            pasajeros.add("Libre");
                        }
                     break;

                }

                ArrayAdapter<String>adapter= new ArrayAdapter<>(contexto,R.layout.list_item_viajes,pasajeros);
               Pasajeros.setAdapter(adapter);
               if(PasajerosABordo==0){
                   TituloPasajeros.setText("Pasajeros");
               }else {
                   TituloPasajeros.setText("Pasajeros" + PasajerosABordo + "/" + CantidadAsientos);
               }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                query += " FROM Solicitudes sol";
                query += " Inner join Usuarios usu";
                query += " ON usu.Email=sol.PasajeroEmail";
                query += " 	Where	sol.IdViaje='" + NroViaje + "'";
                query += " 	And	 sol.EstadoRegistro=1";

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
                    Solicitudess.add(resultados.getString("Nombre")+" "+ resultados.getString("Apellido")+"-"+resultados.getString("Telefono"));
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



//Botones imagen Buttons

    public void CancelarViaje(View view){

        new CancelarViaje().execute();

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

        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){
                Toast.makeText(contexto, "El  viaje a sido Cancelado!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo cancelar el  viaje, intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void FinalizarViaje(View view){

        new FinalizarViaje().execute();

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
                Toast.makeText(contexto, "El  viaje a sido Finalizado!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo finalizar el  viaje, intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}