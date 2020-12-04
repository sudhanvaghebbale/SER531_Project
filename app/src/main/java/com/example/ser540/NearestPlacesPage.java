package com.example.ser540;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import java.io.Serializable;
import java.util.ArrayList;

public class NearestPlacesPage extends AppCompatActivity {

    private String placeType = "All Places";
    private boolean covidSafety = false;
    private boolean popularity = false;

    ArrayList<Place> places = new ArrayList<>();

    Spinner spinner;
    CheckBox covidCheckBox;
    CheckBox popularityCheckBox;
    SearchView searchView;
    LinearLayout mainLinearLayout;
    TextView textView;
    Queries queries;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_places_page);
        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
        System.out.println(latitude + "Ankit");
        queries = new Queries(latitude, longitude);
        queries.KEYWORD = "";
        spinner = (Spinner) findViewById(R.id.spinner);
        covidCheckBox = findViewById(R.id.covidCheckBox);
        popularityCheckBox = findViewById(R.id.popularityCheckBox);
        searchView = findViewById(R.id.keywordSearchView);
        mainLinearLayout = this.findViewById(R.id.linearLayoutInsideScrollView);
        textView = findViewById(R.id.textView5);
        Button submitButton = findViewById(R.id.nearestPlacesSubmit);
        Button goToMapsButton = findViewById(R.id.goToMapButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.places_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinner.setAdapter(adapter);

        this.places = executeQuery(covidSafety, popularity, placeType, queries.KEYWORD);
        createViews();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeType = spinner.getSelectedItem().toString();
                covidSafety = covidCheckBox.isChecked();
                popularity = popularityCheckBox.isChecked();
                String keyword = searchView.getQuery().toString();

                queries.KEYWORD = keyword;
                places = executeQuery(covidSafety, popularity, placeType, queries.KEYWORD);

                createViews();
            }
        });

        goToMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NearestPlacesPage.this, MapsActivity.class);
                i.putExtra("places", places);
                startActivity(i);
            }
        });

    }

    private void createViews() {
        if((mainLinearLayout).getChildCount() > 0)
            (mainLinearLayout).removeAllViews();
        mainLinearLayout.addView(textView);
        for(Place place : places) {
            TextView tv = new TextView(this);
            tv.setText(place.getName() + "                                                        " + place.getType());
            tv.setTextSize(22);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
            lp.setMargins(0,15,0,10);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(lp);
            linearLayout.addView(tv);
            //tv.setMargins(0,10,0,10);
            mainLinearLayout.addView(linearLayout);
        }
    }

    private ArrayList<Place> executeQuery(boolean covidSafety, boolean popularity, String placeType, String keyword) {
        ArrayList<Place> places = new ArrayList<>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String serviceEndPoint = "http://ec2-34-207-128-85.compute-1.amazonaws.com:3030/dataset3";

        String query = queryBuilder(covidSafety, popularity, placeType, keyword);
        //System.out.println(query);


//        Query query1 = QueryFactory.create(query, Syntax.syntaxARQ);
//        QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(query1), new
//                DatasetImpl(ModelFactory.createDefaultModel()));
//        ResultSet resultSet = qe.execSelect();
        QueryExecution q = new QueryEngineHTTP(serviceEndPoint, query);
        ResultSet results = q.execSelect();

        ResultSetFormatter.out(System.out, results);
        places.add(new Place(51.2333, 0.4819, "1", "Name", "POI"));
        places.add(new Place(51.1333, 0.4119, "2", "Name", "POI"));
        places.add(new Place(51.0333, 0.3819, "3", "Name", "POI"));
        return places;
    }
    private String queryBuilder(boolean covidSafety, boolean popularity, String placeType, String keyword) {
        String query = "";
        if(keyword == "") {
            if(placeType.equals("POI"))
                query = queries.nearestPOI();

            else if(placeType.equals("Pubs"))
                query = queries.nearestPubs();// get nearest Pubs

            else if(placeType.equals("Tube Stations"))
                query = queries.nearestStations();//get nearest stations

            else if(covidSafety) {
                if(popularity) {
                    query = queries.nearestCovidSafePopularFederated();// get nearest federated with covid and popularity
                }
                else
                    query = queries.nearestCovidSafeFederated();// get nearest federated with only covid.
            }

            else if(popularity)
                query = queries.nearestPopularFederated();// get nearest federated with only popularity.

            else {
                query = queries.nearestFederated();
                System.out.println("This is thr query");
            }
                //get all federated.

        }

        else {
            query = queries.KeyWordSearchfederated();//get keyword federated.
        }

        System.out.println(query);
        System.out.println(queries.CURRENT_LONG);
        return query;
    }
}
