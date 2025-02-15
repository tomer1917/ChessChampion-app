package com.example.chessfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        Button btnLogout = findViewById(R.id.btn_logout);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Yes"
                        SharedPreferences sharedPreferences = getSharedPreferences("UsernamePref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String username = "";
                        editor.putString("username", username);
                        editor.apply();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "No", close the dialog
                        dialog.dismiss();
                    }
                });

                // Show the confirmation dialog
                AlertDialog dialog = builder.create();
                // Get the dialog window
                Window dialogWindow = dialog.getWindow();
                if (dialogWindow != null) {
                    // Set the background drawable
                    dialogWindow.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
                }

                dialog.show();
            }
        });
    }
}