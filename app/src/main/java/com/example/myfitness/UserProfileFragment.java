/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myfitness.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Date;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    TextView tvEditProfile,tvName,tvEmail,tvBirthday, tvAge,tvWeight, tvHeight,
            tvBmi,tvTargetWeight,tvActivityLevel,tvPassword,tvLogOut;
    // for authentication and DB
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    public UserProfileFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        tvName = v.findViewById(R.id.userProfileFragment_tv_name);
        tvEmail = v.findViewById(R.id.userProfileFragment_tv_email);
        tvEditProfile = v.findViewById(R.id.userProfileFragment_tv_edit);
        tvBirthday = v.findViewById(R.id.userProfileFragment_tv_birthday);
        tvAge = v.findViewById(R.id.userProfileFragment_tv_age);
        tvWeight = v.findViewById(R.id.userProfileFragment_tv_weight);
        tvHeight = v.findViewById(R.id.userProfileFragment_tv_height);
        tvBmi = v.findViewById(R.id.userProfileFragment_tv_bmi);
        tvTargetWeight = v.findViewById(R.id.userProfileFragment_tv_target);
        tvActivityLevel = v.findViewById(R.id.userProfileFragment_tv_active);
        tvLogOut = v.findViewById(R.id.userProfileFragment_logout);
        tvPassword = v.findViewById(R.id.userProfileFragment_change_password);

        //DB
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser(); // get user
        mDatabase.keepSynced(true);

        showUserDetails(); // upload details

        //click to edit profile
        tvEditProfile.setOnClickListener(this);

        //click to log out
        tvLogOut.setOnClickListener(this);

        //click to edit details
        tvName.setOnClickListener(this); // edit name
        tvPassword.setOnClickListener(this); // change password
        tvBirthday.setOnClickListener(this);
        tvWeight.setOnClickListener(this);
        tvHeight.setOnClickListener(this);
        tvTargetWeight.setOnClickListener(this);
        tvActivityLevel.setOnClickListener(this);

        return v;
    }

    // show all details
    private void showUserDetails(){
        if(currentUser != null){
            Log.d("fitness","get user successful");
            tvName.setText(currentUser.getDisplayName());
            tvEmail.setText(currentUser.getEmail());

            mDatabase.child("Users").child(currentUser.getUid()).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = null;
                            try {
                                user = (User) snapshot.getValue(User.class);
                                Log.d("fitness", "success DB");
                            } catch (Exception e) {
                                Log.d("fitness", "Error DB-" + e.getMessage());
                                user = new User();
                            }
                            if(user != null){
                                //upload details to profle fragment
                                if (!user.getBirthday().equals("")) {
                                    tvBirthday.setText(user.getBirthday());
                                }
                                if (user.getWeight() != -1) {
                                    tvWeight.setText(Double.toString(user.getWeight()));
                                }
                                if (user.calculateAge() != -1) {
                                    tvAge.setText(Long.toString(user.calculateAge()));
                                }
                                if (!user.getActivityLevel().equals("")) {
                                    tvActivityLevel.setText(user.getActivityLevel());
                                }
                                if (user.calculateBmi() != -1) {
                                    tvBmi.setText(new DecimalFormat("##.##").format(user.calculateBmi()));
                                }

                                if (user.getHeight() != -1) {
                                    tvHeight.setText(Double.toString(user.getHeight()));
                                }
                                if (user.getTargetWeight() != -1) {
                                    tvTargetWeight.setText(Double.toString(user.getTargetWeight()));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    }
            );

        }
        else{
            Log.d("fitness","get user unsuccessful");
            getActivity().finish(); // exit
        }
    }

    @Override
    public void onClick(View view) {

        if(view == tvEditProfile){
            // change color to blue for all details that user can edit
            tvName.setTextColor(0xff0000ff);
            tvBirthday.setTextColor(0xff0000ff);
            tvWeight.setTextColor(0xff0000ff);
            tvHeight.setTextColor(0xff0000ff);
            tvTargetWeight.setTextColor(0xff0000ff);
            tvActivityLevel.setTextColor(0xff0000ff);

            // user can be able to click
            tvName.setEnabled(true);
            tvBirthday.setEnabled(true);
            tvWeight.setEnabled(true);
            tvHeight.setEnabled(true);
            tvTargetWeight.setEnabled(true);
            tvActivityLevel.setEnabled(true);

            // check if empty - if yes change the text to add
            if(tvBirthday.getText().toString().equals("")){
                tvBirthday.setText(R.string.userProfileFragment_add);
            }
            if(tvWeight.getText().toString().equals("")){
                tvWeight.setText(R.string.userProfileFragment_add);
            }
            if(tvHeight.getText().toString().equals("")){
                tvHeight.setText(R.string.userProfileFragment_add);
            }
            if(tvTargetWeight.getText().toString().equals("")){
                tvTargetWeight.setText(R.string.userProfileFragment_add);
            }
            if(tvActivityLevel.getText().toString().equals("")){
                tvActivityLevel.setText(R.string.userProfileFragment_add);
            }
        }
        //exit this activity
        else if(view == tvLogOut){
            mAuth.signOut(); // user sign out database
            Intent i = new Intent(getActivity(),MainActivity.class);
            startActivity(i); // open login and register
           getActivity().finish(); // exit this activity
        }
        // open change name dialog
        else if(view == tvName){
            showEditNameDialog();
        }

        //open change password dialog
        else if(view == tvPassword){
            showChangePasswordDialog();
        }
        // open change weight dialog
        else if(view == tvWeight){
            showEditWeightDialog();
        }
        //open change height dialog
        else if(view == tvHeight){
            showEditHeightDialog();
        }
        // open change birthday dialog
        else if(view == tvBirthday){
            showEditBirthdayDialog();
        }
        // open change target weight
        else if(view == tvTargetWeight){
            showEditTargetWeightDialog();
        }
        // open change activity level
        else if(view == tvActivityLevel){
            showEditActivityLevel();
        }
    }

    // dialog to change the name
    public void showEditNameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.your_name);

        final View nameDialog = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        builder.setView(nameDialog);
        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText etNameDialog = nameDialog.findViewById(R.id.nameDialog_tv_name);
                if (etNameDialog.getText().toString().equals("") ||
                        etNameDialog.getText().toString().matches(".*\\d.*")) {
                    etNameDialog.setError("The name can't contain numbers or empty!");
                } else {
                    tvName.setText(etNameDialog.getText().toString());
                    //change name on DB

                    if (currentUser != null) {
                        // save user name to DB
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(tvName.getText().toString())
                                .build();
                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("fitness", "User name profile updated.");
                                        }
                                        else{
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
                    dialog.dismiss(); // close the dialog
                }
            }
        });
    }

    //dialog to change the password
    public void showChangePasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_change_password);

        final View passwordDialog = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(passwordDialog);
        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText etPasswordDialog = passwordDialog.findViewById(R.id.passwordDialog_tv_password);
                EditText etRePasswordDialog = passwordDialog.findViewById(R.id.passwordDialog_tv_repassword);

                if (etPasswordDialog.getText().toString().equals("")) { // password not empty
                    etPasswordDialog.setError("The password can't be empty!");
                    etRePasswordDialog.setText("");
                }
                else if(!etPasswordDialog.getText().toString().equals(etRePasswordDialog.getText().toString())){
                    etRePasswordDialog.setError("Incompatible passwords!"); // password != repassword
                    etRePasswordDialog.setText("");
                }
                else if(etPasswordDialog.getText().toString().length() <6){ // password.length <6
                    etPasswordDialog.setError("Password must be at least 6 characters long!");
                }
                else {
                    //change password on DB
                    if (currentUser != null) {
                        // save new password to DB
                        currentUser.updatePassword(etPasswordDialog.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("fitness", "User name profile updated.");
                                }
                                else{
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
                    dialog.dismiss(); // close the dialog
                }
            }
        });
    }

    // dialog to change the weight
    public void showEditWeightDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_edit_weight);

        final View weightDialog = getLayoutInflater().inflate(R.layout.dialog_edit_weight, null);
        builder.setView(weightDialog);
        NumberPicker npfirstNumber = weightDialog.findViewById(R.id.dialog_weight_first_number);
        NumberPicker npSecondNumber = weightDialog.findViewById(R.id.dialog_weight_second_number);
        npfirstNumber.setMinValue(10);
        npfirstNumber.setMaxValue(200);
        npSecondNumber.setMinValue(0);
        npSecondNumber.setMaxValue(9);
        npfirstNumber.setWrapSelectorWheel(false);
        npSecondNumber.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tvWeight.setText(npfirstNumber.getValue() + "." + npSecondNumber.getValue() +"kg");

                //save to DB
                Double weight = Double.parseDouble(npfirstNumber.getValue() + "." + npSecondNumber.getValue());
                mDatabase.child("Users").child(currentUser.getUid()).child("weight").setValue(weight)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("fitness", "User name profile updated.");
                                }
                                else{
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

                dialog.dismiss(); // close the dialog
            }
        });
    }

    //dialog to change the height
    public void showEditHeightDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_edit_height);

        final View heightDialog = getLayoutInflater().inflate(R.layout.dialog_edit_height, null);
        builder.setView(heightDialog);
        NumberPicker npfirstNumber = heightDialog.findViewById(R.id.dialog_height_number);
        npfirstNumber.setMinValue(60);
        npfirstNumber.setMaxValue(245);
        npfirstNumber.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tvHeight.setText(npfirstNumber.getValue() + "cm");

                //save to DB
                Double height = Double.parseDouble(npfirstNumber.getValue() + "." + npfirstNumber.getValue());
                mDatabase.child("Users").child(currentUser.getUid()).child("height").setValue(height)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("fitness", "User name profile updated.");
                        }
                        else{ // DB wrong
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
                dialog.dismiss(); // close the dialog
            }
        });
    }

    //dialog to change the birthday
    public void showEditBirthdayDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_edit_birthday);

        final View birthdayDialog = getLayoutInflater().inflate(R.layout.dialog_edit_birthday, null);
        builder.setView(birthdayDialog);
        DatePicker birthdayPicker = birthdayDialog.findViewById(R.id.dialog_birthday_date);
        birthdayPicker.setMaxDate(new Date().getTime()); // max is today!
        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String birthday = birthdayPicker.getDayOfMonth() + "." +(birthdayPicker.getMonth()+1) + "."+
                        birthdayPicker.getYear();
                tvBirthday.setText(birthday);

                //save to DB
                mDatabase.child("Users").child(currentUser.getUid()).child("birthday").setValue(birthday)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("fitness", "User name profile updated.");
                                }
                                else{ // DB wrong
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
                dialog.dismiss(); // close the dialog
            }
        });
    }

    //dialog to change the target weight
    public void showEditTargetWeightDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_edit_weight);

        final View targetweightDialog = getLayoutInflater().inflate(R.layout.dialog_edit_weight, null);
        builder.setView(targetweightDialog);
        NumberPicker npfirstNumber = targetweightDialog.findViewById(R.id.dialog_weight_first_number);
        NumberPicker npSecondNumber = targetweightDialog.findViewById(R.id.dialog_weight_second_number);
        npfirstNumber.setMinValue(10);
        npfirstNumber.setMaxValue(200);
        npSecondNumber.setMinValue(0);
        npSecondNumber.setMaxValue(9);
        npfirstNumber.setWrapSelectorWheel(false);
        npSecondNumber.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tvTargetWeight.setText(npfirstNumber.getValue() + "." + npSecondNumber.getValue() +"kg");

                //save to DB
                Double targetWeight = Double.parseDouble(npfirstNumber.getValue() + "." + npSecondNumber.getValue());
                mDatabase.child("Users").child(currentUser.getUid()).child("targetWeight").setValue(targetWeight)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("fitness", "User name profile updated.");
                                }
                                else{ // DB wrong
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
                dialog.dismiss(); // close the dialog
            }
        });
    }

    //dialog to change the activity level
    public void showEditActivityLevel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.userProfileFragment_edit_activity_level);

        final View activityLevelDialog = getLayoutInflater().inflate(R.layout.dialog_edit_activity_level, null);
        builder.setView(activityLevelDialog);
        NumberPicker activityLevel = activityLevelDialog.findViewById(R.id.dialog_activity_level_numberPicker);
        String valuesActivityLevel [] = new String[]{"Not Very Active", "Lightly Active", "Active","Very Active"};
        activityLevel.setMinValue(0);
        activityLevel.setMaxValue(valuesActivityLevel.length-1);
        activityLevel.setDisplayedValues(valuesActivityLevel);


        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("CANCEL",null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // click on OK
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tvActivityLevel.setText(valuesActivityLevel[activityLevel.getValue()]);

                //save to DB
                mDatabase.child("Users").child(currentUser.getUid()).child("activityLevel").setValue(
                        valuesActivityLevel[activityLevel.getValue()])
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("fitness", "User name profile updated.");
                                }
                                else{ // DB wrong
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
                dialog.dismiss(); // close the dialog
            }
        });
    }
}// end class USerProfileFragment