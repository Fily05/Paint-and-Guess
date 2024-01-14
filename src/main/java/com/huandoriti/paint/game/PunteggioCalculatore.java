package com.huandoriti.paint.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PunteggioCalculatore {
    /**
     * Nei giocatori è anche presente il disegnatore
     */
    private ArrayList<GiocatoreServer> giocatori;
    /**
     * In Vincitori vengono stabili in numero della posizione nell'arraylist piu avanti vuol dire
     * che hanno indovinato prima
     */
    private ArrayList<GiocatoreServer> vincitori;
    private GiocatoreServer disegnatore;
    public PunteggioCalculatore(ArrayList<GiocatoreServer> giocatori, ArrayList<GiocatoreServer> vincitori, GiocatoreServer disegnatore) {
        this.disegnatore = disegnatore;
        this.giocatori = giocatori;
        this.vincitori = vincitori;
        if (giocatori.size() < 2 || vincitori.size() > giocatori.size()) {
            throw new RuntimeException("Numero giocatori (minimo 2) e vincitori sono sbagliati: giocatore "
                    + giocatori.size() + ", vincitori "+ vincitori.size());
        }

    }

    public LinkedHashMap<GiocatoreServer, Integer> calcolaPunteggi() {
        LinkedHashMap<GiocatoreServer, Integer> punteggi = new LinkedHashMap<>();
        final int giocatoriTotali = giocatori.size();
        final int vincitoriTotali = vincitori.size();
        final int punteggioMax = (int) Math.pow(2, giocatoriTotali - 1);
        final int punteggioMin = 0;

        if (giocatoriTotali - 1 == vincitoriTotali) {
            //tutti hanno indovinato
            for (int i = 0; i < vincitori.size(); i++) {
                int punteggio = punteggioMax / 2 / (i+1);
                punteggi.put(vincitori.get(i), punteggio);
            }
            punteggi.put(disegnatore, 0);
        } else
        if (vincitoriTotali == 0) {
            for (int i = 0; i < giocatori.size(); i++) {
                int punteggio = 2;
                if (!giocatori.get(i).isDisegnatore()) {
                    punteggi.put(giocatori.get(i), 2);
                } else {
                    punteggi.put(giocatori.get(i), 0);
                }
            }
        } else //Esplicita logica
            if (vincitori.size() < giocatoriTotali - 1) {
            final int numGiocatoriPerMigliorePunteggioDisegnatore = (giocatoriTotali - 1) / 2;
            int parametroDisegnatore = Math.abs(numGiocatoriPerMigliorePunteggioDisegnatore - vincitoriTotali);
            for (int i = 0; i < vincitori.size(); i++) {
                punteggi.put(vincitori.get(i), punteggioMax / (i+1));
            }
            punteggi.put(disegnatore, punteggioMax / (parametroDisegnatore + 1));
            for (int i = 0; i < giocatori.size(); i++) {
                if (!punteggi.containsKey(giocatori.get(i))) {
                    punteggi.put(giocatori.get(i), 0);
                }
            }
        }
        return punteggi;

    }



    public ArrayList<GiocatoreServer> getGiocatori() {
        return giocatori;
    }

    public void setGiocatori(ArrayList<GiocatoreServer> giocatori) {
        this.giocatori = giocatori;
    }

    public ArrayList<GiocatoreServer> getVincitori() {
        return vincitori;
    }

    public void setVincitori(ArrayList<GiocatoreServer> vincitori) {
        this.vincitori = vincitori;
    }
}
