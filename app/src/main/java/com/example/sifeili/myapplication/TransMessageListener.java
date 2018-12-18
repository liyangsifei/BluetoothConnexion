package com.example.sifeili.myapplication;

public interface TransMessageListener {

    /*
        Méthode pour prendre les valeurs des options que l'utilisateur a choisi dans le ChooseObjectiveFragment
     */
    public void sendGameOption(String str);

    /*
        Méthode pour prendre le résultat que l'utilisateur a accordé ou refusé l'exercice
     */
    public void sendBeginGameMsg(int i);
}
