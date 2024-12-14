package com.example.applicationessect;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class APropos extends AppCompatActivity {

    private TextView textViewDescription, textViewTechnologies, textViewTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apropos);

        // Initialisation des TextViews
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewTechnologies = findViewById(R.id.textViewTechnologies);
        textViewTeam = findViewById(R.id.textViewTeam);

        // Affichage du contenu dans les TextViews
        textViewDescription.setText("AdminInterface est une application mobile dédiée à la gestion des utilisateurs et des fichiers PDF. Elle permet aux administrateurs d'ajouter, de mettre à jour, de supprimer des utilisateurs, ainsi que de télécharger et gérer des fichiers PDF. L'application assure la sécurité des données utilisateurs et des fichiers à l'aide de Firebase.");

        textViewTechnologies.setText("Technologies utilisées :\n" +
                "- Firebase Authentication\n" +
                "- Firebase Firestore\n" +
                "- Firebase Storage\n" +
                "- Android SDK");

        textViewTeam.setText("Développé par : Noureddine Maha , Hedi Jehane\n" +
                "Étudiants à l'ESSECT Tunis dans le cadre de mon parcours en informatique de gestion.");
    }
}
