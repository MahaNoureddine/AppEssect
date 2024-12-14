package com.example.applicationessect;
import com.google.firebase.FirebaseApp;
import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        MaterialButton btnAboutUs = findViewById(R.id.btnAboutUs);

        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        btnAboutUs.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this ,APropos.class);
            startActivity(intent);
        });
    }
}