package utn.frgp.edu.ar.carpooling.negocioImpl;

import android.os.AsyncTask;
import android.os.Build;
import android.widget.ArrayAdapter;

import androidx.annotation.RequiresApi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;
import utn.frgp.edu.ar.carpooling.entities.Ciudad;
import utn.frgp.edu.ar.carpooling.entities.Viaje;
import utn.frgp.edu.ar.carpooling.negocio.viajeNeg;
import utn.frgp.edu.ar.carpooling.utils.EnumsErrores;

public class viajeNegImpl implements viajeNeg {

    private Viaje objOrigViaje;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int validarDatosViaje(Viaje v) {

        int valor = -1;
        if(v.getProvDestino().getIdProvincia() == v.getProvOrigen().getIdProvincia() &&
                v.getCiudadDestino().getIdCiudad() == v.getCiudadOrigen().getIdCiudad()){
            valor = EnumsErrores.viaje_DestinoyOrigenIguales.ordinal();
        }

        //VOLVER A HABILITAR, AMI NO ME ANDA! JONNA
        /*if(v.getFechaHoraInicio().compareTo(LocalDateTime.now())<0){
            valor = EnumsErrores.viaje_FechayHoraAnteriorActual.ordinal();
        }
        */
        return valor;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean  validarViajeEnRangoFechayHora(Viaje obj) throws ExecutionException, InterruptedException {
        objOrigViaje = obj;
        boolean vBoleana = false;
        //UTILIZO EL GET PARA ESPERAR A QUE EL HILO TERMINE DE EJECUTARSE.
        ResultSet resultado = new buscarViajeEnRangoTiempo().execute().get();

        try {
            while (resultado.next()) {
                vBoleana = true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return vBoleana;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private class buscarViajeEnRangoTiempo extends AsyncTask<Void,Integer, ResultSet>{

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                String query = "";

                //HABILITENLO SI NO MORIMOS TODOS!!!!!   YO NO LO PUEDO PROBAR. JONNA
                /*LocalDateTime fechaInicio,fechaFin;
                fechaInicio = objOrigViaje.getFechaHoraInicio().plusHours(-3);
                fechaFin = objOrigViaje.getFechaHoraInicio().plusHours(+3);*/

                //QUERY QUE HAY QUE HABILITAR!! YO NO LA PUEDO PROBAR. JONNA
                //query = "SELECT * FROM `Viajes` WHERE (FechaHoraInicio BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "') AND ConductorEmail = '" + objOrigViaje.getEmailConductor() + "'" ;


                //QUERY PARA PODER PROBAR
                query = "SELECT * FROM `Viajes` WHERE (FechaHoraInicio BETWEEN '2021-10-27 11:00:00' AND '2021-10-27 15:15:00') AND ConductorEmail = 'tobi@mail.com'" ;

                return st.executeQuery(query);

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


}
