package com.example.translatorgooglemodule;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslatorGoogle {


    private static final String API_KEY = "AIzaSyBjFAEnrWqDZLnXGy7oJCPyxB3CL64TQnE";

    public TranslatorGoogle() {



    }

    public String getTranslatedMsg(String text) {
        final Handler textViewHandler = new Handler();

        final String[] msg = {null};

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TranslateOptions options = TranslateOptions.newBuilder()
                        .setApiKey(API_KEY)
                        .build();
                Translate translate = options.getService();
                final Translation translation =
                        translate.translate("तुम कैसे हो ?",
                                Translate.TranslateOption.targetLanguage("en"));
                String temp = null;

                textViewHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String temp = translation.getTranslatedText();

                    }
                });

                msg[0] = temp;

                return null;
            }
        }.execute();

        return msg[0];
    }
}
