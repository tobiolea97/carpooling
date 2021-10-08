package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
}