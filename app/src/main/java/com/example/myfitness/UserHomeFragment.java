/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class UserHomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private TrainingPlanAdapter trainingPlanAdapter;
    private List<Exercise> exercisesTrainingPlan;
    private SharedPreferences sp;

    // view
    TextView tvSelectAlertTime, tvSelectAlertTimeValue,tvBuildWorkout,tvNoWorkout;
    ListView lvWorkout;

    public UserHomeFragment(){

        exercisesTrainingPlan = new LinkedList<Exercise>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // get user
        mDatabase = FirebaseDatabase.getInstance().getReference(); // get DB
        mDatabase.keepSynced(true);

        sp = getContext().getSharedPreferences("user",0); // for time notification next workout

        // show the user name
        TextView tvUserName = v.findViewById(R.id.userHomeFragment_userName_title);
        String userName = getString(R.string.hello) + " " + currentUser.getDisplayName();
        tvUserName.setText(userName);

        tvBuildWorkout = v.findViewById(R.id.userHomeFragment_build_workout);
        tvNoWorkout = v.findViewById(R.id.userHomeFragment_no_workout);
        tvSelectAlertTime = v.findViewById(R.id.userHomeFragment_select_alert_title);
        // if was empty workout
        tvSelectAlertTime.setVisibility(View.VISIBLE); // if was invisible
        tvNoWorkout.setVisibility(View.INVISIBLE);

        tvSelectAlertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open choose time and start the alert manager to notification in this time
                selectTimeAndNotification();
            }
        });
        tvBuildWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBuildWorkoutFragment();
            }
        });
        tvSelectAlertTimeValue = v.findViewById(R.id.userHomeFragment_select_alert_time);
        lvWorkout = v.findViewById(R.id.userHomeFragment_scrollView_workout); // list of training plan
        trainingPlanAdapter = new TrainingPlanAdapter();
        lvWorkout.setAdapter(trainingPlanAdapter); // save training plan to list view

        // visible menu
        BottomNavigationView btnNavigationView = getActivity().findViewById(R.id.user_menu);
        btnNavigationView.setVisibility(View.VISIBLE);

        getTrainingPlanByUserID(); // get training plan

        // connect to sp for message
        if(sp != null) {
            // get notification for this user !!
            String strMessage = sp.getString("message_" +currentUser.getUid(), null);
            long timeInMills = sp.getLong("clock_" + currentUser.getUid(),0);
            if (strMessage != null && timeInMills != 0) {
                // the notification still exist, the time no pass yet
                if(Calendar.getInstance().getTimeInMillis() < timeInMills){
                    tvSelectAlertTimeValue.setText(strMessage); // show save message
                }
            }
        }
            return v;
    }

    // select time and add to notification in next workout that user choose
    private void selectTimeAndNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View alertDialog = getLayoutInflater().inflate(R.layout.dialog_alert_time, null);
        builder.setView(alertDialog);
        TextView tvMessageError = alertDialog.findViewById(R.id.dialog_alert_time_message_error);
        TimePicker timePicker = alertDialog.findViewById(R.id.dialogAlertTime_select_time);
        timePicker.setIs24HourView(true);
        DatePicker datePicker = alertDialog.findViewById(R.id.dialogAlertTime_select_date);

        // message error invisibility
        tvMessageError.setVisibility(View.INVISIBLE);

        // minimum time is tomorrow
        Calendar timeNow = Calendar.getInstance();
        datePicker.setMinDate(timeNow.getTime().getTime());

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("CANCEL", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // set alert manager - notification
                Calendar timeSelected = Calendar.getInstance();
                timeSelected.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getHour(), timePicker.getMinute(), 0);

                if(timeSelected.before(timeNow) || timeSelected.equals(timeNow)){ // time selected not good
                    tvMessageError.setVisibility(View.VISIBLE);
                }
                else{ // time selected is good
                    // for message in user home page
                    String time = timePicker.getHour() + ":" + timePicker.getMinute();
                    String date = datePicker.getDayOfMonth() + "." + (datePicker.getMonth() + 1) +
                            "." + datePicker.getYear();
                    String message = getString(R.string.userHomeFragment_your_alert) +
                            " " + time + " " + getString(R.string.on) + " " + date;
                    tvSelectAlertTimeValue.setText(message);

                    // save message to sp
                    if(sp != null){
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("message_" +currentUser.getUid(), message);
                        editor.putLong("clock_" + currentUser.getUid(),timeSelected.getTimeInMillis());
                        editor.apply();
                    }
                    AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getContext(), AlertNextFit.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeSelected.getTimeInMillis(), pendingIntent);
                    Log.d("fitness", "Your next workout " + timeSelected.getTime().toString());
                    dialog.dismiss(); // close the dialog
                }
            }
        });
    }

    // open build workout
    private void showBuildWorkoutFragment(){
        BuildWorkoutFragment buildWorkoutFragment = new BuildWorkoutFragment();
        // show fragment exercises
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("build workout back")
                .replace(
                        R.id.fragment_container_with_menu, buildWorkoutFragment).commit();
    }

    // open exercises fragment to edit
    private void showExercisesFragment(Exercise.Type type, long exerciseID) {
        //fragment exercises
        ExercisesFragment exercisesFragment = new ExercisesFragment();
        exercisesFragment.setType(type);
        exercisesFragment.setExerciseID(exerciseID);
        // show fragment exercises
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("exercises list back")
                .replace(
                        R.id.fragment_container_with_menu, exercisesFragment).commit();
    }

    // get training plan from DB
    private  void getTrainingPlanByUserID() {
        mDatabase.child("TrainingPlan").child(currentUser.getUid()).child("Exercises")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                // get exercises details
                                getExerciseDetails(dataSnapshot.getValue().toString());
                            }
                        }
                        else{ // for this user doesn't training plan yet.
                            tvSelectAlertTime.setVisibility(View.INVISIBLE);
                            tvNoWorkout.setVisibility(View.VISIBLE);
                        }
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

    // get exercise details by exercise id
    private void getExerciseDetails(String exerciseID){
        if(!exerciseID.equals("-1")){
            mDatabase.child("Exercises").child(exerciseID)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Exercise exercise =null;
                            try{
                                exercise = snapshot.getValue(Exercise.class);
                            }
                            catch (Exception e){
                                Log.d("fitness",e.getMessage());
                                ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                                        getContext(),
                                        getActivity(),
                                        getString(R.string.errorMessageDialog_error_message),
                                        getString(R.string.errorMessageDialog_go_back),
                                        ErrorMessageDialog.ErrorMessageType.FINISH);
                                errorMessageDialog.show();
                            }
                            if (exercise != null) {
                                exercise.setExerciseID(Long.parseLong(exerciseID));
                                exercisesTrainingPlan.add(exercise);
                            }
                            trainingPlanAdapter.notifyDataSetChanged(); // update the list of workout
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
        else{
            // error!!!
            ErrorMessageDialog errorMessageDialog = new ErrorMessageDialog(
                    getContext(),
                    getActivity(),
                    getString(R.string.errorMessageDialog_error_message),
                    getString(R.string.errorMessageDialog_go_back),
                    ErrorMessageDialog.ErrorMessageType.FINISH);
            errorMessageDialog.show();
        }
    }

    // adapter for training plan list of exercises
    class TrainingPlanAdapter extends BaseAdapter{

        public TrainingPlanAdapter(){}

        @Override
        public int getCount() {
            return exercisesTrainingPlan.size();
        }

        @Override
        public Object getItem(int i) {
            return exercisesTrainingPlan.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                LayoutInflater inflater = getLayoutInflater();
                view = inflater.inflate(R.layout.list_view_workout, null);
            }
            TextView tvExerciseName = view.findViewById(R.id.listView_workout_exercise_name); // name of exercise
            TextView tvTypeName = view.findViewById(R.id.listView_workout_type_name); // name of type
            TextView tvRep = view.findViewById(R.id.listView_workout_rep_number); // number of rep/ time
            TextView tvSetQuantity = view.findViewById(R.id.listView_workout_set_quantity); // set
            TextView tvRepTitle = view.findViewById(R.id.listView_workout_rep_time);
            ImageView ivExerciseEdit = view.findViewById(R.id.listView_workout_edit);
            ImageView ivExerciseImage = view.findViewById(R.id.listView_workout_image);

            Exercise exercise = exercisesTrainingPlan.get(i);
            if(exercise != null){
                tvExerciseName.setText(exercise.getExerciseName());
                String set = "\tx" + exercise.getSetQuantity();
                tvSetQuantity.setText(set);
                String type = exercise.getType();
                if(type.equals(Exercise.Type.Hands_Shoulders.name())){
                    type = "Hands/Shoulders";
                }
                tvTypeName.setText(" " + type);

                if(exercise.getTime() == -1){
                    String number = exercise.getQuantity() + " ";
                    tvRep.setText(number);
                    tvRepTitle.setText(R.string.listView_workout_reps_each_set);
                }
                else{
                    String time = exercise.getTime() + " ";
                    tvRep.setText(time);
                    tvRepTitle.setText(R.string.listView_workout_time_each_set);
                }
                ivExerciseEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open exercises fragment
                        showExercisesFragment(Exercise.Type.valueOf(exercise.getType()),exercise.getExerciseID());
                        trainingPlanAdapter.notifyDataSetChanged();
                    }
                });
                //show image to bigger
                ivExerciseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageExerciseDialog imageExerciseDialog = new ImageExerciseDialog(getContext(),exercise);
                        imageExerciseDialog.show();
                    }
                });
            }
            return view;
        }
    }
}