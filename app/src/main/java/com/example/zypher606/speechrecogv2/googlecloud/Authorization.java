package com.example.zypher606.speechrecogv2.googlecloud;

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
 */
public class Authorization {

    private static final List<String> OAUTH2_SCOPES = Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

    public static ManagedChannel createChannel(InputStream authorizationFile, String host, int port) throws IOException {

        GoogleCredentials creds = GoogleCredentials.fromStream(authorizationFile);
        creds = creds.createScoped(OAUTH2_SCOPES);
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress(host, port)
                        .intercept(new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor()))
                        .build();
        return channel;
    }
}
