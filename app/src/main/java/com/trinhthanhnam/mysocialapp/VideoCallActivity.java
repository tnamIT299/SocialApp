package com.trinhthanhnam.mysocialapp;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;
import com.trinhthanhnam.mysocialapp.databinding.ActivityVideoCallBinding;

import org.json.JSONObject;

public class VideoCallActivity extends AppCompatActivity {

    ActivityVideoCallBinding binding;
    private StringeeCall2 call;
    private boolean isIncomingCall;
    private String to;
    private String callId;
    private String nameTo;

    StringeeCall2.SignalingState mSignalingState;
    StringeeCall2.MediaState mMediaState;
    StringeeAudioManager audioManager;
    boolean isSpeaker = false;
    boolean isMicOn = true;
    boolean isVideo = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addEvent();

        if(getIntent() != null){
            callId = getIntent().getStringExtra("callId");
            to = getIntent().getStringExtra("to");
            isIncomingCall = getIntent().getBooleanExtra("isIncomingCall", false);
            nameTo = getIntent().getStringExtra("nameTo");

            System.out.println("isIncomingCall: " + isIncomingCall);
        }

        binding.layoutIncomingCall.setVisibility(isIncomingCall ? View.VISIBLE : View.GONE);
        binding.btnEnd.setVisibility(isIncomingCall ? View.GONE : View.VISIBLE);

        initCall();

    }

    private void initCall() {
        if(isIncomingCall) {
            call = ChatActivity.call2Map.get(callId);
            if(call == null){
                finish();
                return;
            }
        } else {
            call = new StringeeCall2(ChatActivity.client , ChatActivity.client.getUserId(), to);
            call.setVideoCall(true);
        }
        call.setCallListener(new StringeeCall2.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s, int i, String s1) {
                runOnUiThread(() -> {
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            binding.statusTv.setText("Calling...");
                            break;
                        case RINGING:
                            binding.statusTv.setText("Ringing...");
                            break;
                        case ANSWERED:
                            binding.statusTv.setText("Answered");
                            if (mMediaState == StringeeCall2.MediaState.CONNECTED) {
                                binding.statusTv.setText("Connected");
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
            public void onError(StringeeCall2 stringeeCall2, int i, String s) {
                runOnUiThread(()->{
                    finish();
                    audioManager.stop();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall2 stringeeCall2, StringeeCall2.MediaState mediaState) {
                runOnUiThread(()->{
                    mMediaState = mediaState;
                    if(mediaState == StringeeCall2.MediaState.CONNECTED){
                        if(mSignalingState == StringeeCall2.SignalingState.ANSWERED) {
                            binding.statusTv.setText("Started");
                        }
                    }else{
                        binding.statusTv.setText("Retry to connect...");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(()->{
                    binding.vLocal.removeAllViews();
                    binding.vLocal.addView(stringeeCall2.getLocalView());
                    stringeeCall2.renderLocalView(true);

                });

            }

            @Override
            public void onRemoteStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(()->{
                    binding.vRemote.removeAllViews();
                    binding.vRemote.addView(stringeeCall2.getRemoteView());
                    stringeeCall2.renderRemoteView(false);

                });
            }

            @Override
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall2, JSONObject jsonObject) {

            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }

        });

        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {

        });
        audioManager.setSpeakerphoneOn(true);

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

        binding.btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(()->{
                    if(call != null){
                        call.switchCamera(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                    }
                });
            }
        });

        binding.btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(()->{
                    if(call != null){
                        call.enableVideo(!isVideo);
                        isVideo = !isVideo;
                        binding.btnVideo.setBackgroundResource(isVideo ? R.drawable.facetime : R.drawable.novideo);
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