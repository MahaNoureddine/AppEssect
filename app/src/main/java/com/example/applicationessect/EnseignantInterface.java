package com.example.applicationessect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.Toast;

public class EnseignantInterface extends AppCompatActivity {

    private TextView textViewWelcome, textViewAbsences;
    private Button buttonAddAbsence, buttonViewSchedule, buttonInfos, buttonDisconnect;
    private FirebaseFirestore firestore; // Firebase Firestore instance
    private FirebaseAuth auth; // FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enseignant_interface);

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        textViewWelcome = findViewById(R.id.textview_welcome);
        textViewAbsences = findViewById(R.id.textview_absences);
        buttonAddAbsence = findViewById(R.id.button_add_absence);
        buttonViewSchedule = findViewById(R.id.button_view_schedule);
        buttonInfos = findViewById(R.id.button_infos);
        buttonDisconnect = findViewById(R.id.button_disconnect);

        // Fetch the current user's data from Firestore
        fetchUserData();

        // Set up button listeners
        buttonDisconnect.setOnClickListener(v -> logout());

        buttonAddAbsence.setOnClickListener(v -> {
            Intent intent = new Intent(EnseignantInterface.this, AjouterAbsenceActivity.class);
            startActivity(intent);
        });

        buttonInfos.setOnClickListener(v -> {
            Intent intent = new Intent(EnseignantInterface.this, InfoAbsencesActivity.class);
            startActivity(intent);
        });
    }

    private void logout() {
        // Sign out from FirebaseAuth
        auth.signOut();

        // Show a message to the user
        Toast.makeText(EnseignantInterface.this, "Vous vous êtes déconnecté.", Toast.LENGTH_SHORT).show();

        // Redirect to the login activity (assuming your login activity is named SignInActivity)
        Intent intent = new Intent(EnseignantInterface.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
        startActivity(intent);

        // Close the current activity
        finish();
    }

    private void fetchUserData() {
        // Get the current user's email
        String currentUserEmail = auth.getCurrentUser().getEmail();

        if (currentUserEmail == null) {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the Firestore collection "users" by email
        firestore.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Retrieve user details
                                String fullName = document.getString("name");
                                Long absences = document.getLong("absences");

                                // Update the UI
                                textViewWelcome.setText("Bienvenue cher enseignant " + (fullName != null ? fullName : ""));
                                textViewAbsences.setText("Vous avez " + (absences != null ? absences : 0) + " absences.");
                            }
                        } else {
                            textViewWelcome.setText("Aucun utilisateur trouvé.");
                        }
                    } else {
                        textViewWelcome.setText("Erreur lors de la récupération des données.");
                        System.out.println("Erreur Firestore : " + (task.getException() != null ? task.getException().getMessage() : "Inconnue"));
                    }
                });
    }
}
