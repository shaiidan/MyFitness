/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myfitness.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment  {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SharedPreferences sp;
    Button btnRegister;
    EditText etName, etPassword, etRePassword,etEmail;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        btnRegister = v.findViewById(R.id.registerFragment_btn_register);
        etEmail = v.findViewById(R.id.registerFragment_et_email);
        etName = v.findViewById(R.id.registerFragment_et_name);
        etPassword = v.findViewById(R.id.registerFragment_et_password);
        etRePassword = v.findViewById(R.id.registerFragment_et_repassword);
        mDatabase = FirebaseDatabase.getInstance().getReference(); // to database
        mAuth = FirebaseAuth.getInstance(); // to firebase authentication
        sp=getContext().getSharedPreferences("user",0); // to save user in this device

        // on click button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFormValid()){
                    mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("fitness", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("fitness", "createUserWithEmail:failure", task.getException());
                                        updateUI(null);
                                    }
                                }
                            });
                }
            }
        });
        return v;
    }

    private boolean isFormValid(){
        boolean isCheckOk = true;

        // check name contain numbers
        if(etName.getText().toString().matches(".*\\d.*")){
            etName.setError("The name can't contain numbers!");
            isCheckOk = false;
        }
        // check if name empty
        if(etName.getText().toString().isEmpty()){
            etName.setError("The name can't be empty!");
            isCheckOk = false;
        }
        //check if password empty
        if(etPassword.getText().toString().isEmpty()){
            etPassword.setError("The password can't be empty!");
            isCheckOk = false;
        }
        // check if password == re-password
        if(!etPassword.getText().toString().equals(etRePassword.getText().toString())){
            etRePassword.setError("The passwords don't match");
            isCheckOk = false;
        }

        // check email
        if(!etEmail.getText().toString().matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){
            etEmail.setError("Not email format!");
            isCheckOk = false;
        }

        // check pass <6
        if(etPassword.length()<6){
            etPassword.setError("Password must be 6 characters or more");
            isCheckOk = false;
        }
        return isCheckOk;
    }

    // update user if register successful or not
    private void updateUI(FirebaseUser user)
    {
        if(user != null){
            // save user name to DB
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(etName.getText().toString())
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) { //
                            if (task.isSuccessful()) {
                                Log.d("fitness", "User name profile updated.");

                                // save email and password to sp log in
                                if(sp != null) {
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("email", etEmail.getText().toString());
                                    editor.putString("password", etPassword.getText().toString());
                                    editor.apply();
                                }

                                // initialization user profile
                                mDatabase.child("Users").child(user.getUid()).setValue(new User())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("fitness", "User name profile updated.");
                                                    // intent user to user in activity
                                                    Intent i = new Intent(getActivity(),UserInActivity.class);
                                                    startActivity(i);
                                                    requireActivity().finish(); // finish the activity and go to user in activity
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
                            }
                        }
                    });
        } else{
            etEmail.setError("This email is exist!!");
        }
    }
}