package com.paperlessquiz.users;

import com.paperlessquiz.MyApplication;
import com.paperlessquiz.quiz.QuizDatabase;

/**
 * Simple extention of the User class, represents an organizer
 */
public class Organizer extends User {
    //int organizerType,organizerNr;

    public Organizer(int idQuiz, int idUser, int userNr, int userType, int userStatus, String name) {
        super(idQuiz, idUser, userNr, userType, userStatus, name);
    }


    public Organizer(User user){
        super(user.getIdQuiz(), user.getIdUser(), user.getUserNr(), user.getUserType(), user.getUserStatus(), user.getName());
        switch (user.getUserType()) {
            case QuizDatabase.USERTYPE_QUIZMASTER:
                setDescription("Quizmaster");
                break;
            case QuizDatabase.USERTYPE_CORRECTOR:
                setDescription("Verbeteraar");
                break;
            case QuizDatabase.USERTYPE_RECEPTIONIST:
                setDescription("Receptionist");
                break;
            case QuizDatabase.USERTYPE_JUROR:
                setDescription("Juryvoorzitter");
                break;
            default:
                setDescription("Organizator");
        }
    }

    //20190728 - Create a dummy organizer with the nr given
    public Organizer(int organizerNr){
        super(MyApplication.theQuiz.getListData().getIdQuiz(),0,organizerNr, QuizDatabase.USERTYPE_RECEPTIONIST,QuizDatabase.USERSTATUS_NOTPRESENT,"EMPTY");
    }


/*
    public int getOrganizerType() {
        return organizerType;
    }

    public int getOrganizerNr() {
        return organizerNr;
    }
    */
}
