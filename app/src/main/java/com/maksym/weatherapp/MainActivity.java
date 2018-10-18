package com.maksym.weatherapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button button;
    TextView test, wind, sunset, sunrise, cloud, pressure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        test = (TextView) findViewById(R.id.test);
        wind = (TextView) findViewById(R.id.wind);
        sunset = (TextView) findViewById(R.id.sunset);
        sunrise = (TextView) findViewById(R.id.sunrise);
        cloud = (TextView) findViewById(R.id.cloud);
        pressure = (TextView) findViewById(R.id.pressure);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText search = (EditText) findViewById(R.id.search);
                URL url = createURL(search.getText().toString());

                if(url != null){
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    getLocalWeatherTask.execute(url);
                }

            }
        });
    }

    private URL createURL(String city){
        String apiKey = getString(R.string.api_key);
        String baseURL = getString(R.string.web_service_url);

        try {
            String urlString = baseURL + URLEncoder.encode(city, "UTF-8") +
                    "&units=metric&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    )) {
                        String line;

                        while ((line = reader.readLine()) != null ) {
                            builder.append(line);
                        }
                    }

                    catch (IOException e ) {
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                }
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            finally {
                connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJSON(weather);

        }
    }

    private void convertJSON(JSONObject forecast) {
        try {
                if(forecast != null){
                    JSONObject main = forecast.getJSONObject("main");
                    JSONObject windjson = forecast.getJSONObject("wind");
                    JSONObject clouds = forecast.getJSONObject("clouds");
                    JSONObject sys = forecast.getJSONObject("sys");
                    Double temp = main.getDouble("temp");
                    Integer pressurejson = main.getInt("pressure");
                    Double wind_speed = windjson.getDouble("speed");
                    Integer wind_degree = windjson.getInt("deg");
                    Integer all = clouds.getInt("all");
                    Integer sunrisejson = sys.getInt("sunrise");
                    Integer sunsetjson = sys.getInt("sunset");

                    test.setText(String.valueOf(temp + " °C"));
                    wind.setText(String.valueOf("Wind: Speed:" + wind_speed + " m/sec, Degrees: " + wind_degree));
                    pressure.setText(String.valueOf("Pressure: " + pressurejson + " hPa"));
                    cloud.setText(String.valueOf("Clouds: " + all + " %"));
                    sunrise.setText(String.valueOf("Sunrise: " + unixToDate(sunrisejson)));
                    sunset.setText(String.valueOf("Sunset: " + unixToDate(sunsetjson)));
                }
                else{
                    test.setText("Such city doesn't exist");
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String unixToDate(Integer unix) {
        long dv = Long.valueOf(unix)*1000;
        Date df = new java.util.Date(dv);
        String res = new SimpleDateFormat("MM/dd/yyyy hh:mma").format(df);
        return res;
    }
}