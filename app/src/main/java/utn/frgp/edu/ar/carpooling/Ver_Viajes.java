package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
    ListView Pasajeros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_viajes);
        contexto = this;
        NroViaje=getIntent().getStringExtra("NroViaje");
        EstadoViaje=getIntent().getStringExtra("EstadoViaje");
        grillaverViaje= (GridView) findViewById(R.id.GrVerviaje);
        Pasajeros=findViewById(R.id.LVPasajeros);
        TituloPasajeros=findViewById(R.id.textView10);

        new CargarViajeSeleccionado().execute();
        new CargarPasajeros().execute();

        Pasajeros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!Pasajeros.getItemAtPosition(i).equals("Libre")) {
                    String Numero = "";
                    String[] parts = Pasajeros.getItemAtPosition(i).toString().split("-");
                    String part2 = parts[1];
                    Numero = part2;
                    Intent pagVerPasajero= new Intent(contexto,VerPasajero.class);
                    pagVerPasajero.putExtra("NroViaje",NroViaje);
                    pagVerPasajero.putExtra("NumeroTelefono",Numero);

                    startActivity(pagVerPasajero);
                    finish();
                }
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
                int PasajerosABordo=0;
                String CantidadAsientos="";
                while (resultados.next()) {
                    PasajerosABordo++;
                  pasajeros.add(resultados.getString("Nombre")+" "+ resultados.getString("Apellido")+"-"+resultados.getString("Telefono"));
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

}