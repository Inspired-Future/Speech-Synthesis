package com.example.zypher606.speechrecogv2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zypher606.speechrecogv2.googlecloud.IResults;
import com.example.zypher606.speechrecogv2.googlecloud.MicrophoneStreamRecognizeClient;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements IResults  {

    private static final String API_KEY = "AIzaSyBjFAEnrWqDZLnXGy7oJCPyxB3CL64TQnE";


    private IResults Self = this;
    private TextView textView;

    private String langCode = "en-US";
    private RadioGroup radioGroup;
    private RadioButton langSelectedBtn;

    private TextView finalRecogText;
    private TextView translatedTextView;


    MicrophoneStreamRecognizeClient client;



    private Thread runner = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("Main Activity", "Start");

                client = new MicrophoneStreamRecognizeClient(getResources().openRawResource(R.raw.client_secret_apps_googleusercontent_com), Self, langCode);
                client.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);

        radioGroup = (RadioGroup) findViewById(R.id.lang_select);
        finalRecogText = (TextView) findViewById(R.id.finalRecogText);
        translatedTextView = (TextView) findViewById(R.id.translatedView);

        final Button startButton = (Button) findViewById(R.id.startStreaming);
        final Button stop = (Button) findViewById(R.id.stopStreaming);





        // checking permissions
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int RECORD_AUDIO = 666;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int ACCESS_NETWORK_STATE = 333;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    ACCESS_NETWORK_STATE);
        }



        // Start Button on click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int selectedId = radioGroup.getCheckedRadioButtonId();
                langSelectedBtn = (RadioButton) findViewById(selectedId);
                String langSelected = langSelectedBtn.getText().toString();



                if (langSelected.equals("English")) {
                    langCode = "en-US";

                } else if (langSelected.equals("Hindi")) {
                    langCode = "hi-IN";

                } else {

                    langCode = "gu-IN";
                }


                Toast.makeText(getApplicationContext(), langSelected + "  selected ;-). Code: " + langCode, Toast.LENGTH_SHORT).show();


                runner.start();
                startButton.setClickable(false);
                stop.setClickable(true);
            }
        });



        // Stop button to terminate the listening
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    Log.d("Main Activity", "Stop");
                    client.stop();
                    runner.join();
                    stop.setClickable(false);
                    startButton.setClickable(true);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });


    }


    private void initTranslation(String text) {




        final Handler textViewHandler = new Handler(Looper.getMainLooper());


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TranslateOptions options = TranslateOptions.newBuilder()
                        .setApiKey(API_KEY)
                        .build();
                Translate translate = options.getService();
                final Translation translation =
                        translate.translate(text,
                                Translate.TranslateOption.targetLanguage("en"));

                textViewHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (translatedTextView != null) {
                            translatedTextView.setText(translation.getTranslatedText());
                        }

                    }
                });


                return null;
            }
        }.execute();



    }



    private void setText(String text){
        runOnUiThread(new Runnable() {

            String text;
            public Runnable set(String text) {
                this.text = text;
                return this;
            }

            @Override
            public void run() {

                if(textView != null){
                    textView.setText(text+textView.getText());

                }
            }

        }.set(text));
    }


    @Override
    public void onPartial(String text) {

        setText("Partial: "+text+"\n");
    }

    @Override
    public void onFinal(String text) {

        setText("Final: "+text+"\n");
    }


    @Override
    public void onFinalDisp(String text) {
        runOnUiThread(new Runnable() {

            String text;
            public Runnable set(String text) {
                this.text = text;
                return this;
            }

            @Override
            public void run() {

                if(finalRecogText != null){
                    finalRecogText.setText(text);

                }
            }

        }.set(text));


        // Calling the translator method
        initTranslation(text);

    }




}
