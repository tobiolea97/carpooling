package utn.frgp.edu.ar.carpooling.negocio;

import java.util.concurrent.ExecutionException;

import utn.frgp.edu.ar.carpooling.entities.Viaje;

public interface viajeNeg {

    int validarDatosViaje(Viaje v);
    boolean validarViajeEnRangoFechayHora(Viaje obj) throws ExecutionException, InterruptedException;
}
