package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class HomeConductor extends AppCompatActivity {


    TextView Info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_conductor);

        Info = findViewById(R.id.txtinfo);
        SharedPreferences nombre = getSharedPreferences("Nombre", Context.MODE_PRIVATE);
        SharedPreferences apellido = getSharedPreferences("Apellido", Context.MODE_PRIVATE);
        SharedPreferences email = getSharedPreferences("Email", Context.MODE_PRIVATE);



        Info.setText(nombre.getString("Nombre","No hay Datos")+" "+apellido.getString("Apellido","No hay datos")+" "+email.getString("Email","No hay datos"));
    }
}