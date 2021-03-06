package com.paperlessquiz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paperlessquiz.adapters.DisplayAnswersAdapter;
import com.paperlessquiz.quiz.Quiz;
import com.paperlessquiz.webrequest.EventLogger;
import com.paperlessquiz.loadinglisteners.LoadingActivity;
import com.paperlessquiz.quiz.QuizDatabase;
import com.paperlessquiz.quiz.QuizLoader;
import com.paperlessquiz.quiz.Round;
import com.paperlessquiz.users.User;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Home screen for teams. Contains a round spinner and a question spinner + fields that allow to answer the question that is currently selected
 * Answers are automatically submitted as soon as you navigate to the next question or round.
 * Parts of the screen are hidden based on round status.
 * Answers are sent to the central db when spinning through the questions
 * Info is refreshed from the central db when spinning through the rounds
 */
public class C_ParticipantHome extends MyActivity implements LoadingActivity, FragSpinner.HasSpinner,
        FragRoundSpinner.HasRoundSpinner, FragShowRoundScore.HasShowRoundScore, FragExplainRoundStatus.HasExplainRoundStatus {

    int thisTeamNr;
    User thisTeam;
    FragRoundSpinner roundSpinner;
    FragSpinner questionSpinner;
    FragShowRoundScore roundResultFrag;
    FragExplainRoundStatus explainRoundStatus;
    TextView tvDisplayRoundResults;
    EditText etAnswer;
    ImageView ivChangeTextSize, ivQuestionImage;
    LinearLayout displayAnswersLayout, editAnswerLayout;
    RecyclerView rvDisplayAnswers;
    DisplayAnswersAdapter displayAnswersAdapter;
    RecyclerView.LayoutManager layoutManager;
    String roundStatusExplanation;
    Date lastPausedDate;
    QuizLoader quizLoader;
    boolean roundsLoaded, answersLoaded, scoresLoaded, answersSubmitted;
    boolean activityBeingCreated = true;

    @Override
    public void loadingComplete(int requestID) {
        switch (requestID) {
            case QuizDatabase.REQUEST_ID_GET_ROUNDS:
                roundsLoaded = true;
                break;
            case QuizDatabase.REQUEST_ID_GET_ANSWERS:
                answersLoaded = true;
                break;
            case QuizDatabase.REQUEST_ID_GET_SCORES:
                scoresLoaded = true;
                break;
            case QuizDatabase.REQUEST_ID_SETANSWERSSUBMITTED:
                answersSubmitted = true;
                break;
            case QuizDatabase.REQUEST_ID_SUBMITREMARK:
                //Nothing to do here
                break;
            //This is the id of a question for which an answer was submitted
            default:
                //Determine the round nr and question number from the id
                //Check that this is indeed a question ID
                if (requestID > QuizDatabase.REQUEST_ID_LIMIT) {
                    int questionNr = (requestID % QuizDatabase.CALC_QUESTION_FACTOR);
                    int roundNr = ((requestID / QuizDatabase.CALC_QUESTION_FACTOR) % QuizDatabase.CALC_ROUND_FACTOR);
                    if (quizLoader.submitAnswerRequest.isRequestOK()) {
                        //Set the submitted status to true
                        thisQuiz.setAnswerForTeamSubmitted(roundNr, questionNr, thisTeamNr);
                        refreshAnswers();
                    }
                }

        }
        //If everything is properly loaded, we can start populating the central Quiz object
        if (roundsLoaded && answersLoaded && scoresLoaded) {
            //reset the loading statuses
            roundsLoaded = false;
            answersLoaded = false;
            scoresLoaded = false;
            //14/3/2020: Check if actions runs correctly before proceeding - display a message if not
            //TODO: normally, these checks can be removed again...
            if (quizLoader.updateRoundsIntoQuiz()) {
                if (quizLoader.updateAnswersIntoQuiz()) {
                    if (quizLoader.loadResultsIntoQuiz()) {
                        //roundSpinner.refreshIcons();
                        //roundResultFrag.refresh();  //This will recalculate scores based on the re-loaded corrections
                        //refreshDisplayFragments();  //Display the correct fragments based on new round status etc.
                    } else {
                        Toast.makeText(this, "MAIN: Error updating RESULTS into quiz - please try again", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "MAIN: Error updating ANSWERS into quiz - please try again", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "MAIN: Error updating ROUNDS into quiz - please try again", Toast.LENGTH_LONG).show();
            }
            //Do these actions anyway, even if loading was not successful - in that case, we display based on the old information
            //TODO: check why app crashes when calling this twice in a row
            roundSpinner.refreshIcons();
            roundResultFrag.refresh();  //This will recalculate scores based on the re-loaded corrections
            refreshDisplayFragments();  //Display the correct fragments based on new round status etc.
        }
        if (answersSubmitted) {
            answersSubmitted = false;
            Toast.makeText(this, this.getString(R.string.part_answerssubmitted), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuiz() {
        //Update 14/3/2020: reset  the loading statuses here to make sure they are in the correct status in case previous requests did not complete as expected
        roundsLoaded = false;
        answersLoaded = false;
        scoresLoaded = false;
        quizLoader.loadRounds(roundSpinner.getPosition());
        quizLoader.loadMyAnswers(roundSpinner.getPosition());
        quizLoader.loadScoresAndStandings(roundSpinner.getPosition());
        //The rest is done when loading is complete
    }

    @Override
    //This is called from the round spinner after it was changed.
    public void onRoundChanged(int oldRoundNr, int roundNr) {
        //Similar as with a questionSpinner change, we save the answer that we have and load the new answer - only do this if the field was visible
        //Update 11/3/2020: also set the type of the keyboard correct
        //Update 14/3/2020: check on status of the old round instead of visibility of something
        if (thisQuiz.getRound(oldRoundNr).getRoundStatus() == QuizDatabase.ROUNDSTATUS_OPENFORANSWERS) {
            //Update the answer for the previous round if it was changed - just in case the user entered an answer and then navigate to another round directly
            //If oldRoundNr = roundNr, we don't want to do this - this is when we initialize things
            if (!(oldRoundNr == 1 & roundNr == 1)) {
                String newAnswer = etAnswer.getText().toString().trim();
                updateAnswerIfChanged(newAnswer, oldRoundNr, questionSpinner.getPosition(), thisTeamNr);
            }
            /*
            if (!(oldAnswer.equals(newAnswer))) {
                int questionID = thisQuiz.getQuestionID(oldRoundNr, questionSpinner.getPosition());
                thisQuiz.setAnswerForTeam(oldRoundNr, questionSpinner.getPosition(), thisTeamNr, newAnswer);
                //Store the answer in the central quiz db
                quizLoader.submitAnswer(questionID, newAnswer);
            }
            */
        }
        if (thisQuiz.getRound(roundNr).getRoundStatus() == QuizDatabase.ROUNDSTATUS_OPENFORANSWERS) {
            //Set the keyboard type correct for the first question of this round
            if ((thisQuiz.getQuestion(roundSpinner.getPosition(), 1).getQuestionType() == QuizDatabase.QUESTIONTYPE_SCHIFTING) |
                    thisQuiz.getQuestion(roundSpinner.getPosition(), 1).getQuestionType() == QuizDatabase.QUESTIONTYPE_NUMERIC) {
                etAnswer.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                etAnswer.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        }
        //Set the value of the answer for the new question to what we already have in the central Quiz object
        //Move the QuestionSpinner to position 1 to make sure we have something at that position
        questionSpinner.moveToFirstPos();
        etAnswer.setText(thisQuiz.getAnswerForTeam(roundNr, 1, thisTeamNr).getTheAnswer());
        //Dismiss the keyboard if its there
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etAnswer.getWindowToken(), 0);
        //Update info from the central database // if the activity is being created, just refresh the screens
        if (!activityBeingCreated) {
            updateQuiz();
            //Rest is done when loading is complete
        } else {
            activityBeingCreated = false;
            roundResultFrag.refresh();
            refreshDisplayFragments();
        }
    }

    @Override
    //This is called from the Question spinner when it is changed
    public void onSpinnerChange(int oldPos, int newPos) {
        //Update the answer if it was changed
        //If oldPos = newPos = 1, then we don't need to do this?
        if (!(oldPos == 1 & newPos == 1)) {
            String newAnswer = etAnswer.getText().toString().trim();
            updateAnswerIfChanged(newAnswer, roundSpinner.getPosition(), oldPos, thisTeamNr);
        }
        /*
        //If this is a normal question, convert the answer to uppercase
        if (thisQuiz.getQuestion(roundSpinner.getPosition(), oldPos).getQuestionType() == QuizDatabase.QUESTIONTYPE_NORMAL) {
            newAnswer = newAnswer.toUpperCase();
        }
        //If the new answer is blanc, replace it by a "-"
        if (newAnswer.equals("")) {
            newAnswer = QuizDatabase.BLANC_ANSWER;
        }
        int questionID = thisQuiz.getQuestionID(roundSpinner.getPosition(), oldPos);
        if (!(oldAnswer.equals(newAnswer))) {
            //This action also sets the submitted status to False
            thisQuiz.setAnswerForTeam(roundSpinner.getPosition(), oldPos, thisTeamNr, newAnswer);
        }
        //If the answer is not yet submitted, submit it now
        if (!(thisQuiz.isAnswerSubmitted(roundSpinner.getPosition(), oldPos, thisTeamNr))) {
            quizLoader.submitAnswer(questionID, newAnswer);
        }
        */
        //If this is a schiftingsQuestion or numeric question, we only want numeric answers
        if ((thisQuiz.getQuestion(roundSpinner.getPosition(), newPos).getQuestionType() == QuizDatabase.QUESTIONTYPE_SCHIFTING) |
                thisQuiz.getQuestion(roundSpinner.getPosition(), newPos).getQuestionType() == QuizDatabase.QUESTIONTYPE_NUMERIC) {
            etAnswer.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            etAnswer.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        //Set the value of the answer for the new question to what we already have in the Quiz object
        //If this value  = "-", then set the value to a blanc
        if (thisQuiz.getAnswerForTeam(roundSpinner.getPosition(), newPos, thisTeamNr).getTheAnswer().equals(QuizDatabase.BLANC_ANSWER)) {
            etAnswer.setText("");
        } else {
            etAnswer.setText(thisQuiz.getAnswerForTeam(roundSpinner.getPosition(), newPos, thisTeamNr).getTheAnswer());
        }
        //Dismiss the keyboard if its there
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etAnswer.getWindowToken(), 0);
        refreshAnswers(); //refresh the field that shows all answers already given for this round
        showImageLink();
    }

    //This method updates the answer to question questionNr of roundn roundNr for team teamNr if it was changed
    public void updateAnswerIfChanged(String newAnswer, int roundNr, int questionNr, int teamNr) {
        String oldAnswer = thisQuiz.getAnswerForTeam(roundNr, questionNr, teamNr).getTheAnswer();
        //If this is not a case-sensitive question, convert the answer to uppercase
        if (!(thisQuiz.getQuestion(roundNr, questionNr).getQuestionType() == QuizDatabase.QUESTIONTYPE_CASESENSITIVE)) {
            newAnswer = newAnswer.toUpperCase();
        }
        //If the new answer is blanc, replace it by a "-"
        if (newAnswer.equals("")) {
            newAnswer = QuizDatabase.BLANC_ANSWER;
        }
        int questionID = thisQuiz.getQuestionID(roundNr, questionNr);
        if (!(oldAnswer.equals(newAnswer))) {
            //This action also sets the submitted status to False
            thisQuiz.setAnswerForTeam(roundNr, questionNr, teamNr, newAnswer);
        }
        //If the answer is not yet submitted, submit it now
        if (!(thisQuiz.isAnswerSubmitted(roundNr, questionNr, teamNr))) {
            quizLoader.submitAnswer(questionID, newAnswer);
        }
    }


    @Override
    public int getSizeOfSpinnerArray() {
        return thisQuiz.getRound(roundSpinner.getPosition()).getQuestions().size();
    }

    @Override
    public String getValueToSetForPrimaryField(int newPos) {
        return thisQuiz.getQuestion(roundSpinner.getPosition(), newPos).getQuestionNr() + ". " + thisQuiz.getQuestion(roundSpinner.getPosition(), newPos).getName();
    }

    @Override
    public String getValueToSetForSecondaryField(int newPos) {
        return "(" + thisQuiz.getQuestion(roundSpinner.getPosition(), newPos).getHint() + ")";
    }
    //End stuff for the QuestionSpinner

    private void refreshAnswers() {
        //Update the displayed answers for this round as needed
        displayAnswersAdapter.setAnswers(thisQuiz.getRound(roundSpinner.getPosition()).getQuestions());
        rvDisplayAnswers.setAdapter(displayAnswersAdapter);
    }

    private void showImageLink(){
        //Show the imageView icon if the question is the right type
        if (thisQuiz.getQuestion(roundSpinner.getPosition(),questionSpinner.getPosition()).getQuestionType() == QuizDatabase.QUESTIONTYPE_HASIMAGE) {
            ivQuestionImage.setVisibility(View.VISIBLE);
        }
        else
        {
            ivQuestionImage.setVisibility(View.GONE);
        }
    }

    private void refreshDisplayFragments() {
        //Refresh what is in the display based on the current values of roundSpinner position
        Round thisRound = thisQuiz.getRound(roundSpinner.getPosition());
        switch (thisRound.getRoundStatus()) {
            case QuizDatabase.ROUNDSTATUS_CLOSED:
                roundStatusExplanation = C_ParticipantHome.this.getString(R.string.participant_waitforroundopen);
                explainRoundStatus.setStatus(roundStatusExplanation);
                toggleFragments(R.id.frPlaceHolder, explainRoundStatus, roundResultFrag, questionSpinner);
                editAnswerLayout.setVisibility((View.GONE));
                displayAnswersLayout.setVisibility(View.GONE);
                break;
            case QuizDatabase.ROUNDSTATUS_OPENFORANSWERS:
                //Round is open to enter answers
                //Show the questionSpinner
                toggleFragments(R.id.frPlaceHolder, questionSpinner, roundResultFrag, explainRoundStatus);
                //Show the layouts to edit and display answers
                editAnswerLayout.setVisibility(View.VISIBLE);
                displayAnswersLayout.setVisibility(View.VISIBLE);
                //etAnswer is by default invisible to avoid seeing the keyboard when you shouldn't
                etAnswer.setVisibility(View.VISIBLE);
                //Show the imageView icon if the question is the right type
                showImageLink();
                //Initialize etAnswer with the correct answer
                if (thisQuiz.getAnswerForTeam(roundSpinner.getPosition(), questionSpinner.getPosition(), thisTeamNr).getTheAnswer().equals(QuizDatabase.BLANC_ANSWER)) {
                    etAnswer.setText("");
                } else {
                    etAnswer.setText(thisQuiz.getAnswerForTeam(roundSpinner.getPosition(), questionSpinner.getPosition(), thisTeamNr).getTheAnswer());
                }
                break;
            case QuizDatabase.ROUNDSTATUS_OPENFORCORRECTIONS:
                //Round is closed for answering, but not yet corrected
                //Just display the fragment that tells you this
                roundStatusExplanation = C_ParticipantHome.this.getString(R.string.participant_waitforroundcorrection);
                explainRoundStatus.setStatus(roundStatusExplanation);
                toggleFragments(R.id.frPlaceHolder, explainRoundStatus, roundResultFrag, questionSpinner);
                editAnswerLayout.setVisibility((View.GONE));
                displayAnswersLayout.setVisibility(View.GONE);
                break;
            case QuizDatabase.ROUNDSTATUS_CORRECTED:
                //Show the RoundResults Fragment
                toggleFragments(R.id.frPlaceHolder, roundResultFrag, questionSpinner, explainRoundStatus);
                //Hide the layout to edit answers
                editAnswerLayout.setVisibility((View.GONE));
                displayAnswersLayout.setVisibility(View.VISIBLE);
                break;
        }
        refreshAnswers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.participant, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                updateQuiz();
                break;

            case R.id.messages:
                MyApplication.authorizedBreak = true;
                Intent intentHelp = new Intent(C_ParticipantHome.this, DisplayHelpTopics.class);
                intentHelp.putExtra(QuizDatabase.INTENT_EXTRANAME_HELPTYPE, QuizDatabase.HELPTYPE_QUIZQUESTION);
                startActivity(intentHelp);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_c_participant_home);
        //Get the round spinner fragment
        roundSpinner = (FragRoundSpinner) getSupportFragmentManager().findFragmentById(R.id.frRoundSpinner);
        //Create the other fragments that are needed here and set the action bar icon and title
        questionSpinner = new FragSpinner();
        roundResultFrag = new FragShowRoundScore();
        explainRoundStatus = new FragExplainRoundStatus();
        setActionBarIcon();
        setActionBarTitle();
        //Set LoggedIn to TRUE so we know this to track if the user pauses the app
        thisTeamNr = thisQuiz.getThisUser().getUserNr();
        MyApplication.setLoggedIn(true);
        //Get all the stuff from the layout
        displayAnswersLayout = findViewById(R.id.llDisplay);
        editAnswerLayout = findViewById(R.id.llAnswers);
        tvDisplayRoundResults = findViewById(R.id.tvDisplayRound);
        etAnswer = findViewById(R.id.etAnswer);
        ivChangeTextSize = findViewById(R.id.ivChangeTextSize);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        ivQuestionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog builder = new Dialog(C_ParticipantHome.this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });
                int a = view.getId();
                /*if (R.id.go_pro == a) {
                    uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/pro");    //path of image
                } else if (R.id.img_View == a) {
                    uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/profile"); //path of image
                }
                */
                ImageView imageView = new ImageView(C_ParticipantHome.this);
                String thisQuestionImageURL = QuizDatabase.QUESTIONIMAGES_ROOT + thisQuiz.getListData().getIdQuiz() + "/" +
                        thisQuiz.getQuestion(roundSpinner.getPosition(),questionSpinner.getPosition()).getIdQuestion() + QuizDatabase.QUESTIONIMAGES_EXTENTION;
                if (thisQuestionImageURL.equals("")) {
                    imageView.setImageResource(R.mipmap.placeholder);
                } else {
                    Picasso.with(C_ParticipantHome.this)
                            .load(thisQuestionImageURL)
                            .resize(10 * Quiz.TARGET_WIDTH, 10 * Quiz.TARGET_HEIGHT)
                            //.centerCrop()
                            //.fit()
                            .centerInside()
                            .into(imageView);
                }
                //imageView.setImageURI(uri);                //set the image in dialog popup
                //below code fullfil the requirement of xml layout file for dialoge popup
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.cancel();
                    }
                });
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                builder.show();
            }
    });
        ivChangeTextSize.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View view){
        displayAnswersAdapter.toggleTextSize();
    }
    });
    //btnSubmit = findViewById(R.id.btnSubmit);
    rvDisplayAnswers =

    findViewById(R.id.rvDisplayAnswers);
        rvDisplayAnswers.setHasFixedSize(true);
    layoutManager =new

    LinearLayoutManager(this);
        rvDisplayAnswers.setLayoutManager(layoutManager);
    //Initialize the adapter
    displayAnswersAdapter =new

    DisplayAnswersAdapter(this,thisQuiz.getRound(1).

    getQuestions(),thisTeamNr);
    quizLoader =new

    QuizLoader(C_ParticipantHome .this);

    thisTeam =thisQuiz.getThisUser();
        quizLoader.updateMyStatus(QuizDatabase.USERSTATUS_PRESENTLOGGEDIN);
    //When the RoundSpinner fragment is attached, it will call the onRoundChange method which will do the rest here.

}

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //displayPDF(thisQuiz.getListData().getQuizPdfURL(), C_ParticipantHome.this.getString(R.string.participant_aboutquiztitle));
    }

    @Override
    protected void onPause() {
        if (thisQuiz.isAnyRoundOpen() && !MyApplication.authorizedBreak) {
            lastPausedDate = new Date();
            quizLoader.createPauseEvent(QuizDatabase.TYPE_PAUSE_PAUSE, 0);
            MyApplication.setAppPaused(true);
            quizLoader.updateMyStatus(QuizDatabase.USERSTATUS_PRESENTNOTLOGGEDIN);
        }

        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (MyApplication.isAppPaused() & !(lastPausedDate == null)) {
            Date dateResumed = new Date();
            long timePaused = (dateResumed.getTime() - lastPausedDate.getTime()) / 1000;
            quizLoader.createPauseEvent(QuizDatabase.TYPE_PAUSE_RESUME, timePaused);
            MyApplication.setAppPaused(false);
        }
        quizLoader.updateMyStatus(QuizDatabase.USERSTATUS_PRESENTLOGGEDIN);
        MyApplication.authorizedBreak = false;
    }

    @Override
    public void onBackPressed() {
        if (false) {
            Toast.makeText(this, C_ParticipantHome.this.getString(R.string.participant_nobackallowed), Toast.LENGTH_SHORT).show();
            /*Intent intent = new Intent(C_ParticipantHome.this, A_Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            */
            //super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public String getRoundStatusExplanation() {
        return roundStatusExplanation;
    }

    @Override
    public int getRound() {
        return roundSpinner.getPosition();
    }

    @Override
    public int getTeam() {
        return thisTeamNr;
    }

}
