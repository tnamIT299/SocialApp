package com.trinhthanhnam.mysocialapp;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.stringee.call.StringeeCall;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.trinhthanhnam.mysocialapp.databinding.ActivityCallingBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallingActivity extends AppCompatActivity {
    ActivityCallingBinding binding;
    private StringeeCall call;
    private boolean isIncomingCall;
    private String to;
    private String callId;
    private String nameTo;

    StringeeCall.SignalingState mSignalingState;
    StringeeCall.MediaState mMediaState;
    StringeeAudioManager audioManager;
    boolean isSpeaker = false;
    boolean isMicOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addEvent();

        if(getIntent() != null){
            callId = getIntent().getStringExtra("callId");
            to = getIntent().getStringExtra("to");
            isIncomingCall = getIntent().getBooleanExtra("isIncomingCall", false);
            nameTo = getIntent().getStringExtra("nameTo");
            binding.nameTv.setText(nameTo);
        }

        binding.layoutIncomingCall.setVisibility(isIncomingCall ? View.VISIBLE : View.GONE);
        binding.btnEnd.setVisibility(isIncomingCall ? View.GONE : View.VISIBLE);

        initCall();

    }

    private void initCall() {
        if(isIncomingCall) {
            binding.nameTv.setText(nameTo);
            call = ChatActivity.callMap.get(callId);
            if(call == null){
                finish();
                return;
            }
        } else {
            call = new StringeeCall(ChatActivity.client , ChatActivity.client.getUserId(), to);
        }
        call.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String s, int i, String s1) {
                runOnUiThread(() -> {
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            binding.statusTv.setText("Calling...");
                            binding.nameTv.setText(nameTo);
                            break;
                        case RINGING:
                            binding.statusTv.setText("Ringing...");
                            binding.nameTv.setText(nameTo);
                            break;
                        case ANSWERED:
                            binding.statusTv.setText("Answered");
                            if (mMediaState == StringeeCall.MediaState.CONNECTED) {
                                binding.statusTv.setText("Connected");
                                binding.nameTv.setText(nameTo);
                            }
                            break;
                        case BUSY:
                            binding.statusTv.setText("Busy");
                            audioManager.stop();
                            finish();
                            break;
                        case ENDED:
                            binding.statusTv.setText("Ended");
                            audioManager.stop();
                            finish();
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall stringeeCall, int i, String s) {
                runOnUiThread(()->{
                    finish();
                    audioManager.stop();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall stringeeCall, StringeeCall.MediaState mediaState) {
                runOnUiThread(()->{
                    mMediaState = mediaState;
                    if(mediaState == StringeeCall.MediaState.CONNECTED){
                        if(mSignalingState == StringeeCall.SignalingState.ANSWERED) {
                            binding.statusTv.setText("Connected");
                        }
                    }else{
                        binding.statusTv.setText("Connecting...");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall stringeeCall) {

            }

            @Override
            public void onRemoteStream(StringeeCall stringeeCall) {

            }

            @Override
            public void onCallInfo(StringeeCall stringeeCall, JSONObject jsonObject) {

            }
        });

        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {

        });
        audioManager.setSpeakerphoneOn(false);

        if(isIncomingCall) {
            call.ringing(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }else{
            call.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }

    private void addEvent() {
        binding.btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() ->{
                    if(audioManager != null){
                        audioManager.setSpeakerphoneOn(!isSpeaker);
                        isSpeaker = !isSpeaker;
                        binding.btnSpeaker.setBackgroundResource(isSpeaker ? R.drawable.speaker : R.drawable.mutespeaker);
                    }
                });
            }
        });

        binding.btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() ->{
                    if(call != null){
                        call.mute(!isMicOn);
                        isMicOn = !isMicOn;
                        binding.btnMute.setBackgroundResource(isMicOn ? R.drawable.mute : R.drawable.unmute);
                    }

                });

            }
        });

        binding.btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(()->{
                    if(call != null){
                        call.answer(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        binding.layoutIncomingCall.setVisibility(View.GONE);
                        binding.btnEnd.setVisibility(View.VISIBLE);
                    }

                });
            }
        });
        binding.btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(call != null){
                    call.hangup(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                    audioManager.stop();
                    finish();
                }
            }
        });

        binding.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(()->{
                    if(call != null){
                        call.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        audioManager.stop();
                        finish();
                    }

                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        if(grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }else{
                    isGranted = true;
                }
            }
        }
        if(requestCode == 0){
            if(!isGranted){
                finish();
            }else{
                initCall();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}