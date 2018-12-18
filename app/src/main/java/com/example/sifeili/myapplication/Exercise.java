package com.example.sifeili.myapplication;

import java.util.ArrayList;

/*
    Un exercice est composé par son numéro, son nom, son description, son code d'entrée
    et deux listes des activité respectivement pour les deus utilisateurs (le code d'entrée indique ces deux listes)
 */
public class Exercise {

    private int numExercise;
    private String nameExercise;
    private String description;
    private String codeEnter;
    private ArrayList<String> listForPlayer1;
    private ArrayList<String> listForPlayer2;

    public Exercise(int numExercise, String name, String description,String codeEnter) {
        this.nameExercise = name;
        this.numExercise = numExercise;
        this.description = description;
        this.codeEnter = codeEnter;
        this.listForPlayer1 = new ArrayList<>();
        this.listForPlayer2 = new ArrayList<>();
    }

    public void addList1(String opt) {
        this.listForPlayer1.add(opt);
    }
    public void addList2(String opt) {
        this.listForPlayer2.add(opt);
    }

    public String getDescription() {
        return this.description;
    }
    public String getNameExercise() { return this.nameExercise; }
    public String getCodeEnter() { return this.codeEnter; }

}
