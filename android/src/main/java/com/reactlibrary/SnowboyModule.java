package com.reactlibrary;


import com.facebook.react.common.ReactConstants;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;

import android.os.Handler;
import android.os.Message;

import android.widget.Toast;

import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.audio.AudioDataSaver;
import ai.kitt.snowboy.audio.RecordingThread;
import ai.kitt.snowboy.AppResCopy;

public class SnowboyModule extends ReactContextBaseJavaModule {
    
    private RecordingThread recordingThread;
	private int preVolume = -1;
    private final ReactApplicationContext reactContext;

    public SnowboyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private static final String TAG = "Snowboy";

    @Override
    public String getName() {
        return "Snowboy";
    }

    @ReactMethod
    public void sampleMethod(Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("working");
    }

    @ReactMethod
    public void startSnowboyService() {
        Intent intent = new Intent(reactContext, SnowboyService.class);
        reactContext.startForegroundService(intent);
    }

    @ReactMethod
    public void stopSnowboyService() {
        Intent intent = new Intent(reactContext, SnowboyService.class);
        reactContext.stopService(intent);
    }

    @ReactMethod
    public void initHotword(Promise promise) {
        if (ActivityCompat.checkSelfPermission(reactContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(reactContext,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            AppResCopy.copyResFromAssetsToSD(reactContext);
			try {
				recordingThread = new RecordingThread(new Handler() {
                    
					@Override
					public void handleMessage(Message msg) {
						MsgEnum message = MsgEnum.getMsgEnum(msg.what);
						String messageText = (String) msg.obj;

						switch(message) {
							case MSG_ACTIVE:
								//HOTWORD DETECTED. NOW DO WHATEVER YOU WANT TO DO HERE
								sendEvent("msg-active", "MSG_ACTIVE");
                                // Log.v(TAG, "MSG_ACTIVE");
								break;
							case MSG_INFO:
								sendEvent("msg-info", "MSG_INFO");
								break;
							case MSG_VAD_SPEECH:
								sendEvent("msg-vad-speech", "MSG_VAD_SPEECH");
								break;
							case MSG_VAD_NOSPEECH:
								sendEvent("msg-vad-nospeech", "NO_SPEECH");
								break;
							case MSG_ERROR:
								sendEvent("msg-error", "MSG_ERROR");
								break;
							default:
								super.handleMessage(msg);
								break;
						}
					}
				}, new AudioDataSaver());
                //Toast.makeText(reactContext, "thread created", Toast.LENGTH_SHORT).show();
				promise.resolve(true);
			} catch(Exception e) {
				String errorMessage = e.getMessage();
				promise.reject(errorMessage);
			}
        }

    }

    @ReactMethod
    public void start() {
        Log.v(TAG, "Start recording");

        if(recordingThread !=null) {
			recordingThread.startRecording();
        }
    }

    @ReactMethod
    public void stop() {
        Log.v(TAG, "Stop recording");

        if(recordingThread !=null){
            recordingThread.stopRecording();
        }
    }

    @ReactMethod
    public void destroy() {
        recordingThread.stopRecording();
    }
	 
	private void sendEvent(String eventName, String msg) {
        WritableMap params = Arguments.createMap();
        params.putString("value", msg);
        reactContext
				.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
