package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.Toast;

import java.sql.*;
import java.util.*;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;

public class MisViajes extends AppCompatActivity {
    AlertDialog filtroDialog;
    View dialogFragmentView;
    Spinner spFiltroProvOrigen;
    Spinner spFiltroProvDestino;
    Spinner spFiltroCiudOrigen;
    Spinner spFiltroCiudDestino;
    Spinner spFiltroEstado;
    GridView grillaViajes;
    String emailUsuario, rolUsuario;
    Context contexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("");
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

                //Toast.makeText(contexto, "asd2  "+part3, Toast.LENGTH_SHORT).show();



               Intent pagVerViaje= new Intent(contexto,Ver_Viajes.class);
                pagVerViaje.putExtra("NroViaje",part3);
                startActivity(pagVerViaje);
                finish();


                //Para viaje finalizado
               /* Intent pagVerViajeFinalizado= new Intent(contexto,VerVIajeFinalizado.class);
                pagVerViajeFinalizado.putExtra("NroViaje",part3);
                startActivity(pagVerViajeFinalizado);
                finish();*/
            }
        });
        LayoutInflater inflater = this.getLayoutInflater();
        dialogFragmentView = inflater.inflate(R.layout.fragment_filtrar_viajes_dialog, null);
        spFiltroProvOrigen = dialogFragmentView.findViewById(R.id.spFiltroProvOrigen);
        spFiltroProvDestino = dialogFragmentView.findViewById(R.id.spFiltroProvDestino);
        spFiltroCiudOrigen = dialogFragmentView.findViewById(R.id.spFiltroCiudOrigen);
        spFiltroCiudDestino = dialogFragmentView.findViewById(R.id.spFiltroCiudDestino);
        spFiltroEstado = dialogFragmentView.findViewById(R.id.spFiltroEstado);

        crearFiltroDialog(); // CREO EL DIALOG PERO NO LO ABRO. ASI NO CREAMOS UN DIALOG DE CERO CADA VEZ QUE ABRIMOS EL FILTRO
        cargarSpinnerEstado();

        new CargarViajesFiltrados().execute(generateQuery(new HashMap<String, String>()));
        new CargarFiltroProvinciaSpinners().execute();
        new CargarFiltroCiudadSpinners().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu miMenu) {
        getMenuInflater().inflate(R.menu.menu_conductor, miMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem opcionMenu) {
        int id = opcionMenu.getItemId();

        if(id == R.id.inicio) {
            finish();
            Intent intent = new Intent(this, Home.class);
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

    public void ClickAgregarNuevoViaje(View view){
        Intent i = new Intent(this,NuevoViaje.class);
        startActivity(i);
    }

    public void crearFiltrarClickListener (View view) {
        filtroDialog.show();
    }

    private void crearFiltroDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogFragmentView)
                .setPositiveButton(R.string.Aplicar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HashMap<String, String> filtros = new HashMap<String, String>();
                        filtros.put("provinciaOrigen", spFiltroProvOrigen.getSelectedItem().toString());
                        filtros.put("provinciaDestino", spFiltroProvDestino.getSelectedItem().toString());
                        filtros.put("ciudadOrigen", spFiltroCiudOrigen.getSelectedItem().toString());
                        filtros.put("ciudadDestino", spFiltroCiudDestino.getSelectedItem().toString());
                        filtros.put("estado", spFiltroEstado.getSelectedItem().toString());
                        new CargarViajesFiltrados().execute(generateQuery(filtros));
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        filtroDialog = builder.create();
    }

    private void cargarSpinnerEstado () {
        Spinner spFiltroEstado = dialogFragmentView.findViewById(R.id.spFiltroEstado);
        String[] datos = new String[] {"--NINGUNO--", "1", "En Espera"};
        spFiltroEstado.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datos));
    }

    private class CargarFiltroCiudadSpinners extends AsyncTask<String,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(String... strings) {
            return ejecutarQuery("SELECT Nombre FROM Ciudades");
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                List<String> ciudades = new ArrayList<String>();
                ciudades.add("--NINGUNA--");

                while (resultados.next()) { ciudades.add(resultados.getString("Nombre")); }

                ArrayAdapter<String> adapterCiudades = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, ciudades);
                adapterCiudades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFiltroCiudOrigen.setAdapter(adapterCiudades);
                spFiltroCiudDestino.setAdapter(adapterCiudades);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarFiltroProvinciaSpinners extends AsyncTask<String,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(String... strings) {
            return ejecutarQuery("SELECT Nombre FROM Provincias");
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                List<String> provincias = new ArrayList<String>();
                provincias.add("--NINGUNA--");

                while (resultados.next()) { provincias.add(resultados.getString("Nombre")); }

                ArrayAdapter<String> adapterProvincias = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, provincias);
                adapterProvincias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFiltroProvOrigen.setAdapter(adapterProvincias);
                spFiltroProvDestino.setAdapter(adapterProvincias);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarViajesFiltrados extends AsyncTask<String,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(String... queries) {
            return ejecutarQuery(queries[0]);
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
            if (!filtros.get("provinciaOrigen").equals("--NINGUNA--")) {
                query += " AND pr1.Nombre = '" + filtros.get("provinciaOrigen") + "'";
            }
            if (!filtros.get("provinciaDestino").equals("--NINGUNA--")) {
                query += " AND pr2.Nombre = '" + filtros.get("provinciaDestino") + "'";
            }
            if (!filtros.get("ciudadOrigen").equals("--NINGUNA--")) {
                query += " AND ci1.Nombre = '" + filtros.get("ciudadOrigen") + "'";
            }
            if (!filtros.get("ciudadDestino").equals("--NINGUNA--")) {
                query += " AND ci2.Nombre = '" + filtros.get("ciudadDestino") + "'";
            }
            if (!filtros.get("estado").equals("--NINGUNO--")) {
                query += " AND vi.Estado = '" + filtros.get("estado") + "'";
            }
        }
        query += " ORDER BY FechaHoraInicio ASC";

        return query;
    }

    private ResultSet ejecutarQuery (String query) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
            Statement st = con.createStatement();

            return st.executeQuery(query);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}