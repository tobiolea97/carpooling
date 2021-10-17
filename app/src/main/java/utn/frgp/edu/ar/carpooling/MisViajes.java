package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
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

public class MisViajes extends AppCompatActivity {

    GridView grillaViajes;
    String emailUsuario, rolUsuario;
    Context contexto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_viajes);

        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        contexto = this;
        grillaViajes = (GridView) findViewById(R.id.gvMisViajes);
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        grillaViajes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String Texto="";
                Texto=adapterView.getItemAtPosition(position).toString();


                String[] parts = Texto.split("NroViaje=");
                String part2 = parts[1]; // 654321

                //Para obtener el id del viaje
                String[] partspt2 = part2.split(",");
                String part3 = partspt2[0]; // 123


                Toast.makeText(contexto, "asd2  "+part3, Toast.LENGTH_SHORT).show();
                
            }
        });
        new CargarProximosViajes().execute(generateQuery(new HashMap<String, String>()));
    }

    public void crearFiltrarClickListener (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(R.layout.fragment_filtrar_viajes_dialog)
                .setPositiveButton(R.string.Aplicar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HashMap<String, String> filtros = new HashMap<String, String>();
                        // filtros.put("provinciaOrigen", findViewById(R.id.spProvOrigen))
                        new CargarProximosViajes().execute(generateQuery(filtros));
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String generateQuery (HashMap<String, String> filtros) {
        String query = "";
        query += " SELECT 	vj.FechaHoraInicio,";
        query += "  	vj.Id,";
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
        query += rolUsuario.equals("PAS") ? " WHERE ppv.UsuarioEmail = '" + emailUsuario + "' AND" : " WHERE vj.ConductorEmail = '" + emailUsuario + "'";

        if (!filtros.isEmpty()) {
            if (filtros.get("provinciaOrigen") != null) {
                query += " AND pr1.Nombre = '" + filtros.get("provinciaOrigen") + "'";
            }
            if (filtros.get("provinciaDestino") != null) {
                query += " AND pr2.Nombre = '" + filtros.get("provinciaDestino") + "'";
            }
            if (filtros.get("ciudadOrigen") != null) {
                query += " AND ci1.Nombre = '" + filtros.get("ciudadOrigen") + "'";
            }
            if (filtros.get("ciudadDestino") != null) {
                query += " AND ci2.Nombre = '" + filtros.get("ciudadDestino") + "'";
            }
            if (filtros.get("estado") != null) {
                query += " AND vi.Estado = '" + filtros.get("estado") + "'";
            }
        }
        query += " ORDER BY FechaHoraInicio ASC";

        return query;
    }

    private class CargarProximosViajes extends AsyncTask<String,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(String... myQuery) {
            System.out.println("!!!!!! " + myQuery[0]);
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                return st.executeQuery(myQuery[0]);
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
                grillaViajes.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}