package com.example.zypher606.speechrecogv2.googlecloud;

/**
 * Created by Ashim on 23/07/2018
 */

public interface IResults {

    void onPartial(String text);

    void onFinal(String text);

    void onFinalDisp(String text);

}
