package com.example.sifeili.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

public class ChooseObjectiveFragment extends Fragment {

    private CheckBox checkBoxObjective1,checkBoxObjective2,checkBoxObjective3,checkBoxObjective4,checkBoxObjective5;
    private Button buttonValide;

    /*
        Créer une instance de l'interface TransMessageListener. Transférer la classe MainActivity à type TransMessageListener
        C'est pour appler les méthodes dans MainActivity et y transmettres les variables.
     */
    private TransMessageListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (TransMessageListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        //Trouver l'id de fragment pour le charger.
        View view  = inflater.inflate(R.layout.fragment_choose_objective, null);

        /*
            Trouver les composantes dans l'interface
         */
        checkBoxObjective1 = (CheckBox) view.findViewById(R.id.f2_cb_objective1);
        checkBoxObjective2 = (CheckBox) view.findViewById(R.id.f2_cb_objective2);
        checkBoxObjective3 = (CheckBox) view.findViewById(R.id.f2_cb_objective3);
        checkBoxObjective4 = (CheckBox) view.findViewById(R.id.f2_cb_objective4);
        checkBoxObjective5 = (CheckBox) view.findViewById(R.id.f2_cb_objective5);
        buttonValide = (Button) view.findViewById(R.id.f2_btn_valide);

        buttonValide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    Créer un buffer pour former un String de pas à pas
                    Quand cliquer sur le bouton, les valeurs (true ou false) dans les checkboxs va être noté et envoyé à MainActivity
                 */
                String info = new String();
                StringBuffer buffer = new StringBuffer();
                buffer.append("CODE");
                if(checkBoxObjective1.isChecked()) {
                    buffer.append("Y");
                } else {
                    buffer.append("N");
                }
                if(checkBoxObjective2.isChecked()) {
                    buffer.append("Y");
                } else {
                    buffer.append("N");
                }
                if(checkBoxObjective3.isChecked()) {
                    buffer.append("Y");
                } else {
                    buffer.append("N");
                }
                if(checkBoxObjective4.isChecked()) {
                    buffer.append("Y");
                } else {
                    buffer.append("N");
                }
                if(checkBoxObjective5.isChecked()) {
                    buffer.append("Y");
                } else {
                    buffer.append("N");
                }
                info = buffer.toString();
                //Envoyer à MainActivity
                listener.sendGameOption(info);
            }
        });
        return view;
    }
}
