package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

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
import utn.frgp.edu.ar.carpooling.entities.Ciudad;
import utn.frgp.edu.ar.carpooling.entities.Provincia;

public class Buscar extends AppCompatActivity {

    AlertDialog filtroDialog, filtroDialog2;
    View dialogFragmentView, dialogFragmentView1;
    String nombreUsuario, apellidoUsuario, emailUsuario, rolUsuario;
    GridView grillaViajes;
    Context context;
    TextView filtroRecorrido, filtroRecorridoDestino, filtroFecha, filtroFechaQuery;
    Spinner spFiltroProvinciaOrigen, spFiltroCiudadesOrigen, spFiltroProvinciaDestino, spFiltroCiudadesDestino;
    ArrayList<String> listaCiudadesOrigen;
    List<Ciudad> itemsCiudadesOrigen;
    Provincia provOrigSelecc;
    List<Provincia> itemsProvincias;
    Chip chOrigen,chDestino, chFecha;
    ArrayAdapter<String> adapterProvincias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Busqueda");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar);

        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nombreUsuario = spSesion.getString("Nombre","No hay datos");
        apellidoUsuario = spSesion.getString("Apellido","No hay datos");
        emailUsuario = spSesion.getString("Email","No hay datos");
        rolUsuario = spSesion.getString("Rol","No hay datos");
        getSupportActionBar().setTitle(nombreUsuario+" "+ apellidoUsuario+" Rol: "+rolUsuario);
        grillaViajes = (GridView) findViewById(R.id.gvHomeProximosVIajes);
        context = this;

        LayoutInflater inflater = this.getLayoutInflater();
        dialogFragmentView = inflater.inflate(R.layout.fragment_filtro_provincia_ciudad, null);
        spFiltroProvinciaOrigen = dialogFragmentView.findViewById(R.id.spFragmentFiltroProvinciaCiudadProvincias);
        spFiltroCiudadesOrigen = dialogFragmentView.findViewById(R.id.spFragmentFiltroProvinciaCiudadCiudades);
        spFiltroProvinciaDestino = dialogFragmentView.findViewById(R.id.spFragmentFiltroProvinciaCiudadProvincias3);
        spFiltroCiudadesDestino = dialogFragmentView.findViewById(R.id.spFragmentFiltroProvinciaCiudadProvincias4);

        filtroRecorrido = (TextView) findViewById(R.id.textView19);
        filtroRecorridoDestino = (TextView) findViewById(R.id.textView20);
        filtroFecha = (TextView) findViewById(R.id.textView25);
        filtroFechaQuery = (TextView) findViewById(R.id.textView22);
        filtroFechaQuery.setText(" ");


        spFiltroProvinciaOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //POR MEDIO DE LA POS DEL ITEM SELECCIONADO EN EL SPINNER, OBTENGO EL OBJETO CARGADO DE MI LISTA DE OBJETOS EN LA MISMA POS

                if(position == 0) return;

                provOrigSelecc = itemsProvincias.get(position - 1);
                //CARGO EL SPINNER CON LOS DATOS DE LAS CIUDADES PERTENECIENTES A LA PROV SELECCIONADA.
                new CargarSpinnersCiudadesOrigen().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFiltroProvinciaDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //POR MEDIO DE LA POS DEL ITEM SELECCIONADO EN EL SPINNER, OBTENGO EL OBJETO CARGADO DE MI LISTA DE OBJETOS EN LA MISMA POS

                if(position == 0) return;

                provOrigSelecc = itemsProvincias.get(position - 1);
                //CARGO EL SPINNER CON LOS DATOS DE LAS CIUDADES PERTENECIENTES A LA PROV SELECCIONADA.
                new CargarSpinnersCiudadesDestino().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        crearFiltroDialog();

        new CargarProximosViajes().execute();
        new CargarFiltroProvinciaSpinners().execute();
    }

    private class CargarProximosViajes extends AsyncTask<Void,Integer, ResultSet> {

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
                query += rolUsuario.equals("PAS") ? " INNER JOIN PasajerosPorViaje ppv ON ppv.ViajeId = vj.Id" : "";
                query += " LEFT JOIN Provincias pr1";
                query += " 	ON pr1.Id = vj.ProvinciaOrigenId";
                query += " LEFT JOIN Provincias pr2";
                query += " 	ON pr2.Id = vj.ProvinciaDestinoId";
                query += " LEFT JOIN Ciudades ci1";
                query += " 	ON ci1.Id = vj.CiudadOrigenId";
                query += " LEFT JOIN Ciudades ci2";
                query += " 	ON ci2.Id = vj.CiudadDestinoId";
                query += rolUsuario.equals("PAS") ? " WHERE ppv.UsuarioEmail = '" + emailUsuario + "' AND" : "";
                query += rolUsuario.equals("CON") ? " WHERE 	vj.ConductorEmail = '" + emailUsuario + "' AND" : "";
                query += " 		vj.EstadoViaje IN ('1','En Espera')";
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
                SimpleAdapter simpleAdapter = new SimpleAdapter(context, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaViajes.setAdapter(simpleAdapter);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onClickFecha(View view) {

        DatePickerViajeFragment newFragment = DatePickerViajeFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                String selectedDate =
                        (day < 10 ? "0" + day :  day) + "/" +
                                (month + 1 < 10 ? "0" + (month+1) : (month+1)) +"/" +
                                year;
                filtroFecha.setText("Despues del " + selectedDate);
                filtroFechaQuery.setText(year + "-" + (month + 1) + "-" + day);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    public void onClickBuscar(View view) {

        String filtro = "";
        boolean flag = false;

        if(!spFiltroProvinciaOrigen.getSelectedItem().equals(" ")) {
            filtro += flag ? " AND " : " WHERE ";
            filtro += " po.Nombre = '" + spFiltroProvinciaOrigen.getSelectedItem().toString() + "' ";
            flag = true;
        }

        if(spFiltroCiudadesOrigen.getAdapter() != null && !spFiltroCiudadesOrigen.getSelectedItem().equals(" ")) {
            filtro += flag ? " AND " : " WHERE ";
            filtro += " co.Nombre = '" + spFiltroCiudadesOrigen.getSelectedItem().toString() + "' ";
            flag = true;
        }

        if(spFiltroProvinciaDestino.getAdapter() != null && !spFiltroProvinciaDestino.getSelectedItem().equals(" ")) {
            filtro += flag ? " AND " : " WHERE ";
            filtro += " pd.Nombre = '" + spFiltroProvinciaDestino.getSelectedItem().toString() + "' ";
            flag = true;
        }

        if(spFiltroCiudadesDestino.getAdapter() != null && !spFiltroCiudadesDestino.getSelectedItem().equals(" ")) {
            filtro += flag ? " AND " : " WHERE ";
            filtro += " cd.Nombre = '" + spFiltroCiudadesDestino.getSelectedItem().toString() + "' ";
            flag = true;
        }

        if(!filtroFechaQuery.equals(" ")) {
            filtro += flag ? " AND " : " WHERE ";
            filtro += " vj.FechaHoraInicio > '" + filtroFechaQuery.getText() + "' ";
        }

        CargarViajesFiltrados task = new CargarViajesFiltrados(filtro);
        task.execute();


    }


    // Dialogo de busuqeda de pronvicia
    public void onClickFiltrarOrigen (View view) {
        filtroDialog.show();
    }

    private void crearFiltroDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogFragmentView)
                .setPositiveButton(R.string.Aplicar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(context,"Texto: " + (spFiltroProvinciaOrigen.getSelectedItem() == null ? "null" : "not null"),Toast.LENGTH_LONG);

                        Object item = spFiltroProvinciaOrigen.getSelectedItem();

                        if(!spFiltroProvinciaOrigen.getSelectedItem().equals(" "))
                            filtroRecorrido.setText("Desde " + spFiltroCiudadesOrigen.getSelectedItem().toString() + ", " + spFiltroProvinciaOrigen.getSelectedItem().toString());
                        else filtroRecorrido.setText(" ");
                        if(!spFiltroProvinciaDestino.getSelectedItem().equals(" "))
                            filtroRecorridoDestino.setText("Hacia " + spFiltroCiudadesDestino.getSelectedItem().toString() + ", " + spFiltroProvinciaDestino.getSelectedItem().toString());
                        else filtroRecorridoDestino.setText(" ");
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        filtroDialog = builder.create();

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setView(dialogFragmentView)
                .setPositiveButton(R.string.Aplicar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chDestino.setText("Hasta " + spFiltroCiudadesDestino.getSelectedItem() + ", " + spFiltroProvinciaDestino.getSelectedItem().toString());
                    }
                })
                .setNegativeButton(R.string.Cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        filtroDialog2 = builder2.create();
    }

    private class CargarFiltroProvinciaSpinners extends AsyncTask<String,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(String... strings) {
            return ejecutarQuery("SELECT * FROM Provincias");
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                List<String> provincias = new ArrayList<String>();
                itemsProvincias = new ArrayList<Provincia>();
                provincias.add(" ");

                while (resultados.next()) {
                    provincias.add(resultados.getString("Nombre"));
                    itemsProvincias.add(new Provincia(resultados.getInt("Id"),resultados.getString("Nombre"),resultados.getBoolean("EstadoRegistro")));
                }

                adapterProvincias = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, provincias);
                adapterProvincias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFiltroProvinciaOrigen.setAdapter(adapterProvincias);
                spFiltroProvinciaDestino.setAdapter(adapterProvincias);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private class CargarSpinnersCiudadesOrigen extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";

                //SI EL OBJETO DE PROV SELECCIONADA FUE CARGADO, SIGNIFICA QUE FUE SELECCIONADO DEL SPINNER.
                if(provOrigSelecc != null){
                    query = "SELECT * FROM Ciudades WHERE ProvinciaId=" + provOrigSelecc.getIdProvincia();
                }
                else{
                    query = "SELECT * FROM Ciudades";
                }


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
                listaCiudadesOrigen = new ArrayList<String>();
                itemsCiudadesOrigen = new ArrayList<Ciudad>();

                while (resultados.next()) {
                    Ciudad ciudad = new Ciudad(resultados.getInt("Id"),resultados.getInt("ProvinciaId"),resultados.getString("Nombre"),resultados.getBoolean("EstadoRegistro"));
                    listaCiudadesOrigen.add(ciudad.getNombre());
                    //CARGO LA LISTA GLOBAL PARA DESPUES PODES BUSCAR EL ELEMENTO SELECCIONADO
                    itemsCiudadesOrigen.add(ciudad);

                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listaCiudadesOrigen);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFiltroCiudadesOrigen.setAdapter(adapter);

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarSpinnersCiudadesDestino extends AsyncTask<Void,Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";

                //SI EL OBJETO DE PROV SELECCIONADA FUE CARGADO, SIGNIFICA QUE FUE SELECCIONADO DEL SPINNER.
                if(provOrigSelecc != null){
                    query = "SELECT * FROM Ciudades WHERE ProvinciaId=" + provOrigSelecc.getIdProvincia();
                }
                else{
                    query = "SELECT * FROM Ciudades";
                }


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
                listaCiudadesOrigen = new ArrayList<String>();
                itemsCiudadesOrigen = new ArrayList<Ciudad>();

                while (resultados.next()) {
                    Ciudad ciudad = new Ciudad(resultados.getInt("Id"),resultados.getInt("ProvinciaId"),resultados.getString("Nombre"),resultados.getBoolean("EstadoRegistro"));
                    listaCiudadesOrigen.add(ciudad.getNombre());
                    //CARGO LA LISTA GLOBAL PARA DESPUES PODES BUSCAR EL ELEMENTO SELECCIONADO
                    itemsCiudadesOrigen.add(ciudad);

                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listaCiudadesOrigen);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFiltroCiudadesDestino.setAdapter(adapter);

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarViajesFiltrados extends AsyncTask<String,Integer, ResultSet> {

        String filtro;

        public CargarViajesFiltrados(String _filtro) {
            this.filtro = _filtro;
        }

        @Override
        protected ResultSet doInBackground(String... queries) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += " SELECT vj.Id, po.Nombre ProvinciaOrigen, co.Nombre CiudadOrigen, pd.Nombre ProvinciaDestino, cd.Nombre CiudadDestino, vj.FechaHoraInicio, vj.EstadoViaje ";
                query += " FROM Viajes vj ";
                query += " INNER JOIN Provincias po ";
                query += " 	ON vj.ProvinciaOrigenId = po.Id ";
                query += " INNER JOIN Ciudades co ";
                query += " 	ON vj.CiudadOrigenId = co.Id ";
                query += " INNER JOIN Provincias pd ";
                query += " 	ON vj.ProvinciaDestinoId = pd.Id ";
                query += " INNER JOIN Ciudades cd ";
                query += " 	ON vj.CiudadDestinoId = cd.Id ";
                query += filtro;

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
                    item.put("estado",resultados.getString("EstadoViaje"));
                    itemsGrilla.add(item);
                }

                String[] from = {"NroViaje","origen", "destino", "fecha", "hora","estado"};
                int[] to = {R.id.tvGridItemViajeNroViaje,R.id.tvGridItemViajeOrigen, R.id.tvGridItemViajeDestino, R.id.tvGridItemViajeOrigenFecha, R.id.tvGridItemViajeOrigenHora,R.id.tvGridItemEstadoViaje};
                SimpleAdapter simpleAdapter = new SimpleAdapter(context, itemsGrilla, R.layout.grid_item_viaje, from, to);
                grillaViajes.setAdapter(simpleAdapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}