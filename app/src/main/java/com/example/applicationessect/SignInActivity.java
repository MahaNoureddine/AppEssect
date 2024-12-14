package com.example.applicationessect;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;


public class SignInActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private FirebaseAuth mAuth;
    private FirebaseAuth auth; // FirebaseAuth instance


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialiser views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);



        // Set click listeners
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {

        });

        btnSignIn.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnSignIn.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();  // Use mAuth here instead of auth

                        if (user != null) {
                            if (email.equals("noureddinemaha7@gmail.com")) {
                                // Navigate to Admin Interface
                                Intent intent = new Intent(SignInActivity.this, AdminInterface.class);
                                startActivity(intent);
                                finish();
                            }
                                else if (email.equals("ii@gmail.com")) {
                                    // Navigate to Admin Interface
                                    Intent intent = new Intent(SignInActivity.this, AgentInterface.class);
                                    startActivity(intent);
                                    finish();
                            } else {
                                // Navigate to default user interface (e.g., EnseignantInterface)
                                Intent intent = new Intent(SignInActivity.this, EnseignantInterface.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(SignInActivity.this,
                                R.string.sign_in_error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}