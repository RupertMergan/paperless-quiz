package com.paperlessquiz.question;

import com.paperlessquiz.parsers.JsonParser;
import com.paperlessquiz.quiz.QuizGenerator;

import org.json.JSONException;
import org.json.JSONObject;

public class QuestionParser implements JsonParser<Question> {
//Headers are taken from the QuizGenerator class and must match those of course

    @Override
    public Question parse(JSONObject jo) throws JSONException {
        try {
            return new Question(jo.getString(QuizGenerator.QUESTION_ID), jo.getInt(QuizGenerator.QUESTION_NR), jo.getInt(QuizGenerator.ROUND_NR), jo.getString(QuizGenerator.QUESTION_NAME),
                    jo.getString(QuizGenerator.QUESTION_HINT), jo.getString(QuizGenerator.QUESTION_FULL), jo.getString(QuizGenerator.QUESTION_CORRECT_ANSWER),
                    jo.getInt(QuizGenerator.QUESTION_MAX_SCORE));
        } catch (Exception e) {
            return new Question(jo.getString(QuizGenerator.QUESTION_ID), jo.getInt(QuizGenerator.QUESTION_NR), jo.getInt(QuizGenerator.ROUND_NR), jo.getString(QuizGenerator.QUESTION_NAME),
                    jo.getString(QuizGenerator.QUESTION_HINT), jo.getString(QuizGenerator.QUESTION_FULL), jo.getString(QuizGenerator.QUESTION_CORRECT_ANSWER),
                    0);
        }
    }
}
