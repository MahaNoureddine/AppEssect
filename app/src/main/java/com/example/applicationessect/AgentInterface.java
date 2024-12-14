package com.example.applicationessect;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AgentInterface extends AppCompatActivity {

    private LinearLayout containerLayout; // LinearLayout to hold all absence containers

    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agent_interface);  // Ensure your layout is correct

        // Initialize Firebase instance
        db = FirebaseFirestore.getInstance();

        // Initialize the container layout that will hold the individual absence containers
        containerLayout = findViewById(R.id.containerLayout);

        // Fetch and display all absence details
        fetchAllAbsenceDetails();
    }

    // Method to fetch all absence details from Firestore
    private void fetchAllAbsenceDetails() {
        // Query all documents from the 'absences' collection
        db.collection("absences")
                .get()  // No filtering needed; get all documents
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Loop through all the documents and create a container for each absence
                            for (DocumentSnapshot document : querySnapshot) {
                                String date = document.getString("date");
                                String heure = document.getString("heure");
                                String salle = document.getString("salle");
                                String classe = document.getString("classe");
                                String id = document.getString("id");

                                // Create a container for each absence
                                LinearLayout absenceContainer = createAbsenceContainer(id, date, heure, salle, classe);

                                // Add the container to the main container layout
                                containerLayout.addView(absenceContainer);
                            }
                        } else {
                            // Handle case if no absences are found
                            Toast.makeText(AgentInterface.this, "Aucune absence trouvée.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle any errors during the Firestore query
                        Toast.makeText(AgentInterface.this, "Erreur lors de la récupération des informations", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to create a container for each absence
    private LinearLayout createAbsenceContainer(String id, String date, String heure, String salle, String classe) {
        LinearLayout absenceContainer = new LinearLayout(this);
        absenceContainer.setOrientation(LinearLayout.VERTICAL);  // Stack items vertically
        absenceContainer.setPadding(16, 16, 16, 16);
        absenceContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Create and add a TextView for each field
        TextView idTextView = createTextView("ID: " + id);
        TextView dateTextView = createTextView("Date: " + date);
        TextView heureTextView = createTextView("Heure: " + heure);
        TextView salleTextView = createTextView("Salle: " + salle);
        TextView classeTextView = createTextView("Classe: " + classe);

        // Add each TextView to the absence container
        absenceContainer.addView(idTextView);
        absenceContainer.addView(dateTextView);
        absenceContainer.addView(heureTextView);
        absenceContainer.addView(salleTextView);
        absenceContainer.addView(classeTextView);

        return absenceContainer;
    }

    // Helper method to create a TextView with a specific text
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);  // Set text size
        textView.setPadding(0, 8, 0, 8);  // Add padding between elements
        return textView;
    }
}
