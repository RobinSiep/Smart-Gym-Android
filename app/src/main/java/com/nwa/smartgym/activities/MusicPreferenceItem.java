package com.nwa.smartgym.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.nwa.smartgym.R;
import com.nwa.smartgym.api.MusicPreferenceAPI;
import com.nwa.smartgym.api.ServiceGenerator;
import com.nwa.smartgym.api.SportScheduleAPI;
import com.nwa.smartgym.api.interfaces.MusicPreferenceAPIInterface;
import com.nwa.smartgym.lib.SecretsHelper;
import com.nwa.smartgym.models.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rikvanderwerf on 11-6-16.
 */
public class MusicPreferenceItem extends AppCompatActivity {
//    private com.nwa.smartgym.models.MusicPreference musicPreference;
//    private SecretsHelper secretsHelper;
//    private Spinner spGenres;
//
//    private MusicPreferenceAPIInterface musicPreferenceService;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_music_preference_item);
//        spGenres = (Spinner) findViewById(R.id.spGenres);
//        List<String> list = new ArrayList<String>();
//        list.add("list 1");
//        list.add("list 2");
//        list.add("list 3");
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, list);
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spGenres.setAdapter(dataAdapter);

//    }
}