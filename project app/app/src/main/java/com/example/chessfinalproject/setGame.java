package com.example.chessfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Random;

public class setGame extends Activity {

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


    int[] whiteTime = new int[2];
    int[] blackTime = new int[2];
    String gameMode = "";
    int addTime;
    int reduceTime;
    boolean isWhitePlayer = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_game);

        Intent intent1 = new Intent(this, MusicService.class);
        startService(intent1);

        // bind to the MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        buttonPauseResume = findViewById(R.id.stopResumeButton);
        buttonPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicService.pauseResume();
                    updateButtonState();
                }
            }
        });





        LinearLayout localPlay = findViewById(R.id.localPlay);
        Spinner gameModeSpinner = findViewById(R.id.gameMode);
        Spinner playerOneTime = findViewById(R.id.playerOneTime);
        Spinner playerTwoTime = findViewById(R.id.playerTwoTime);
        EditText reduceTimeEditTxt = findViewById(R.id.reduceTime);
        EditText addTimeEditTxt = findViewById(R.id.addTime);

        ImageView whiteStart = findViewById(R.id.whiteStart);
        Button randomStart = findViewById(R.id.RandomStart);
        ImageView blackStart = findViewById(R.id.blackStart);
        Button startGameBtn = findViewById(R.id.startGameBtn);
        Button changeToDefault = findViewById(R.id.changeToDefault);



        setSpinners(playerOneTime, playerTwoTime, gameModeSpinner);
        setFirstPlayer(whiteStart, blackStart, randomStart);

        //set all values to default
        changeToDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blackTime = new int[]{10,0};
                whiteTime = new int[]{10,0};
                addTime =15;
                reduceTime =15;
                isWhitePlayer = true;
                gameMode ="Classic Chess";

                reduceTimeEditTxt.setText(String.valueOf(reduceTime));
                addTimeEditTxt.setText(String.valueOf(addTime));
                blackStart.setImageResource(R.drawable.regular_rec);
                randomStart.setBackground(getResources().getDrawable(R.drawable.regular_rec));
                whiteStart.setImageResource(R.drawable.selected_rec);


                playerOneTime.setSelection(5);
                playerTwoTime.setSelection(5);
                gameModeSpinner.setSelection(0);

            }
        });


        //save the value via intent and send it to a different intent
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String reduceTimeText = reduceTimeEditTxt.getText().toString();
                String addTimeText = addTimeEditTxt.getText().toString();

                if (reduceTimeText.equals("") || addTimeText.equals("")){
                    Toast.makeText(getApplicationContext(), "enter value to add/reduce time", Toast.LENGTH_SHORT).show();
                    return;
                }


                reduceTime = Integer.parseInt(reduceTimeText);
                addTime = Integer.parseInt(addTimeText);
                if (reduceTime < 0 || addTime < 0 ){
                    Toast.makeText(getApplicationContext(), "add/reduce value should be positive", Toast.LENGTH_SHORT).show();
                    return;
                }

                //add the rest to the database


                if (gameMode.equals("Custom Chess")){
                    Intent intent = new Intent(getApplicationContext(), CustomSetBoard.class);
                    intent.putExtra("blackTime", blackTime);
                    intent.putExtra("whiteTime", whiteTime);
                    intent.putExtra("addTime", addTime);
                    intent.putExtra("reduceTime", reduceTime);
                    intent.putExtra("isWhitePlayer", isWhitePlayer);
                    intent.putExtra("gameMode", gameMode);
                    startActivity(intent);

                    startActivity(intent);
                }else {

                    Intent intent = new Intent(getApplicationContext(), gameBoardActivity.class);
                    intent.putExtra("blackTime", blackTime);
                    intent.putExtra("whiteTime", whiteTime);
                    intent.putExtra("addTime", addTime);
                    intent.putExtra("reduceTime", reduceTime);
                    intent.putExtra("isWhitePlayer", isWhitePlayer);
                    intent.putExtra("gameMode", gameMode);
                    startActivity(intent);

                    startActivity(intent);
                }

            }
        });

    }

    //mark the the button/image of the first player
    public void setFirstPlayer(ImageView whiteStart, ImageView blackStart, Button randomStart){
        whiteStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blackStart.setImageResource(R.drawable.regular_rec);
                randomStart.setBackground(getResources().getDrawable(R.drawable.regular_rec));

                whiteStart.setImageResource(R.drawable.selected_rec);

                isWhitePlayer = true;

            }
        });

        blackStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteStart.setImageResource(R.drawable.regular_rec);
                randomStart.setBackground(getResources().getDrawable(R.drawable.regular_rec));

                blackStart.setImageResource(R.drawable.selected_rec);

                isWhitePlayer = false;

            }
        });
        randomStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteStart.setImageResource(R.drawable.regular_rec);
                blackStart.setImageResource(R.drawable.regular_rec);

                randomStart.setBackground(getResources().getDrawable(R.drawable.selected_rec));

                Random random = new Random();
                isWhitePlayer = random.nextBoolean();

            }
        });



    }

    //set the spinner and save their data
    public void setSpinners(Spinner playerOneTime, Spinner playerTwoTime , Spinner gameModeSpinner){
        String[] time = new String[]{"1 minute", "2 minute", "4 minute", "6 minute", "8 minute", "10 minute","15 minute","20 minute","30 minute"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, time);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        playerOneTime.setAdapter(adapter);
        playerOneTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String currentTIme = adapterView.getItemAtPosition(position).toString();
                String[] minute = currentTIme.split(" "); // Split the string into parts separated by spaces
                int value = Integer.parseInt(minute[0]); // Convert the first part (which should be the number) to an integer
                whiteTime[0] = value;
                whiteTime[1] = 0;

                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do something when nothing is selected

            }
        });

        playerTwoTime.setAdapter(adapter);
        playerTwoTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String currentTIme = adapterView.getItemAtPosition(position).toString();
                String[] minute = currentTIme.split(" "); // Split the string into parts separated by spaces
                int value = Integer.parseInt(minute[0]); // Convert the first part (which should be the number) to an integer
                blackTime[0] = value;
                blackTime[1] = 0;

                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do something when nothing is selected

            }
        });


        String[] gameModes = new String[]{"Classic Chess","Custom Chess"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gameModes);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        gameModeSpinner.setAdapter(adapter2);
        gameModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGameMode = adapterView.getItemAtPosition(position).toString();
                gameMode = currentGameMode;

                // Do something with the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do something when nothing is selected

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unbind from the MusicService
        if (isBound) {
            unbindService(serviceConnection);
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