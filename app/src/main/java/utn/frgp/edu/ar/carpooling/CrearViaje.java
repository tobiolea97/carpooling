package utn.frgp.edu.ar.carpooling;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.entities.Ciudad;
import utn.frgp.edu.ar.carpooling.entities.Provincia;
import utn.frgp.edu.ar.carpooling.entities.Viaje;

public class CrearViaje extends AppCompatActivity {

    private EditText fechaViaje;
    private EditText  horaViaje;
    private Spinner spProvinciasOrigen;
    private Spinner spProvinciasDestino;
    private Spinner spCiudadesOrigen;
    private Spinner spCiudadesDestino;
    private Spinner spCantPasajeros;

    //LOS ARRAYS LIST SON PARA MOSTRAR LOS DATOS EN EL SPINNER
    //LOS LIST SON PARA PODER BUSCAR EL OBJETO CORRESPONDIENTE AL ITEM SELECCIONADO EN EL SPINNER
    List<Ciudad> itemsCiudadesOrigen;
    ArrayList<String> listaCiudadesOrigen;
    List<Ciudad> itemsCiudadesDestino;
    ArrayList<String> listaCiudadesDestino;

    List<Provincia> itemsProvincias;
    ArrayList<String> listaProvincias;

    //SON PARA PODER OBTENER EL ID DE LA PROVINCIA SELECCINADA Y BUSCAR SUS CIUDADES
    Provincia provOrigSelecc;
    Provincia provDestSelecc;

    //ES PARA PODER DAR DE ALTA EL NUEVO VIAJE
    Viaje nuevoViaje;

    private Context contexto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_viaje);

        fechaViaje = (EditText) findViewById(R.id.edTextFecha);
        horaViaje = (EditText) findViewById(R.id.edTextHora);

        spCantPasajeros = (Spinner) findViewById(R.id.spCantPasajeros);

        spProvinciasOrigen = (Spinner) findViewById(R.id.spProvOrigen);
        spCiudadesOrigen = (Spinner) findViewById(R.id.spCiudadOrigen);

        spProvinciasDestino = (Spinner) findViewById(R.id.spProvDestino);
        spCiudadesDestino= (Spinner) findViewById(R.id.spCiudadDestino);

        contexto = this;

        provDestSelecc = null;
        provOrigSelecc = null;
        nuevoViaje = null;

        ArrayList<String> listaCantPasajeros = new ArrayList<String>();

        for(int i = 1; i<=4; i++){
            listaCantPasajeros.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaCantPasajeros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCantPasajeros.setAdapter(adapter);

        fechaViaje.requestFocus();
        new CargarSpinnersProvincias().execute();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickCrearViaje(View view){

        nuevoViaje = new Viaje();


        SharedPreferences spSesion = getSharedPreferences("Sesion", Context.MODE_PRIVATE);
        nuevoViaje.setEmailConductor( spSesion.getString("Email","No hay datos"));
        nuevoViaje.setProvOrigen(itemsProvincias.get(spProvinciasOrigen.getSelectedItemPosition()));
        nuevoViaje.setCiudadOrigen(itemsCiudadesOrigen.get(spCiudadesOrigen.getSelectedItemPosition()));
        nuevoViaje.setProvDestino(itemsProvincias.get(spProvinciasDestino.getSelectedItemPosition()));
        nuevoViaje.setCiudadDestino(itemsCiudadesDestino.get(spCiudadesDestino.getSelectedItemPosition()));
        nuevoViaje.setCantPasajeros(Integer.parseInt(spCantPasajeros.getSelectedItem().toString()));
        nuevoViaje.setEstadoViaje("En Espera");

        String separadorFecha = Pattern.quote("/");
        String separadorHora = Pattern.quote(":");

        int dia = Integer.parseInt(fechaViaje.getText().toString().split(separadorFecha)[0]);
        int mes = Integer.parseInt(fechaViaje.getText().toString().split(separadorFecha)[1]);
        int anio = Integer.parseInt(fechaViaje.getText().toString().split(separadorFecha)[2]);
        int hora = Integer.parseInt(horaViaje.getText().toString().split(separadorHora)[0]);
        int minuto = Integer.parseInt(horaViaje.getText().toString().split(separadorHora)[1]);

        //ESTA COMENTADO PORQUE AMI NO ME FUNCIONA, NO OLVIDAR ACTIVARLO NUEVAMENTE!!!!  JONNA.
        //nuevoViaje.setFechaHoraInicio(LocalDateTime.of(anio,mes,dia,hora,minuto));

        new AltaNuevoViaje().execute();


    }

    public void onClickFechaViaje(View view) {

        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                String selectedDate =
                        (day < 10 ? "0" + day :  day) + "/" +
                                (month + 1 < 10 ? "0" + (month+1) : (month+1)) +"/" +
                                year;
                fechaViaje.setText(selectedDate);
                fechaViaje.setError(null);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    private class CargarSpinnersCiudadesOrigen extends AsyncTask<Void,Integer, ResultSet>  {

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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaCiudadesOrigen);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCiudadesOrigen.setAdapter(adapter);

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarSpinnersCiudadesDestino extends AsyncTask<Void,Integer, ResultSet>  {

        @Override
        protected ResultSet doInBackground(Void... voids) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";

                //SI EL OBJETO DE PROV SELECCIONADA FUE CARGADO, SIGNIFICA QUE FUE SELECCIONADO DEL SPINNER.
                if(provDestSelecc != null){
                    query = "SELECT * FROM Ciudades WHERE ProvinciaId=" + provDestSelecc.getIdProvincia();
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
                listaCiudadesDestino = new ArrayList<String>();
                itemsCiudadesDestino = new ArrayList<Ciudad>();

                while (resultados.next()) {
                    Ciudad ciudad = new Ciudad(resultados.getInt("Id"),resultados.getInt("ProvinciaId"),resultados.getString("Nombre"),resultados.getBoolean("EstadoRegistro"));
                    listaCiudadesDestino.add(ciudad.getNombre());
                    //CARGO LA LISTA GLOBAL PARA DESPUES PODES BUSCAR EL ELEMENTO SELECCIONADO
                    itemsCiudadesDestino.add(ciudad);

                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaCiudadesDestino);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCiudadesDestino.setAdapter(adapter);

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class CargarSpinnersProvincias extends AsyncTask<Void,Integer, ResultSet>{

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query = "SELECT * FROM Provincias";

                return st.executeQuery(query);

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                listaProvincias = new ArrayList<String>();
                itemsProvincias = new ArrayList<Provincia>();

                while (resultados.next()) {
                    Provincia provincia = new Provincia(resultados.getInt("Id"),resultados.getString("Nombre"),resultados.getBoolean("EstadoRegistro"));
                    listaProvincias.add(provincia.getNombre());

                    //CARGO LA LISTA GLOBAL PARA DESPUES PODES BUSCAR EL ELEMENTO SELECCIONADO
                    itemsProvincias.add(provincia);

                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaProvincias);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spProvinciasOrigen.setAdapter(adapter);
                spProvinciasDestino.setAdapter(adapter);

                spProvinciasOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //POR MEDIO DE LA POS DEL ITEM SELECCIONADO EN EL SPINNER, OBTENGO EL OBJETO CARGADO DE MI LISTA DE OBJETOS EN LA MISMA POS
                        provOrigSelecc = itemsProvincias.get(position);
                        //CARGO EL SPINNER CON LOS DATOS DE LAS CIUDADES PERTENECIENTES A LA PROV SELECCIONADA.
                        new CargarSpinnersCiudadesOrigen().execute();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spProvinciasDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //POR MEDIO DE LA POS DEL ITEM SELECCIONADO EN EL SPINNER, OBTENGO EL OBJETO CARGADO DE MI LISTA DE OBJETOS EN LA MISMA POS
                        provDestSelecc = itemsProvincias.get(position);
                        //CARGO EL SPINNER CON LOS DATOS DE LAS CIUDADES PERTENECIENTES A LA PROV SELECCIONADA.
                        new CargarSpinnersCiudadesDestino().execute();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class AltaNuevoViaje extends AsyncTask<Void,Integer,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";

                query += "INSERT INTO Viajes";
                query += "(ConductorEmail,";
                query += "ProvinciaOrigenId,";
                query += "CiudadOrigenId,";
                query += "ProvinciaDestinoId,";
                query += "CiudadDestinoId,";
                query += "FechaHoraInicio,";
                query += "CantidadPasajeros,";
                query += "EstadoViaje)";
                query += "VALUES";
                query += "(";
                query +=  "'" + nuevoViaje.getEmailConductor() + "',";
                query +=  "'" + nuevoViaje.getProvOrigen().getIdProvincia()+ "',";
                query +=  "'" + nuevoViaje.getCiudadOrigen().getIdCiudad()+ "',";
                query +=  "'" + nuevoViaje.getProvDestino().getIdProvincia() + "',";
                query +=  "'" + nuevoViaje.getCiudadDestino().getIdCiudad() + "',";
                query +=  "'2021-10-11 00:00:00',";
                //query +=  "'" + nuevoViaje.getFechaHoraInicio() + "',"; VOLVER HABILITAR, AMI NO ME FUNCIONA. JONNA
                query +=  "'" + nuevoViaje.getCantPasajeros() + "',";
                query +=  "'" + nuevoViaje.getEstadoViaje() + "'";
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

        @Override
        protected void onPostExecute(Boolean resultado) {
            super.onPostExecute(resultado);
            if(resultado){
                Toast.makeText(contexto, "El nuevo viaje a sido creado!.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(contexto, "No se pudo generar el nuevo viaje, intente nuevamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}