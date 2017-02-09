package com.example.fabrice.parking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private String coord = null;
    private String urldistance = null;
    String[] tableau = null;
    private LocationManager lm;
    private ArrayList<LocationProvider> providers = new ArrayList<>();
    private ArrayList<Double> coordonnes=new ArrayList();
    public static Location maPosition;

    // URL DU JASON
    private static String url = "https://opendata.lillemetropole.fr/api/records/1.0/search/?dataset=disponibilite-parkings&facet=libelle&facet=ville&facet=etat&rows=24";
    public static ArrayList<HashMap<String, String>> contactList;

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactList = new ArrayList<>();
        this.mHandler = new Handler();
        lv = (ListView) findViewById(R.id.list);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        for(String name : lm.getProviders(true))
        {
            providers.add(lm.getProvider("name"));
        }
        // RECUP2RATION DES COORDDONNEES DE MA POSITON
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == 0)
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,MainActivity.this);
            maPosition= new Location(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        }
        new GetParking().execute();
        m_Runnable.run();
    }

    // METHOD POUR RAFFRAICHIR
    public final Runnable m_Runnable = new Runnable() {
        @Override
        public void run() {

            MainActivity.this.mHandler.postDelayed(m_Runnable,5000);

        }
    };

    // Clicquer sur le TEXT CARTE pour afficher la page googlmap
    public boolean buttonOnClick (View v){

        // Intent et split des coordones du json pour les récuperer dans le google map

        Intent gomap = new Intent(getApplicationContext(),MapsActivity.class);
        TextView temp = (TextView) v.findViewById(R.id.coordones);
        tableau = temp.getText().toString().split(",");
        tableau[0] = tableau[0].substring(1);
        tableau[1] = tableau[1].subSequence(0, tableau[1].length()-1).toString();
        gomap.putExtra("latitude", tableau[0]);
        gomap.putExtra("longitude", tableau[1]);
        startActivity(gomap);
        return true;
    }
    public boolean complet (View v){

        // Intent et split des coordones du json pour les récuperer dans le google map

        Intent gomap = new Intent(getApplicationContext(),MapsActivity.class);
        TextView temp = (TextView) findViewById(R.id.coordones);
        tableau = temp.getText().toString().split(",");
        tableau[0] = tableau[0].substring(1);
        tableau[1] = tableau[1].subSequence(0, tableau[1].length()-1).toString();
        double[] tableauTemp = new double[coordonnes.size()];
        for (int i =0; i<tableauTemp.length; i++)
        {
            tableauTemp[i] = coordonnes.get(i);
        }
        gomap.putExtra("liste",tableauTemp);
        gomap.putExtra("latitude", tableau[0]);
        gomap.putExtra("longitude", tableau[1]);
        gomap.putExtra("all",true);
        startActivity(gomap);
        return true;
    }


    /**
     * Async task class to get json by making HTTP call
     */
    // CLASSE ASYNTOPE
    private class GetParking extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // chercher dans le json dans le node records
                    JSONArray contacts = jsonObj.getJSONArray("records");

                    // boucle pour chercher les parkings dans les jason
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("datasetid");

                        // NODE FIELDS
                        JSONObject fields = c.getJSONObject("fields");

                        String ville = fields.getString("ville");

                        // Condition la ville de roubaix on met dans la liste
                        if (fields.getString("ville").equals("Roubaix")) {
                            String adresse = fields.getString("adresse");
                            String libelle = fields.getString("libelle");
                            coord = fields.getString("coordgeo");

                            HashMap<String, String> contact = new HashMap<>();
                            // on insere les donnés JSON
                            contact.put("datasetid", id);
                            contact.put("ville", ville);
                            contact.put("adresse", adresse);
                            contact.put("libelle", libelle);
                            contact.put("coordgeo", coord);
                            contact.put("dispo", "indisponible");
                            contact.put("etat", "indisponible");
                            contactList.add(contact);
                            tableau = coord.split(",");
                            tableau[0] = tableau[0].substring(1);
                            tableau[1] = tableau[1].subSequence(0, tableau[1].length() - 1).toString();
                            coordonnes.add(Double.parseDouble(tableau[0]));
                            coordonnes.add(Double.parseDouble(tableau[1]));
                        } else {
                            String dispo = fields.getString("dispo");
                            String etat = fields.getString("etat");
                            String adresse = fields.getString("adresse");
                            String libelle = fields.getString("libelle");
                            coord = fields.getString("coordgeo");
                            HashMap<String, String> contact = new HashMap<>();
                            // adding each child node to HashMap key => value
                            contact.put("datasetid", id);
                            contact.put("ville", ville);
                            contact.put("adresse", adresse);
                            contact.put("libelle", libelle);
                            contact.put("coordgeo", coord);
                            contact.put("etat", etat);
                            contact.put("dispo", dispo);
                            contactList.add(contact);
                            tableau = coord.split(",");
                            tableau[0] = tableau[0].substring(1);
                            tableau[1] = tableau[1].subSequence(0, tableau[1].length() - 1).toString();
                            coordonnes.add(Double.parseDouble(tableau[0]));
                            coordonnes.add(Double.parseDouble(tableau[1]));
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();


            Gestion_distance api = new Gestion_distance();
            ;
            for (int i = 0; i < contactList.size(); i++) {
                //Créer un nouveau thread pour chaque élément
                api.getNewThread().execute(Double.parseDouble(i + ""), coordonnes.get(i * 2), coordonnes.get((i * 2) + 1));

            }
            while (contactList.size() != api.liste_distance.size()) {
            }
            for (int i = 0; i < contactList.size(); i++) {
                contactList.get(i).put("distance", api.liste_distance.get(i) + "");
            }


            //TRIER LA LIST DANS L'ORDRE APR 0 L'ETAT OUVERT FERMER :
            //Trie de la liste en fonctione des distances:
            Collections.sort(contactList, new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                    String etat1 = "";
                    String etat2 = "";
                    if (o1.get("etat").equals("OUVERT") || o1.get("etat").equals("LIBRE") ){
                        etat1 = "OUVERT";
                    } else {
                        etat1 = "FERMER";
                    }
                    if (o2.get("etat").equals("OUVERT") || o2.get("etat").equals("LIBRE")) {
                        etat2 = "OUVERT";
                    } else {
                        etat2 = "FERMER";
                    }

                    if (etat1.equals(etat2)) {
                        return o1.get("distance").compareTo(o2.get("distance"));
                    } else {
                        return etat2.compareTo(etat1);
                    }
                }
            });

            // Convertir la distance en KM
            for(int i=0 ; i<contactList.size(); i++)
            {
                String distance = contactList.get(i).get("distance");
                // COnvertir en km
                distance = (float)(Double.parseDouble(distance)/1000.0) +"";
                Log.d("COUCOUCOUCOU",distance+ "");
                // split pour récuperer 2 chifrres apres la virgules

                if(distance.split("\\.")[1].length() ==1)
                {
                    distance= distance.split("\\.")[0] + "." + distance.split("\\.")[1] +" km";
                }
                else
                {
                    distance= distance.split("\\.")[0] + "." + distance.split("\\.")[1].substring(0,2) +" km";

                }


                // Remplacer le . en ,
                distance.replace(".", ",");
                contactList.get(i).remove("distance");
                contactList.get(i).put("distance" , distance);
            }

            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"ville", "libelle", "adresse", "etat", "dispo", "coordgeo", "distance"}, new int[]{R.id.ville,
                    R.id.libelle, R.id.adresse, R.id.etat, R.id.dispo, R.id.coordones, R.id.dist}) {
                // changer le couleur en fonction de l'état
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(R.id.etat);
                    switch (text1.getText().toString()) {
                        case "OUVERT":
                            text1.setBackgroundColor(Color.GREEN);
                            break;
                        case "LIBRE":
                            text1.setBackgroundColor(Color.GREEN);
                            break;
                        default:
                            text1.setBackgroundColor(Color.RED);
                            break;
                    }
                    return view;
                }
            };

            lv.setAdapter(adapter);
        }
    }
    // METHOD POUR RECUPERER SES COORDONNEES
    @Override
    public void onLocationChanged(Location location)
    {
        /*double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        float accuracy = location.getAccuracy();
        String msg = String.format(getResources().getString(R.string.new_location), String.valueOf(latitude), String.valueOf(longitude), String.valueOf(altitude), String.valueOf(accuracy));
        Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_LONG).show();*/
    }
    @Override
    public void onProviderDisabled(String provider)
    {
       /* String msg = String.format(getResources().getString(R.string.provider_disabled), provider);
       Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();   */

    }
    @Override
    public void onProviderEnabled(String provider)
    {
       /* String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); */
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        /*String newStatus = "";
        switch (status)
        {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                 newStatus = "AVAILABLE";
                break;
        }
        String msg = String.format(getResources().getString(R.string.provider_new_status), provider,newStatus);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.d("TEST",msg)     }
                */
    }

}