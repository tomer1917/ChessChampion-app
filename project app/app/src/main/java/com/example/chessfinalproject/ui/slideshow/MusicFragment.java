package com.example.chessfinalproject.ui.slideshow;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chessfinalproject.MusicService;
import com.example.chessfinalproject.R;

import java.util.ArrayList;


public class MusicFragment extends Fragment {
    Activity context;
    Spinner spinner;
    ImageButton  setMusic;
    MediaPlayer testMediaPlayer = new MediaPlayer();
    String selectedMusic;
    Integer selectedMusicRaw = null; // the raw of the selectedMusic
    ArrayList<String> musicList = new ArrayList<>();// update it with the data base
    ArrayList<Integer> rawList = new ArrayList<Integer>();//the audio file of the song in the same index of the musicList
    Boolean clicked = false;



    private MusicService musicService;
    private boolean isBound = false;

    private Button buttonPauseResume;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();

        View root = inflater.inflate(R.layout.fragment_music, container, false);
        return root;

    }

    @Override
    public void onStart() {
        super.onStart();


        Intent intent1 = new Intent(context, MusicService.class);
        context.startService(intent1);

        // bind to the MusicService
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        musicList.add("choose music");
        musicList.add("Dragon Force");
        musicList.add("Lacrimosa");
        musicList.add("Dance Of The Knights");

        String[] options = musicList.toArray(new String[0]);

        musicList.remove(0);

        rawList.add(R.raw.dragonforce);
        rawList.add(R.raw.lacrimosa);
        rawList.add(R.raw.danceoftheknights);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner= (Spinner) context.findViewById(R.id.musicSpinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedMusic = adapterView.getItemAtPosition(position).toString();
                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do something when nothing is selected
                selectedMusic = null;
            }
        });

    }


    @Override
    public void onResume(){
        super.onResume();
        buttonPauseResume = context.findViewById(R.id.stopResumeButtonFrag);
        buttonPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound) {
                    musicService.pauseResume();
                    updateButtonState();
                }
            }
        });


        ImageButton testMusic = context.findViewById(R.id.testMusic);
        //play the music without changing the service
        testMusic.setOnClickListener(new View.OnClickListener() {
            private ValueAnimator valueAnimator;
            @Override
            public void onClick(View view) {
                if (selectedMusic==null){
                    return;
                }
                if (isBound) {
                    if (!clicked&&!musicService.isPaused()){
                        musicService.pauseResume();
                        updateButtonState();
                    }
                }
                if (clicked){
                    testMediaPlayer.stop();
                    clicked = false;

                    //cancel animation
                    valueAnimator.cancel();
                    return;
                }

                for (int i = 0; i < musicList.size(); i++) {
                    if (selectedMusic.equals(musicList.get(i))){
                        testMediaPlayer = MediaPlayer.create(context, rawList.get(i));
                        testMediaPlayer.start();
                        clicked = true;
                        selectedMusicRaw = rawList.get(i);

                        //start animation
                        valueAnimator = ValueAnimator.ofFloat(0f, 36000f);
                        valueAnimator.setDuration(testMediaPlayer.getDuration());
                        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float rotation = (float) animation.getAnimatedValue();
                                view.setRotation(rotation);
                            }
                        });

                        valueAnimator.start();
                        return;

                    }
                }

            }
        });


        setMusic= context.findViewById(R.id.setMusic);

        //change set the service to play the new music
        setMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedMusic == null)return;
                if (testMediaPlayer.isPlaying())
                    testMediaPlayer.stop();
                else
                {
                    testMusic.callOnClick();
                    testMediaPlayer.stop();

                }

                if (!musicService.isPaused()){
                    musicService.pauseResume();
                    updateButtonState();
                }


                if (selectedMusicRaw == null) {
                    for (int i = 0; i < musicList.size(); i++) {
                        if (selectedMusic.equals(musicList.get(i))) {
                            selectedMusicRaw = rawList.get(i);
                            musicService.setMusicSource(selectedMusicRaw);
                            if (isBound) {
                                updateButtonState();
                            }
                            selectedMusicRaw = null;
                        }
                        return;
                    }
                }
                musicService.setMusicSource(selectedMusicRaw);
                if (isBound) {
//                    musicService.pauseResume();
                    updateButtonState();
                }
                selectedMusicRaw = null;
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        testMediaPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unbind from the MusicService
        if (isBound) {
            musicService.pauseResume();
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void updateButtonState() {
        if (isBound && musicService.isPaused()) {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24)); // change the text of the button to "Resume"
        } else {
            buttonPauseResume.setBackground(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24)); // change the text of the button to "Stop"
        }
    }
}
