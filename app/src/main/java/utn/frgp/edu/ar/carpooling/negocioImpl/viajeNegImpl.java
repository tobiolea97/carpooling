package utn.frgp.edu.ar.carpooling.negocioImpl;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;

import utn.frgp.edu.ar.carpooling.entities.Viaje;
import utn.frgp.edu.ar.carpooling.negocio.viajeNeg;
import utn.frgp.edu.ar.carpooling.utils.EnumsErrores;

public class viajeNegImpl implements viajeNeg {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int validarDatosViaje(Viaje v) {

        int valor = 0;
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
}
