package com.example.zypher606.speechrecogv2.googlecloud;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.auth.ClientAuthInterceptor;

/**
 * Created by Ashim on 23/07/2018
 *
 * more help here: https://cloud.google.com/speech/reference/rpc/google.cloud.speech.v1beta1
 */
public class MicrophoneStreamRecognizeClient {

    private static final String TAG = "MicrophoneStreamRecog";
    private String host = "speech.googleapis.com";
    private Integer port = 443;
    private ManagedChannel channel;
    private StreamingRecognizeClient client;

    private final List<String> OAUTH2_SCOPES = Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

    /**
     *
     * @param authorizationFile
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    private ManagedChannel createChannel(InputStream authorizationFile, String host, int port) throws IOException {

        GoogleCredentials creds = GoogleCredentials.fromStream(authorizationFile);
        creds = creds.createScoped(OAUTH2_SCOPES);
        return ManagedChannelBuilder.forAddress(host, port)
            .intercept(new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor()))
            .build();
    }

    /**
     *
     * @param autorizationFile
     * @throws IOException
     */
    public MicrophoneStreamRecognizeClient(InputStream autorizationFile, IResults screen, String langCode) throws IOException {

        channel = createChannel(autorizationFile, host, port);
        client = new StreamingRecognizeClient(channel, screen);
        client.setLangCode(langCode);
    }

    /**
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void start() throws IOException, InterruptedException {

        client.recognize();
    }

    /**
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {

        Log.i(TAG, "Recognize stopped on command.");
        client.shutdown();
    }

    public void setLang(String langCode) {
        client.setLangCode(langCode);
    }
}
