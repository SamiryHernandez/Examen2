package com.uth.examen;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uth.examen.clases.Entrevistas;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import android.util.Base64;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_PERMISSION_CODE = 1;
    ImageView txtImagen;
    EditText txtPeriodista, txtDescripcion;
    TextView lblMessage;
    Button btnPhoto, btnStart, btnStop, btnSave, btnList, btnDate;
    LocalDateTime localDateTime = null;
    String date = "", filePath;
    Bitmap image = null;
    byte[] record = null;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        setMessage("Iniciar grabación");
        btnSave.setOnClickListener(this::onClickSave);
        btnPhoto.setOnClickListener(this::onClickPhoto);
        btnStart.setOnClickListener(this::onClickStart);
        btnStop.setOnClickListener(this::onClickStop);
        btnList.setOnClickListener(this::onClickList);
    }

    private void onClickList(View v) {
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        startActivity(intent);
    }

    private void onClickStop(View v) {
        setMessage("Audio grabado exitosamente.");

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            if (filePath != null) {
                record = extractAudioBytes(filePath);
            }
        }

        btnStop.setEnabled(false);
        btnStart.setEnabled(true);
    }

    private void onClickStart(View view) {
        if (checkPermissions()) {
            startRecording();
        } else {
            requestPermissions();
        }
    }

    private void startRecording() {
        setMessage("Grabando Audio...");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePath = getFilePath();
        }

        mediaRecorder.setOutputFile(filePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    private void onClickSave(View v) {
        if (image == null) {
            message(v, "La imagen es obligatoria.");

            return;
        }

        if (record == null) {
            message(v, "El audio es obligatorio.");

            return;
        }

        if (isEmpty(txtPeriodista)) {
            txtPeriodista.setError(getString(R.string.obligatorio));

            return;
        }

        if (isEmpty(txtDescripcion)) {
            txtDescripcion.setError(getString(R.string.obligatorio));

            return;
        }

        if(date.trim().isEmpty()) {
            message(v, "La fecha es obligatoria.");

            return;
        }

        Entrevistas entrevistas = new Entrevistas();
        entrevistas.setPeriodista(txtPeriodista.getText().toString().trim());
        entrevistas.setDescripcion(txtDescripcion.getText().toString().trim());
        entrevistas.setFecha(date);
        entrevistas.setAudio(byteArrayToBase64(record));
        entrevistas.setImagen(bitmapToBase64(image));

        saveData(entrevistas, v);
    }

    private void saveData(@NonNull Entrevistas ent, View v) {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Entrevistas");
        DocumentReference documentReference = collectionReference.document();
        String id = documentReference.getId();
        ent.setIdOrden(id);

        collectionReference.document(id).set(ent).addOnSuccessListener(unused -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotification(getApplicationContext(), "Datos guardados", "Los datos se han guardado exitosamente.");
            }

            clearInputs(getWindow().getDecorView().findViewById(android.R.id.content));
            image = null;
            record = null;
            txtImagen.setImageBitmap(null);
            setMessage("Iniciar grabación");
        }).addOnFailureListener(e -> message(v, e.getMessage()));
    }

    private void init() {
        txtDescripcion = findViewById(R.id.txtLDescripcion);
        txtPeriodista = findViewById(R.id.txtLPeriodista);
        txtImagen = findViewById(R.id.txtLImage);

        lblMessage = findViewById(R.id.lblMessage);
        btnDate = findViewById(R.id.btnOpenDatePicker);
        btnList = findViewById(R.id.btnList);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnSave = findViewById(R.id.btnSave);

        btnStop.setEnabled(false);

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                localDateTime = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.of("America/Tegucigalpa"))
                        .toLocalDateTime();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                date = localDateTime.format(formatter);
            }
            Log.e("Fecha", "Fecha = " + localDateTime);
        });

        findViewById(R.id.btnOpenDatePicker).setOnClickListener(view -> materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER_TAG"));
    }

    private void setMessage(String msg) {
        lblMessage.setText(msg);
    }

    public boolean isEmpty(@NonNull EditText editText) {
        return (editText.getText().toString().trim()).isEmpty();
    }

    public void message(View view, String msj) {
        Snackbar.make(view, msj, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void onClickPhoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean checkPermissions() {
        int recordPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        int storagePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return recordPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    private byte[] extractAudioBytes(String filePath) {
        File file = new File(filePath);
        byte[] audioBytes = null;

        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                audioBytes = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return audioBytes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras == null) throw new AssertionError();
            image = (Bitmap) extras.get("data");
            txtImagen.setImageBitmap(image);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                startRecording();
            }
        }
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private String getFilePath() {
        File directory = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
        return Objects.requireNonNull(directory).getAbsolutePath() + "/audio_record.3gp";
    }

    public void clearInputs(@NonNull ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof ViewGroup) {
                clearInputs((ViewGroup) childView);
            } else if (childView instanceof EditText) {
                ((EditText) childView).setText("");
            }
        }
    }

    public static String byteArrayToBase64(byte[] byteArray) {
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static String bitmapToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(@NonNull Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = "Application";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(context, "1").setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, builder.build());
    }
}