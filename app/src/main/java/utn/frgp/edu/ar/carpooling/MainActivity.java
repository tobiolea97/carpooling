package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.utils.Helper;

public class MainActivity extends AppCompatActivity {

    private String regExpEmail = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+";
    TextView email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.etMainActivityEmail);
        password = findViewById(R.id.etMainActivityPassword);

        /*Intent nextForm = new Intent(this, ConductorHome.class);
        startActivity(nextForm);
        finish();*/

    }

    public void onClickRegistrar(View view) {
        Intent nextForm = new Intent(this, PreRegistro.class);
        startActivity(nextForm);
    }

    public void onClickLogin(View view) {

        boolean isValid = true;
        isValid = validarEmail(isValid);
        isValid = validarPassword(isValid);

        if(!isValid) return;

        new IngresoConductor().execute();
        //Intent nextForm = new Intent(this, SeleccionRol.class);
        //startActivity(nextForm);
    }

    // Validaciones
    private boolean validarEmail(boolean flag) {
        if(email.getText().toString().equals("")) {
            email.setError("Campo obligatorio");
            return false;
        }
        if(!email.getText().toString().matches(regExpEmail)) {
            email.setError("Fromato requerido: ejemplo@dominio.com");
            return false;
        }
        if(email.getText().toString().length() >= 30) {
            email.setError("Este campo admite un maximo de 30 characteres");
            return false;
        }
        email.setError(null);
        return flag;
    }

    private boolean validarPassword(boolean flag) {
        if(password.getText().toString().equals("")) {
            password.setError("Campo obligatorio");
            return false;
        }
        password.setError(null);
        return flag;
    }

    private class IngresoConductor extends AsyncTask<Void,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {

                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT * FROM Usuarios WHERE Email = '";
                query += Helper.RemoverCaracteresSQLInjection(email.getText().toString());
                query+="' AND Pass ='";
                query += password.getText();
                query += "' AND Rol ='CON'";

           // query="Select * FROM Usuarios WHERE Email='"+Helper.RemoverCaracteresSQLInjection(email.getText().toString())+"' AND Pass='"+password.getText()+"' AND Rol='PAS'";

                return st.executeQuery(query);

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                boolean exists = false;
                while (resultados.next()) {
                    exists = true;
                }

                if(exists) {
                    Toast.makeText(MainActivity.this, "Existe Conductor", Toast.LENGTH_SHORT).show();

                    return;
                }
                new IngresoPasajero().execute();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class IngresoPasajero extends AsyncTask<Void,Integer, ResultSet> {
        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {

                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";
                query += "SELECT * FROM Usuarios WHERE Email = '";
                query += Helper.RemoverCaracteresSQLInjection(email.getText().toString());
                query+="' AND Pass = '";
                query += password.getText();
                query += "' AND Rol ='PAS'";
                return st.executeQuery(query);

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                boolean exists = false;
                while (resultados.next()) {
                    exists = true;
                }

                if(exists) {
                    Toast.makeText(MainActivity.this, "Existe pasajero", Toast.LENGTH_SHORT).show();

                    return;
                }
                else {
                    Toast.makeText(MainActivity.this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show();

                }

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



}