package com.example.paperlessquiz.Corrections;

import com.example.paperlessquiz.google.access.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CorrectionsListParser implements JsonParser<CorrectionsList> {
    //Strings here MUST match the headers in the Answers tab of the Quiz sheet
    public static final String QUESTION_ID = "QuestionID";
    public static final String ROUND_NR = "RoundNr";
    public static final String QUESTION_NR = "QuestionNr";
    public static final int START_OF_TEAMS = 3;
    //The other headers in the Scores tab are assumed to be team numbers

    @Override
    public CorrectionsList parse(JSONObject jo) throws JSONException {
        CorrectionsList correctionsList = new CorrectionsList(jo.getString(QUESTION_ID), jo.getInt(ROUND_NR), jo.getInt(QUESTION_NR));
        ArrayList<Correction> allCorrections = new ArrayList<>();
        boolean isCorrect, isCorrected;
        for (int i = 3; i < jo.length(); i++) {
            //Check if there is something filled out, if not, the question has not been corrected
            try {
                isCorrect = jo.getBoolean("" + (i - START_OF_TEAMS + 1));
                isCorrected = true;
            } catch (Exception e) {
                isCorrect = false;
                isCorrected = false;
            }
            String test = jo.getString("" + (i - START_OF_TEAMS + 1));
            allCorrections.add(i - START_OF_TEAMS, new Correction(isCorrect, isCorrected));
        }
        correctionsList.setAllCorrections(allCorrections);
        return correctionsList;
    }
}
