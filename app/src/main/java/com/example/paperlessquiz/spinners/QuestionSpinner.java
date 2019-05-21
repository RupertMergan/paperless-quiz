package com.example.paperlessquiz.spinners;

import android.widget.EditText;
import android.widget.TextView;

import com.example.paperlessquiz.answer.Answer;
import com.example.paperlessquiz.question.Question;

import java.util.ArrayList;

public class QuestionSpinner {
    private ArrayList<ArrayList<Question>> questionsList;
    private int position;
    private int oldPosition;
    private int rndId;
    private TextView tvMain, tvSub, tvDisplayAnswers;
    private ArrayList<ArrayList<Answer>> myAnswers;
    private EditText answerField;

    public QuestionSpinner(ArrayList<ArrayList<Question>> questionsList, TextView tvMain, TextView tvSub,TextView tvDisplayAnswers,
                           ArrayList<ArrayList<Answer>> myAnswers, EditText answerField, int rndId) {
        this.questionsList = questionsList;
        this.tvMain = tvMain;
        this.tvSub = tvSub;
        this.tvDisplayAnswers = tvDisplayAnswers;
        this.position = 0;
        this.oldPosition = questionsList.size() - 1;
        //this.myAnswers will in practice be a link to the myAnswers
        this.myAnswers = myAnswers;
        this.answerField = answerField;
        this.rndId = rndId;
    }

    public String displayAnswers() {
        String tmp = "";
        for (int i = 0; i < questionsList.get(rndId).size(); i++) {
            tmp = tmp + (i+1) + ". " + myAnswers.get(rndId).get(i).getAnswer() + "\n";
        }
        return tmp;

    }
    public void refreshDisplay(){
        tvMain.setText(questionsList.get(rndId).get(position).getName());
        tvSub.setText(questionsList.get(rndId).get(position).getDescription());
        tvDisplayAnswers.setText(displayAnswers());
    }

    public void positionChanged() {
        //Save the answer that was given in the correct position of the myAnswers array
        setAnswer(rndId, oldPosition, answerField.getText().toString().trim());
        //Set the value of the answer for the new question to what we have in the array
        answerField.setText(getAnswer(rndId, position));
        //Show that the question has changed
        refreshDisplay();
    }

    public void initialize(int pos) {
        //This is called when a round changes
        //We don't want to save the answer that is currently in answerField in that case, because it belongs to a previous round
        //So just set the value of the answer for the new question to what we have in the array and refresh
        answerField.setText(getAnswer(rndId, pos));
        position = pos;
        refreshDisplay();
    }

    public void setMyAnswers(ArrayList<ArrayList<Answer>> myAnswers) {
        this.myAnswers = myAnswers;
    }

    public void moveTo(int newPosition) {
        oldPosition = position;
        if (newPosition < questionsList.get(rndId).size()) {
            position = newPosition;
        } else {
            position = 1;
        }
        positionChanged();
    }

    public void moveUp() {
        oldPosition = position;
        if (position == questionsList.get(rndId).size() - 1) {
            position = 0;
        } else {
            position++;
        }
        positionChanged();
    }

    public void moveDown() {
        oldPosition = position;
        if (position == 0) {
            position = questionsList.get(rndId).size() - 1;
        } else {
            position--;
        }
        positionChanged();
    }

    public void setArrayList(ArrayList<ArrayList<Question>> questionsList) {
        this.questionsList = questionsList;
    }

    public Question getQuestion(int rndId, int questionId) {
        return questionsList.get(rndId).get(questionId);
    }

    public void setAnswer(int rndId, int questionId, String answer) {
        myAnswers.get(rndId).get(questionId).setAnswer(answer);
    }

    public String getAnswer(int rndId, int questionId) {
        return myAnswers.get(rndId).get(questionId).getAnswer();
    }

    public ArrayList<ArrayList<Question>> getQuestionsList() {
        return questionsList;
    }

    public ArrayList<ArrayList<Answer>> getMyAnswers() {
        return myAnswers;
    }

    public void setRndId(int rndId) {
        this.rndId = rndId;
    }

    public void clearAnswer() {
        answerField.setText("");
    }
}