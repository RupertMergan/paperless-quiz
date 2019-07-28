package com.paperlessquiz.answer;

import java.io.Serializable;

public class Answer implements Serializable{
    //TODO: add question ID here so we can display it when correcting answers

    //private int questionNr;
    private int teamNr,roundNr, questionNr;
    private String theAnswer;
    private boolean corrected;
    private boolean correct;

    public Answer(int teamNr, int roundNr, int questionNr, String theAnswer, boolean corrected, boolean correct) {
        this.teamNr = teamNr;
        this.roundNr = roundNr;
        this.questionNr = questionNr;
        this.theAnswer = theAnswer;
        this.corrected = corrected;
        this.correct = correct;
    }

    public Answer(String myAnswer) {
        this.theAnswer = myAnswer;
    }

    public Answer(int questionNr){
        //this.questionNr=questionNr;
        this.theAnswer = "";
        this.correct=false;
        this.corrected = false;
    }


    public Answer(int questionNr, String myAnswer) {
        //this.questionNr = questionNr;
        this.theAnswer = myAnswer;
        //this.teamID = teamID;
    }

    public boolean isCorrected() {
        return corrected;
    }

    public void setCorrected(boolean corrected) {
        this.corrected = corrected;
    }

    //public int getQuestionNr() {
    //    return questionNr;
    //}

    public String getTheAnswer() {
        return theAnswer;
    }

    public void setTheAnswer(String myAnswer) {
        this.theAnswer = myAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
        this.corrected=true;
    }

    public int getRoundNr() {
        return roundNr;
    }

    public int getQuestionNr() {
        return questionNr;
    }

    public int getTeamNr() {
        return teamNr;
    }
}
