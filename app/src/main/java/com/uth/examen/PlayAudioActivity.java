package com.uth.examen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.uth.examen.clases.Entrevistas;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PlayAudioActivity extends AppCompatActivity {
    Button btnPlay;
    ImageView txtImage;
    Entrevistas entrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);

        entrevista = (Entrevistas) getIntent().getSerializableExtra("entrevista");

        btnPlay = findViewById(R.id.btnPlay);
        txtImage = findViewById(R.id.txtPImage);

        setData(entrevista);

        btnPlay.setOnClickListener(this::onClickPlay);
    }

    private void onClickPlay(View view) {
        if (entrevista.getAudio() != null && !entrevista.getAudio().isEmpty()) {
            byte[] audioData = Base64.decode(entrevista.getAudio(), Base64.DEFAULT);

            File tempAudioFile;

            try {
                tempAudioFile = File.createTempFile("temp_audio", ".mp3", getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempAudioFile);
                fos.write(audioData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setData(@NonNull Entrevistas entrevista) {
        if (entrevista.getImagen() != null && !entrevista.getImagen().isEmpty()) {
            Bitmap bitmap = decodeBase64(entrevista.getImagen());
            txtImage.setImageBitmap(bitmap);
        }
    }

    private Bitmap decodeBase64(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}