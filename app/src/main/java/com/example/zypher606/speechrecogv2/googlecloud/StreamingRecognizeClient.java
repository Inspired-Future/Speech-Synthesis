package com.example.zypher606.speechrecogv2.googlecloud;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.google.cloud.speech.v1beta1.RecognitionConfig;
import com.google.cloud.speech.v1beta1.SpeechGrpc;
import com.google.cloud.speech.v1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Created by Ashim on 23/07/2018
 */
public class StreamingRecognizeClient {


    private int RECORDER_SAMPLERATE;
    private int RECORDER_CHANNELS;
    private int RECORDER_AUDIO_ENCODING;

    private String TAG = "StreamRecognizeClient";

    private int bufferSize = 0;

    private static final Logger logger = Logger.getLogger(StreamingRecognizeClient.class.getName());

    private final ManagedChannel channel;
    private final SpeechGrpc.SpeechStub speechClient;
    private StreamObserver<StreamingRecognizeResponse> responseObserver;
    private StreamObserver<StreamingRecognizeRequest> requestObserver;

    private AudioRecord recorder = null;

    private IResults mScreen;

    private String langCode = "en-US";

    private boolean isRecording = false;





    public StreamingRecognizeClient(ManagedChannel channel, IResults screen) throws IOException {

        this.RECORDER_SAMPLERATE = 16000;
        this.RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
        this.RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        this.bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        this.channel = channel;
        this.mScreen = screen;

        speechClient = SpeechGrpc.newStub(channel);

        responseObserver = new StreamObserver<StreamingRecognizeResponse>() {
            @Override
            public void onNext(StreamingRecognizeResponse response) {

                int numOfResults = response.getResultsCount();

                if( numOfResults > 0 ){

                    for (int i=0;i<numOfResults;i++){

                        StreamingRecognitionResult result = response.getResultsList().get(i);
                        String text = result.getAlternatives(0).getTranscript();

                        if( result.getIsFinal() ){
                            Log.d("\tFinal",  text);
                            mScreen.onFinal(text);

                            mScreen.onFinalDisp(text);

                            isRecording = false;
                        }
                        else{
                            Log.d("Partial",  text);
                            mScreen.onPartial(text);
                            isRecording = true;
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.w(TAG, "Recognize failed: {0}", error);

                try {
//                    shutdown(false);
                    shutdown();
//                    recognize();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, "Recognize complete.");

                try {
                    shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        requestObserver = speechClient.streamingRecognize(responseObserver);
    }

    public void shutdown() throws InterruptedException {

        shutdown(true);


    }

    public void shutdown(boolean closeChannel) throws InterruptedException {

        if( recorder != null ){
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if( closeChannel )
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Send streaming recognize requests to server. */
    public void recognize() throws InterruptedException, IOException {


        try {
            // Build and send a StreamingRecognizeRequest containing the parameters for
            // processing the audio.
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRate(this.RECORDER_SAMPLERATE)
                            //.setLanguageCode("en-US")
                            .setLanguageCode(langCode)
                            .build();

            // Sreaming config
            StreamingRecognitionConfig streamingConfig =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(config)
                            .setInterimResults(true)
                            .setSingleUtterance(false)
                            .build();
            // First request
            StreamingRecognizeRequest initial =
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build();

            requestObserver.onNext(initial);

            // Microphone listener and recorder
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    this.RECORDER_SAMPLERATE,
                    this.RECORDER_CHANNELS,
                    this.RECORDER_AUDIO_ENCODING,
                    bufferSize);

            recorder.startRecording();

            byte[] buffer = new byte[bufferSize];
            int recordState;

            // App Crash fix by detecting null
            if (recorder == null) return;


            // loop through the audio samplings
            while ( (recordState = recorder.read(buffer, 0, buffer.length) ) > -1 ) {

                // Set recording flag to True state
//                isRecording = true;

                // skip if there is no data
                if( recordState < 0 )
                    continue;

                // create a new recognition request
                StreamingRecognizeRequest request =
                        StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(ByteString.copyFrom(buffer, 0, buffer.length))
                                .build();

                // put it on the works
                requestObserver.onNext(request);
            }

        } catch (RuntimeException e) {
            // Cancel RPC.

            Log.d(TAG, "Error triggered");

            requestObserver.onError(e);
            return;
            // Don't throw any error, app will crash .... The fix
            // throw e;
        }
        // Mark the end of requests.
        requestObserver.onCompleted();
    }



    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }
}
