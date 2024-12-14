package com.example.applicationessect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;


public class AjouterAbsenceActivity extends AppCompatActivity {
    private EditText editTextDate, editTextHeure, editTextSalle, editTextClasse, editTextId;
    private Button buttonSubmit, buttonReset;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajouter_absence);

        // Initialiser Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialiation views
        editTextDate = findViewById(R.id.editTextDate);
        editTextHeure = findViewById(R.id.editTextHeure);
        editTextSalle = findViewById(R.id.editTextSalle);
        editTextClasse = findViewById(R.id.editTextClasse);
        editTextId = findViewById(R.id.editTextId);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonReset = findViewById(R.id.buttonReset);

        buttonReset.setOnClickListener(v -> {
            Intent intent = new Intent(AjouterAbsenceActivity.this, EnseignantInterface.class);
            startActivity(intent);
        });

        buttonSubmit.setOnClickListener(v -> {
            String date = editTextDate.getText().toString();
            String heure = editTextHeure.getText().toString();
            String salle = editTextSalle.getText().toString();
            String classe = editTextClasse.getText().toString();
            String id = editTextId.getText().toString();

            if (date.isEmpty() || heure.isEmpty() || salle.isEmpty() || classe.isEmpty() || id.isEmpty()) {
                Toast.makeText(AjouterAbsenceActivity.this, "Tous les champs sont obligatoires!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Erreur : Utilisateur non authentifié.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = firebaseAuth.getCurrentUser().getUid();

            // Create a map to store the absence data
            Map<String, Object> absenceData = new HashMap<>();
            absenceData.put("date", date);
            absenceData.put("heure", heure);
            absenceData.put("salle", salle);
            absenceData.put("classe", classe);
            absenceData.put("id", id); // id corresponds to the teacher's name in absences

            // enregistrer absence data sur Firestore
            firestore.collection("absences")
                    .add(absenceData)
                    .addOnSuccessListener(documentReference -> {
                        updateUserAbsenceCount(id); // Pass "id" to match the name field in users
                        Toast.makeText(AjouterAbsenceActivity.this, "Absence ajoutée avec succès!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AjouterAbsenceActivity.this, "Erreur lors de l'ajout de l'absence", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Erreur lors de l'ajout de l'absence", e);
                    });
        });
    }

    private void updateUserAbsenceCount(String userName) {
        // Step 1: Ensure the absences have been added first
        firestore.collection("absences")
                .whereEqualTo("id", userName) // Match id with the name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            int totalAbsences = querySnapshot.size();

                            // Step 2: Update user absence count sur users collection
                            firestore.collection("users")
                                    .whereEqualTo("name", userName) // Find the user by name
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult() != null && !userTask.getResult().isEmpty()) {
                                            String userId = userTask.getResult().getDocuments().get(0).getId();

                                            Map<String, Object> userUpdates = new HashMap<>();
                                            userUpdates.put("absences", totalAbsences);

                                            firestore.collection("users")
                                                    .document(userId)
                                                    .update(userUpdates)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("Firestore", "Mise à jour des absences réussie pour " + userName);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("Firestore", "Erreur lors de la mise à jour des absences", e);
                                                        Toast.makeText(AjouterAbsenceActivity.this, "Erreur lors de la mise à jour des absences", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Log.e("Firestore", "Utilisateur introuvable pour le nom: " + userName);
                                            Toast.makeText(AjouterAbsenceActivity.this, "Utilisateur introuvable.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(AjouterAbsenceActivity.this, "Aucune absence trouvée pour l'utilisateur.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Erreur lors de la récupération des absences", task.getException());
                        Toast.makeText(AjouterAbsenceActivity.this, "Erreur lors de la récupération des absences", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}