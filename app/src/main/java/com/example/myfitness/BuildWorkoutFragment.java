/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myfitness.Model.Exercise;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BuildWorkoutFragment extends Fragment {

    //DB
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    private Exercise.Difficulty level; // for check difficult level
    private List<Long> trainingPlanExercises; // exerciseID for all exercises the user choose
    private Map<String,Integer> exerciseNumberByType; // build dictionary of <type name, number of exercise to this type>

    //view
    NumberPicker npBack, npChest, npBelly, npHands, npLegs;
    RadioGroup radioGroup;
    Button btnBuild;
    ImageView imvExit;

    public BuildWorkoutFragment() {
        // Required empty public constructor
        trainingPlanExercises = new LinkedList<Long>();
        exerciseNumberByType = new HashMap<>();
        level = Exercise.Difficulty.E; // for default
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_build_workout, container, false);

        // DB
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // get user
        mDatabase = FirebaseDatabase.getInstance().getReference(); // get DB
        mDatabase.keepSynced(true);
        // start view
        radioGroup = view.findViewById(R.id.buildWorkoutFragment_level);
        npBack = view.findViewById(R.id.buildWorkoutFragment_back_selected);
        npBelly = view.findViewById(R.id.buildWorkoutFragment_belly_selected);
        npChest = view.findViewById(R.id.buildWorkoutFragment_chest_selected);
        npHands = view.findViewById(R.id.buildWorkoutFragment_hands_selected);
        npLegs = view.findViewById(R.id.buildWorkoutFragment_legs_selected);
        btnBuild = view.findViewById(R.id.buildWorkoutFragment_btn_build);

        //exit
        imvExit = view.findViewById(R.id.buildWorkoutFragment_ic_exit);
        imvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // back to user in activity
                Intent i = new Intent(getActivity(),UserInActivity.class);
                startActivity(i);
                requireActivity().finish(); // finish the activity and go to user in activity
            }
        });

        // build to training plan
        btnBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildTrainingPlan(); // start build the training plan
            }
        });
        radioGroup.check(R.id.buildWorkoutFragment_level_easy); // check default

        // select a level
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                if(checkedID == R.id.buildWorkoutFragment_level_easy){
                    level = Exercise.Difficulty.E;
                }
                else if(checkedID == R.id.buildWorkoutFragment_level_medium){
                    level = Exercise.Difficulty.M;
                }
                else if(checkedID == R.id.buildWorkoutFragment_level_hard){
                    level = Exercise.Difficulty.H;
                }
            }
        });

        // invisible menu
        BottomNavigationView btnNavigationView = getActivity().findViewById(R.id.user_menu);
        btnNavigationView.setVisibility(View.INVISIBLE); // invisible user menu

        return view;
    }

    private void buildTrainingPlan(){
        // build his select number of exercises for each type by input from the user
        exerciseNumberByType.put(Exercise.Type.Back.name(),npBack.getValue());
        exerciseNumberByType.put(Exercise.Type.Chest.name(),npChest.getValue());
        exerciseNumberByType.put(Exercise.Type.Legs.name(),npLegs.getValue());
        exerciseNumberByType.put(Exercise.Type.Hands_Shoulders.name(),npHands.getValue());
        exerciseNumberByType.put(Exercise.Type.Belly.name(),npBelly.getValue());

        // get the exercises from DB for this difficult level
        mDatabase.child("Exercises").orderByChild("difficulty").equalTo(level.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Exercise exercise = null;
                            try{
                                exercise = dataSnapshot.getValue(Exercise.class); // get exercises
                            }
                            catch (Exception e){
                                Log.d("fitness",e.getMessage());
                                ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                        getContext(),
                                        getActivity(),
                                        getString(R.string.errorMessageDialog_error_message),
                                        getString(R.string.errorMessageDialog_go_back),
                                        ErrorMessageDialog.ErrorMessageType.USER_HOME);
                                errorMessageDialog.show();
                            }
                            if(exercise != null){
                                exercise.setExerciseID(Long.parseLong(dataSnapshot.getKey())); // get exercisesID
                                int count = exerciseNumberByType.get(exercise.getType()); // get how much number for this type
                                if(count > 0){ // add if count > 0
                                    trainingPlanExercises.add(exercise.getExerciseID());
                                    count--; // change the number
                                    exerciseNumberByType.put(exercise.getType(),count);
                                }
                            }
                        }
                        saveTrainingPlanToDB(); // after finish to build - save to DB
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // DB wrong
                        ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                getContext(),
                                getActivity(),
                                getString(R.string.errorMessageDialog_error_message),
                                getString(R.string.errorMessageDialog_go_back),
                                ErrorMessageDialog.ErrorMessageType.FINISH);
                        errorMessageDialog.show();
                    }
                });
    }

    // save training plan to DB
    private void saveTrainingPlanToDB() {
        if (trainingPlanExercises.size() > 0) { // select all number of exercises > 0
            Map<String, Object> exercises = new HashMap<>();
            // save all exercisesID
            for (int i = 0; i < trainingPlanExercises.size(); i++) {
                exercises.put(Integer.toString(i), trainingPlanExercises.get(i));
            }

            // save on DB
            mDatabase.child("TrainingPlan").child(currentUser.getUid()).child("Exercises").updateChildren(exercises)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("fitness", "Add training plan successfully.");
                                goBackToUserHomeFragment(); // go back
                            } else { // DB wrong
                                ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                        getContext(),
                                        getActivity(),
                                        getString(R.string.errorMessageDialog_error_message),
                                        getString(R.string.errorMessageDialog_go_back),
                                        ErrorMessageDialog.ErrorMessageType.FINISH);
                                errorMessageDialog.show();
                            }
                        }
                    });
        }
    }

    // go to home user
    public void goBackToUserHomeFragment(){
        // delete this fragment from back stack
        getActivity().getSupportFragmentManager()
                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // go back to home fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container_with_menu, new UserHomeFragment())
                .commit();
    }

}