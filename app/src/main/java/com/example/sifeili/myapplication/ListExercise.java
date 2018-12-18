package com.example.sifeili.myapplication;

import java.util.ArrayList;

/*
    Quand cette classe est initialisé, il va créer une liste d'exercice pour conserver différente exercice
 */

public class ListExercise {

    private static String COUP_DROIT = "Coupe droit";
    private static String REVERS = "Revers";
    private static String JEU_DE_JAMBE = "Jeu de Jambe";
    private static String SERVICE = "Service";
    private static String RETOUR_DE_SERVICE = "Retour de Service";

    private static String CODE1 = "CODENNYYNCODENNNNY";
    private static String CODE2 = "CODENYNYNCODENNNNN";
    private ArrayList<Exercise> list ;

    public ListExercise() {
        list = new ArrayList<>();
        init();
    }
    public void init() {
        Exercise ex1 = new Exercise(1,"Exercise1", "Exercice1: \nPlayer1: \n Service; \n Jeu de Jambe \n\nPlayer2: \n Retour de Service", CODE1);
        ex1.addList1(SERVICE);
        ex1.addList1(JEU_DE_JAMBE);
        ex1.addList2(RETOUR_DE_SERVICE);
        this.list.add(ex1);
        Exercise ex2 = new Exercise(2, "Exercise2", "do exercise2", CODE2);
        ex2.addList1(REVERS);
        ex2.addList1(SERVICE);
        this.list.add(ex2);
    }

    public ArrayList<Exercise> getList() {
        return this.list;
    }


}
