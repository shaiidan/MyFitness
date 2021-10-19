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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ExercisesFragment extends Fragment  {

    private Exercise.Type type; // to know how exercises show
    private long exerciseID; // to know how exercise replace
    private List<Exercise> exercises;
    private ExercisesListAdapter exercisesListAdapter;
    ListView exercisesList;

    // DB
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    //view
    private ImageView imvExit;

    public ExercisesFragment(){
        type = null;
        exerciseID = -1;
        exercises = new LinkedList<Exercise>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_exercises, container, false);

        if(exerciseID == -1 || type == null){
            goBackToUserHomeFragment();
            return v;
        }

        // DB
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // get user
        mDatabase = FirebaseDatabase.getInstance().getReference(); // get DB
        mDatabase.keepSynced(true);

        // invisible menu
        BottomNavigationView btnNavigationView = getActivity().findViewById(R.id.user_menu);
        btnNavigationView.setVisibility(View.INVISIBLE); // invisible user menu

        // list of exercises
        exercisesList = v.findViewById(R.id.exercisesFragment_listView);
        exercisesListAdapter = new ExercisesListAdapter();
        exercisesList.setAdapter(exercisesListAdapter);

        //exit image
        imvExit = v.findViewById(R.id.exercisesFragment_ic_exit);
        imvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // back to user in activity
                Intent i = new Intent(getActivity(),UserInActivity.class);
                startActivity(i);
                requireActivity().finish(); // finish the activity and go to user in activit
            }
        });
        // selected
        exercisesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Exercise exercise = null;
                try{
                    exercise = (Exercise) exercisesListAdapter.getItem (position);
                }
                catch (Exception e){
                    Log.d("fitness","error casting ");
                    ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                            getContext(),
                            getActivity(),
                            getString(R.string.errorMessageDialog_error_message),
                            getString(R.string.errorMessageDialog_go_back),
                            ErrorMessageDialog.ErrorMessageType.USER_HOME);
                    errorMessageDialog.show();
                }

                if(exercise != null){
                    editExerciseInDB(exercise); // save edit in DB and go home page user
                }
                else{// message error
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
        getExercisesByType(); // upload exercises from DB

        return v;
    }

    // for get type
    public Exercise.Type getType() {
        return type;
    }
    // for set type
    public void setType(Exercise.Type type) {
        this.type = type;
    }

    // for get exerciseID to remove
    public long getExerciseID() {
        return exerciseID;
    }
    // for set exerciseID to remove
    public void setExerciseID(long exerciseID) {
        this.exerciseID = exerciseID;
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
    // get exercises by type from DB
    private void getExercisesByType(){
        mDatabase.child("Exercises").orderByChild("type").equalTo(type.name()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            ArrayList<String> exerciseName = new ArrayList<String>();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Exercise exercise = null;
                                try{
                                    exercise = dataSnapshot.getValue(Exercise.class);
                                }
                                catch (Exception e){
                                    Log.d("fitness","error casting ");
                                    ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                            getContext(),
                                            getActivity(),
                                            getString(R.string.errorMessageDialog_error_message),
                                            getString(R.string.errorMessageDialog_go_back),
                                            ErrorMessageDialog.ErrorMessageType.USER_HOME);
                                    errorMessageDialog.show();
                                }
                                if (exercise != null) {
                                    if(!exercises.contains(exercise)){ // the exercise name is not exist in array
                                        exercise.setExerciseID(Long.parseLong(dataSnapshot.getKey()));
                                        exercises.add(exercise);
                                        exercisesListAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            Log.d("fitness","Exercises number = " + Integer.toString(exercises.size()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // error
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

    // edit exercises in the DB
    private void editExerciseInDB(Exercise exercise){
        if(exercise != null){
            // search the exercise key to update
            mDatabase.child("TrainingPlan").child(currentUser.getUid()).child("Exercises")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    if (dataSnapshot.getValue().toString().equals(Long.toString(exerciseID))) {
                                        mDatabase.child("TrainingPlan").child(currentUser.getUid()).child("Exercises")
                                                .child(dataSnapshot.getKey()).setValue(exercise.getExerciseID())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    // edit success !!
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d("fitness", "Edit exercise successfully");
                                                        goBackToUserHomeFragment(); // go to home page user
                                                    }
                                                });
                                    }
                                }
                            } else {//value does not exists go to home page use
                                ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                        getContext(),
                                        getActivity(),
                                        getString(R.string.errorMessageDialog_no_training_plan1),
                                        getString(R.string.errorMessageDialog_no_training_plan2),
                                        ErrorMessageDialog.ErrorMessageType.USER_HOME);
                                errorMessageDialog.show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
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
    }


    class ExercisesListAdapter extends BaseAdapter  {

        @Override
        public int getCount() {
            return exercises.size();
        }

        @Override
        public Object getItem(int i) {
            return exercises.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            // show view by list-view_exercise layout
            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.list_view_exercise, null);
            }

            // update the view by exercises list
            TextView tvExerciseTitle = view.findViewById(R.id.listView_exercise_title);
            ImageView imvExercise = view.findViewById(R.id.listView_exercise_image);
            Exercise exercise = exercises.get(i);

            if( exercise != null){
                tvExerciseTitle.setText(exercise.getExerciseName());
                Uri uri = Uri.parse("android.resource://"+getContext().getPackageName()+"/drawable/" +exercise.getPicName());
                imvExercise.setImageURI(uri);
            }
            return view;
        }
    }
}// end class ExercisesFragment