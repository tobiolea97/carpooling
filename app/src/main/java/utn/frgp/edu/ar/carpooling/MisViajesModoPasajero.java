package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

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

public class MisViajesModoPasajero extends AppCompatActivity {
    Context contexto;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario, idUsuario;
    GridView GrMisViajesModoPasajero;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes_modo_pasajero);
        contexto = this;
        GrMisViajesModoPasajero=findViewById(R.id.GrMisViajesModoPasajero);

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
        GrMisViajesModoPasajero.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                String Texto="";
                Texto=adapterView.getItemAtPosition(position).toString();

                String[] parts = Texto.split("NroViaje=");
                String part2 = parts[1];

                //Para obtener el id del viaje
                String[] partspt2 = part2.split(",");
                String part3 = partspt2[0]; // 123

                String estadoViaje = Texto.split("estado=")[1].split(",")[0];
                if(estadoViaje.equals("Aceptado")||estadoViaje.equals("Pendiente")) {
                    Intent PagCancelarViaje = new Intent(contexto, CancelarViajePasajero.class);
                    PagCancelarViaje.putExtra("NroViaje", part3);
                    PagCancelarViaje.putExtra("EstadoViaje", estadoViaje);
                    startActivity(PagCancelarViaje);
                }

                //Para viaje finalizado
               /* Intent pagVerViajeFinalizado= new Intent(contexto,VerVIajeFinalizado.class);
                pagVerViajeFinalizado.putExtra("NroViaje",part3);
                startActivity(pagVerViajeFinalizado);
                finish();*/
            }
        });
        new CargarViajes().execute();
    }
    private class CargarViajes extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();
                String query = "";
                query += " SELECT   ";
                query += "   vj.FechaHoraInicio,   ";
                query += "   vj.Id,   ";
                query += "   pr1.Nombre ProvinciaOrigen,   ";
                query += "   ci1.Nombre CiudadOrigen,   ";
                query += "   pr2.Nombre ProvinciaDestino,   ";
                query += "   ci2.Nombre CiudadDestino,   ";
                query += "   vj.CantidadAcompaniantes,   ";
                query += "   vj.EstadoSolicitud,  ";
                query += "   vj.PasajeroId   ";
                query += " FROM   ";
                query += "   Solicitudes vj   ";
                query += "   LEFT JOIN Provincias pr1 ON pr1.Id = vj.ProvinciaOrigenId   ";
                query += "   LEFT JOIN Provincias pr2 ON pr2.Id = vj.ProvinciaDestinoId   ";
                query += "   LEFT JOIN Ciudades ci1 ON ci1.Id = vj.CiudadOrigenId   ";
                query += "   LEFT JOIN Ciudades ci2 ON ci2.Id = vj.CiudadDestinoId  ";
                query += " Where   ";
                query += "   vj.PasajeroId = '" + idUsuario + "';  ";

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
                    item.put("estado", resultados.getString("EstadoSolicitud"));
                    itemsGrilla.add(item);
                }

                String[] from = {"NroViaje","origen", "destino", "fecha", "hora","estado"};
                int[] to = {R.id.tvGridItemViajeNroViaje,R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora, R.id.tvGridItemEstadoViaje};
                SimpleAdapter simpleAdapter = new SimpleAdapter(contexto, itemsGrilla, R.layout.grid_item_viaje, from, to);
                GrMisViajesModoPasajero.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}