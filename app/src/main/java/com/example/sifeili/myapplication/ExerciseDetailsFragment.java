package com.example.sifeili.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;

public class ExerciseDetailsFragment extends Fragment {

    private TextView textViewDetails;
    private Button btnAccept, btnRefuse;
    private String optionsList;
    private ListExercise listExercise;
    private Exercise exerciseCible;

    /*
        Créer une instance de l'interface TransMessageListener. Transférer la classe MainActivity à type TransMessageListener
        C'est pour appler les méthodes dans MainActivity et y transmettres les variables.
     */
    private TransMessageListener listener;
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (TransMessageListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        //Trouver l'id de fragment pour le charger.
        View view  = inflater.inflate(R.layout.fragment_exercise_details, null);
        /*
            Créer une instance de listExercise pour obtenir la liste des exercices créés dans le constructeur de ListExercice
         */
        listExercise = new ListExercise();

        /*
            Trouver les composantes dans l'interface
         */
        textViewDetails = (TextView) view.findViewById(R.id.f3_tv_details);
        btnAccept = (Button) view.findViewById(R.id.f3_btn_accept);
        btnRefuse = (Button) view.findViewById(R.id.f3_btn_refuse);

        //Prendre les variables envoyé par MainActivity
        Bundle bundle = getArguments();
        optionsList = bundle.getString("objective");

        /*
            Trouver s'il existe un même exercice dans la liste conservée avec l'exercice choisi par les utilisateurs
            Si oui, le Text View va afficher son description
         */
        Iterator it = listExercise.getList().iterator();
        while (it.hasNext()) {
            Exercise ex = (Exercise) it.next();
            if(optionsList.equals(ex.getCodeEnter())) {
                exerciseCible = ex;
                textViewDetails.setText(ex.getDescription());
            }
        }

        /*
            Pour identifier si les utilisateurs ont accepté ou refusé cet exercice.
            Transmettre le résultat à MainActivity en cliquant sur l'un des deux boutons
         */
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendBeginGameMsg(1);
            }
        });
        btnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendBeginGameMsg(0);
            }
        });
        return view;
    }
}
