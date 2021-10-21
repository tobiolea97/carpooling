package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class VerPasajero extends AppCompatActivity {
    String NroViaje,Numero;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pasajero);
        NroViaje=getIntent().getStringExtra("NroViaje");
        Numero=getIntent().getStringExtra("NroViaje");

        System.out.println(NroViaje+" "+Numero);

    }
}