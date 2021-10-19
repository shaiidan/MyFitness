/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myfitness.Model.Exercise;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class FitShowFragment extends Fragment implements View.OnClickListener{

    // for authentication and DB
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private List<String> exercisesIDTrainingPlan;
    private int exerciseIDTrainingPlanPosition;
    private long exerciseSetQuantityNow, exerciseSetQuantityMax;
    private long exerciseTimerMax ;
    private ExerciseTimer exerciseTimer;
    private String timerFinishMessage;
    private boolean isTrainingPlanDone = false;

    //view
    TextView tvExerciseName, tvExerciseSetNumber, tvTimer;
    ImageView imvExercise, imvExit;
    Button btnNext,btnStart, btnStop;

    public FitShowFragment(){
        exercisesIDTrainingPlan = new LinkedList<String>();
        exerciseIDTrainingPlanPosition = 0 ; // position from linedList exercisesID
        exerciseSetQuantityNow = 1; // number of set show now
        exerciseSetQuantityMax = 1; // number of set we need to show
        exerciseTimerMax = 0; // start the timer
        timerFinishMessage = "";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fit_show, container, false);

        //DB
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser(); // get user
        mDatabase.keepSynced(true);

        // invisible menu
        BottomNavigationView btnNavigationView = getActivity().findViewById(R.id.user_menu);
        btnNavigationView.setVisibility(View.INVISIBLE);

        //view
        imvExit = view.findViewById(R.id.fitShowFragment_ic_exit);
        imvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // back to user in activity
                Intent i = new Intent(getActivity(),UserInActivity.class);
                startActivity(i);
                requireActivity().finish(); // finish the activity and go to user in activity
            }
        });
        tvExerciseName = view.findViewById(R.id.fitShowFragment_exercise_name);
        tvExerciseSetNumber = view.findViewById(R.id.fitShowFragment_exercise_set_number);
        tvTimer = view.findViewById(R.id.fitShowFragment_start_timer);
        btnNext = view.findViewById(R.id.fitShowFragment_btn_next);
        btnStop = view.findViewById(R.id.fitShowFragment_btn_stop);
        btnStart = view.findViewById(R.id.fitShowFragment_btn_start);
        btnStart.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        imvExercise = view.findViewById(R.id.fitShowFragment_start_exercise_image);

        btnNext.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);

        // start
        getTrainingPlan();


        return view;
    }

    @Override
    public void onClick(View view) {
        if(view == btnNext){

            // finish the go to fit fragment
            if(isTrainingPlanDone){
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container_with_menu, new FitFragment()).commit();
            }
            //cancel old timer
            exerciseSetQuantityNow++; // the next set quantity

            // show the next set
            if(exerciseSetQuantityNow <= exerciseSetQuantityMax){
                // the next set quantity in the same exercise
                String setNumber = exerciseSetQuantityNow + "/" + exerciseSetQuantityMax;
                tvExerciseSetNumber.setText(setNumber);
                if(exerciseTimer != null){
                    tvTimer.setText(Long.toString(exerciseTimerMax)); // go to start
                    exerciseTimer.resetTimer();
                }
            }
            // the all set is done change exercise
            else if(exerciseSetQuantityNow > exerciseSetQuantityMax) {
                exerciseSetQuantityNow = 1; //start new exercise so start 1 again
                exerciseSetQuantityMax = 1;
                exerciseIDTrainingPlanPosition++; // the next exercise in the list
                exerciseTimer = null;
                startShowTrainingPlan();
            }
            btnNext.setEnabled(true);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
        else if(view == btnStart){
            if(exerciseTimer != null){
                exerciseTimer.startTimer();

            }
        }
        else if(view == btnStop){
            //stop the timer
            if(exerciseTimer != null){
                exerciseTimer.pauseTimer();
            }
        }
    }

    // get training plan from DB, return exercises ID
    private void getTrainingPlan() {
        mDatabase.child("TrainingPlan").child(currentUser.getUid()).child("Exercises")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // get exercises details
                            exercisesIDTrainingPlan.add(dataSnapshot.getValue().toString());
                        }
                        startShowTrainingPlan(); // start show exercise
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // show trainingPlan by call function to get exercise details
    private void startShowTrainingPlan(){

        // to user don't have training plan yet !!!
        if(exercisesIDTrainingPlan.isEmpty()) {
            //  show error and go back to user home page
            btnNext.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
           ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                   getContext(),
                   getActivity(),
                   getString(R.string.errorMessageDialog_no_training_plan1),
                   getString(R.string.errorMessageDialog_no_training_plan2),
                   ErrorMessageDialog.ErrorMessageType.USER_HOME);
           errorMessageDialog.show();
        }
        else{ // next exercise to show
            if(exerciseIDTrainingPlanPosition < exercisesIDTrainingPlan.size()){
                getExerciseDetails(exercisesIDTrainingPlan.get(exerciseIDTrainingPlanPosition));
            }
			// finish training plan
			else{
				tvTimer.setText(R.string.fitShowFragment_training_plan_done);
				btnNext.setText(R.string.finish);
				btnStart.setVisibility(View.GONE);
				btnStop.setVisibility(View.GONE);
				imvExercise.setVisibility(View.GONE);
                tvExerciseName.setVisibility(View.GONE);
                tvExerciseSetNumber.setVisibility(View.GONE);
                isTrainingPlanDone = true;
			}
        }
    }

    // get exercise details from DB
    private void getExerciseDetails(String exerciseID){
        mDatabase.child("Exercises").child(exerciseID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Exercise exercise = null;
                        try{
                            exercise = snapshot.getValue(Exercise.class);
                        }
                        catch (Exception e){
                            Log.d("fitness",e.getMessage());
                        }
                        if(exercise != null){
                            exercise.setExerciseID(Long.parseLong(exerciseID));
                            showExerciseInFirstTime(exercise);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // show exercise on the screen
    private void showExerciseInFirstTime(Exercise exercise){
        if(exercise != null){
            // set exercise in first time on screen
            exerciseSetQuantityMax = exercise.getSetQuantity();
            Uri uri = Uri.parse("android.resource://"+getContext().getPackageName()+"/drawable/" +exercise.getPicName());
            imvExercise.setImageURI(uri);
            String setNumber = exerciseSetQuantityNow + "/" + exerciseSetQuantityMax;
            tvExerciseSetNumber.setText(setNumber);
            tvExerciseName.setText(exercise.getExerciseName());

            // for timer!!
            // number of time in sec no quantity
             timerFinishMessage = getString(R.string.finish) + " " ;
            if(exercise.getTime() != -1){
                exerciseTimerMax = exercise.getTime();
                timerFinishMessage += exerciseTimerMax + " " + getString(R.string.second);
                tvTimer.setText(Long.toString(exercise.getTime()));

             }
            // number of quantity no time
            else if(exercise.getQuantity() != -1){
                exerciseTimerMax = exercise.getQuantity();
                timerFinishMessage += + exerciseTimerMax + " " + getString(R.string.reps);
                tvTimer.setText(Long.toString(exercise.getQuantity()));
            }

            //start new timer
            exerciseTimer = new ExerciseTimer(exerciseTimerMax*1000,tvTimer,timerFinishMessage,btnNext,btnStart,btnStop);
        }
    }
}
