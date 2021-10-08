package utn.frgp.edu.ar.carpooling;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

public class Registro extends AppCompatActivity {

    EditText nombre, apellido, telefono, email, nacimiento, password, reingresoPassword;
    private String regExpNoNumbers = "^([^0-9]*)$";
    private String regExpHasNonNumericChar = "[^0-9]";
    private String regExpEmail = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+";
    private String regExpPassword = "\"^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        nombre = findViewById(R.id.etPreRegistroEmail);
        apellido = findViewById(R.id.etRegistroApellido);
        telefono = findViewById(R.id.etRegistroTelefono);
        //email = findViewById(R.id.etRegistroEmail);
        nacimiento = findViewById(R.id.etRegistroNacimiento);
        password = findViewById(R.id.etRegistroPassword);
        reingresoPassword = findViewById(R.id.etRegistroRepetirPassword);

        nacimiento.setFocusable(false);
        nacimiento.setFocusableInTouchMode(false);
        nacimiento.setInputType(InputType.TYPE_NULL);
    }

    public void onClickRegistrar(View view) {

        boolean isValid = true;
        isValid = validarNombre(isValid);
        isValid = validarApellido(isValid);
        isValid = validarTelefono(isValid);
        isValid = validarEmail(isValid);
        isValid = validarNacimiento(isValid);
        isValid = validarPassword(isValid);
        isValid = validarReingresoPassword(isValid);

        //if(!isValid) return;

        //Intent nextForm = new Intent(this, SeleccionRol.class);
        //startActivity(nextForm);
        //finish();
    }

    public void onClickFechaNacimiento(View view) {

        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                String selectedDate =
                        (day < 10 ? "0" + day :  day) + "/" +
                                (month + 1 < 10 ? "0" + (month+1) : (month+1)) +"/" +
                                year;
                nacimiento.setText(selectedDate);
                nacimiento.setError(null);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    // VALIDACIONES
    private boolean validarNombre(boolean flag) {

        if(nombre.getText().toString().equals("")) {
            nombre.setError("Campo obligatorio");
            return false;
        }
        if(!nombre.getText().toString().matches(regExpNoNumbers)) {
            nombre.setError("Este campo no admite números");
            return false;
        }
        if(nombre.getText().toString().length() >= 20) {
            nombre.setError("Este campo admite un maximo de 20 characteres");
            return false;
        }
        nombre.setError(null);
        return flag;
    }

    private boolean validarApellido(boolean flag) {

        if(apellido.getText().toString().equals("")) {
            apellido.setError("Campo obligatorio");
            return false;
        }
        if(!apellido.getText().toString().matches(regExpNoNumbers)) {
            apellido.setError("Este campo no admite números");
            return false;
        }
        if(apellido.getText().toString().length() >= 20) {
            apellido.setError("Este campo admite un maximo de 20 characteres");
            return false;
        }
        apellido.setError(null);
        return flag;
    }

    private boolean validarTelefono(boolean flag) {
        if(telefono.getText().toString().equals("")) {
            telefono.setError("Campo obligatorio");
            return false;
        }
        if(telefono.getText().toString().matches(regExpHasNonNumericChar)) {
            telefono.setError("Este campo solo admite números");
            return false;
        }
        if(telefono.getText().toString().length() >= 20) {
            telefono.setError("Este campo admite un máximo de 15 caracteres");
            return false;
        }
        telefono.setError(null);
        return flag;
    }

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

    private boolean validarNacimiento(boolean flag) {

        if(nacimiento.getText().toString().equals("")) {
            nacimiento.setError("Campo obligatorio");
            return false;
        }
        nacimiento.setError(null);
        return flag;

    }

    private boolean validarPassword(boolean flag) {
        if(password.getText().toString().equals("")) {
            password.setError("Campo obligatorio");
            return false;
        }
        if(password.getText().toString().length() < 8) {
            password.setError("La contraseña debe tener al menos 8 caracteres");
            return false;
        }
        password.setError(null);
        return flag;
    }

    private boolean validarReingresoPassword(boolean flag) {
        if(reingresoPassword.getText().toString().equals("")) {
            reingresoPassword.setError("Campo obligatorio");
            return false;
        }
        if(!reingresoPassword.getText().toString().equals(password.getText().toString())) {
            reingresoPassword.setError("Las contraseñas no coinciden");
            return false;
        }
        reingresoPassword.setError(null);
        return flag;
    }

}