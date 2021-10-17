package utn.frgp.edu.ar.carpooling;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import utn.frgp.edu.ar.carpooling.conexion.DataDB;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link filtrar_viajes_dialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class filtrar_viajes_dialog extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context contexto;
    private Spinner spProvOrigen;
    private Spinner spProvDestino;

    public filtrar_viajes_dialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment filtrar_viajes_dialog.
     */
    // TODO: Rename and change types and number of parameters
    public static filtrar_viajes_dialog newInstance(String param1, String param2) {
        filtrar_viajes_dialog fragment = new filtrar_viajes_dialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtrar_viajes_dialog, container, false);
        contexto = view.getContext();
        spProvOrigen = view.findViewById(R.id.spProvOrigen);
        spProvDestino = view.findViewById(R.id.spProvDestino);

        new CargarProvincias().execute();
        return view;
    }

    private class CargarProvincias extends AsyncTask<Void, Integer, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass);
                Statement st = con.createStatement();

                return st.executeQuery("SELECT Nombre FROM Provincias");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultados) {
            super.onPostExecute(resultados);
            try {
                List<String> provincias = new ArrayList<String>();

                while (resultados.next()) {
                    provincias.add(resultados.getString("Nombre"));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, provincias);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spProvOrigen.setAdapter(adapter);
                spProvDestino.setAdapter(adapter);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}