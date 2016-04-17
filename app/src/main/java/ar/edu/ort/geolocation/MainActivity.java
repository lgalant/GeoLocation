package ar.edu.ort.geolocation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


// Para que este codigo funcione se debe previamente:

// 1)Agregar compile 'com.squareup.okhttp:okhttp:2.4.0' en build.gradle
// 2)Agregar lo siguiente a AndroidManifest.xml
// <uses-permission android:name="android.permission.INTERNET" />
// <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

public class MainActivity extends AppCompatActivity {

    EditText direccion;
    TextView dirEncontrada, coordenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        direccion = (EditText) findViewById(R.id.direccion);
        dirEncontrada = (TextView) findViewById(R.id.dirEncontrada);
        coordenadas = (TextView) findViewById(R.id.coordenadas);
    }

    public void consultarDireccion(View v) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        if (!direccion.getText().toString().isEmpty()) {
            url += direccion.getText().toString();   // Copio la direccion ingresada al final de la URL
            new GeolocalizacionTask().execute(url);  // Llamo a clase async con url
        }
    }


    // Se conecta a Google API geocode(JavaScript). No utiliza la clase android.location.Geocoder ya que requiere API KEY
    // Parametros
    // String - la url que recibe doInBackground
    // Void -  Progreso (no se usa)
    // ArrayList<Direccion> - lo que devuelve doInBackground
    private class GeolocalizacionTask extends AsyncTask<String, Void, ArrayList<Direccion>> {
        private OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPostExecute(ArrayList<Direccion> direcciones) {
            super.onPostExecute(direcciones);
            if (!direcciones.isEmpty()) {
                dirEncontrada.setText(direcciones.get(0).direccion);    // Muestro en pantalla la primera direccion recibida
                coordenadas.setText(direcciones.get(0).coordenadas);
            }
        }

        @Override
        protected ArrayList<Direccion> doInBackground(String... params) {
            String url = params[0];

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();  // Llamado al Google API
                return parsearResultado(response.body().string());      // Convierto el resultado en ArrayList<Direccion>

            } catch (IOException | JSONException e) {
                Log.d("Error",e.getMessage());                          // Error de Network o al parsear JSON
                return new ArrayList<Direccion>();
            }
        }


        // Convierte un JSON en un ArrayList de Direccion
        ArrayList<Direccion> parsearResultado(String JSONstr) throws JSONException {
            ArrayList<Direccion> direcciones = new ArrayList<>();
            JSONObject json = new JSONObject(JSONstr);                 // Convierto el String recibido a JSONObject
            JSONArray jsonDirecciones = json.getJSONArray("results");  // Array - una busqueda puede retornar varios resultados
            for (int i=0; i<jsonDirecciones.length(); i++) {
                // Recorro los resultados recibidos
                JSONObject jsonResultado = jsonDirecciones.getJSONObject(i);
                String jsonAddress = jsonResultado.getString("formatted_address");  // Obtiene la direccion formateada

                JSONObject jsonGeometry = jsonResultado.getJSONObject("geometry");
                JSONObject jsonLocation = jsonGeometry.getJSONObject("location");
                String jsonLat = jsonLocation.getString("lat");                     // Obtiene latitud
                String jsonLng = jsonLocation.getString("lng");                     // Obtiene longitud
                String coord = jsonLat + "," + jsonLng;

                Direccion d = new Direccion(jsonAddress, coord);                    // Creo nueva instancia de direccion
                direcciones.add(d);                                                 // Agrego objeto d al array list
                Log.d("Direccion:",d.direccion + " " + coord);
            }
            return direcciones;
        }

    }
}