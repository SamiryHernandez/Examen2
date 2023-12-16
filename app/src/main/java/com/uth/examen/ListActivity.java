package com.uth.examen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uth.examen.clases.CustomAdapter;
import com.uth.examen.clases.Entrevistas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListActivity extends AppCompatActivity {
    ListView txtList;
    List<Entrevistas> entrevistas= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        init();

        txtList.setOnItemClickListener(this::onItemClickListener);
    }

    private void onItemClickListener(AdapterView<?> adapterView, View view, int position, long id) {
        Entrevistas entrevista = entrevistas.get(position);

        Intent intent = new Intent(this, PlayAudioActivity.class);

        intent.putExtra("entrevista", (Serializable) entrevista);
        startActivity(intent);
    }

    public void init() {
        txtList = findViewById(R.id.txtList);

        getData();
    }

    public void getData() {
        try {
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Entrevistas");
            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot item : queryDocumentSnapshots) {
                        Entrevistas ent = item.toObject(Entrevistas.class);
                        entrevistas.add(ent);
                    }

                    txtList.setAdapter(new CustomAdapter(getApplicationContext(), entrevistas));
                }
            }).addOnFailureListener(e -> Log.e("Error -> GetData()", Objects.requireNonNull(e.getMessage())));
        } catch (Exception ex) {
            Log.e("Error", Objects.requireNonNull(ex.getMessage()));
        }
    }
}