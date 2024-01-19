package com.example.languagetranslatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner sourceLanguageSpinner, targetLanguageSpinner;
    private TextInputEditText sourceEditText;
    private ImageView micImageView;
    private MaterialButton translateButton;
    private TextView translatedTextView;

    String[] sourceLanguages = {"Din", "Română", "Afrikaans", "Arabă", "Bengali", "Bielorusă", "Bulgară", "Catalană",
            "Chineză", "Cehă", "Coreeană", "Engleză", "Franceză", "Germană", "Hindi", "Italiană", "Japoneză", "Maghiară", "Spaniolă", "Ucraineană", "Urdu"};

    String[] targetLanguages = {"În", "Română", "Afrikaans", "Arabă", "Bengali", "Bielorusă", "Bulgară", "Catalană",
            "Chineză", "Cehă", "Coreeană", "Engleză", "Franceză", "Germană", "Hindi", "Italiană", "Japoneză", "Maghiară", "Spaniolă", "Ucraineană", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int sourceLanguageCode, targetLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceLanguageSpinner = findViewById(R.id.idSourceLanguageSpinner);
        targetLanguageSpinner = findViewById(R.id.idTargetLanguageSpinner);
        sourceEditText = findViewById(R.id.idEditTextSource);
        micImageView = findViewById(R.id.idImageViewMic);
        translateButton = findViewById(R.id.idButtonTranslate);
        translatedTextView = findViewById(R.id.idTextViewTranslated);

        sourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sourceLanguageCode = getLanguageCode(sourceLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter sourceAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, sourceLanguages);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceLanguageSpinner.setAdapter(sourceAdapter);

        targetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguageCode = getLanguageCode(targetLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter targetAdapter = new ArrayAdapter(this, R.layout.spinner_item, targetLanguages);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetLanguageSpinner.setAdapter(targetAdapter);

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTextView.setText("");
                if (sourceEditText.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Introduceți textul pe care doriți să-l traduceți", Toast.LENGTH_SHORT).show();
                } else if (sourceLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Selectați limba textului de tradus", Toast.LENGTH_SHORT).show();
                } else if (targetLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Selectați limba în care doriți să traduceți textul", Toast.LENGTH_SHORT).show();
                } else {
                    performTranslation(sourceLanguageCode, targetLanguageCode, sourceEditText.getText().toString());
                }
            }
        });

        micImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Vorbește pentru a converti în text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEditText.setText(result.get(0));
            }
        }
    }

    private void performTranslation(int sourceLanguageCode, int targetLanguageCode, String source) {
        translatedTextView.setText("Descărcare model..");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTextView.setText("Traducere în curs...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTextView.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Traducerea nu a fost reușită: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Descărcarea modelului de limbaj a eșuat " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Română":
                languageCode = FirebaseTranslateLanguage.RO;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabă":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Bielorusă":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgară":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Catalană":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Chineză":
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            case "Cehă":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Coreeană":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Engleză":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Franceză":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Germană":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Italiană":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japoneză":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Maghiară":
                languageCode = FirebaseTranslateLanguage.HU;
                break;
            case "Spaniolă":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Ucraineană":
                languageCode = FirebaseTranslateLanguage.UK;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            default:
                languageCode = 0;
        }
        return languageCode;
    }
}
