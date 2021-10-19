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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    Button btnLogin;
    EditText etEmail, etPassword;
    TextView tvError;
    SharedPreferences sp;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        btnLogin = v.findViewById(R.id.loginFragment_btn_login);
        etEmail = v.findViewById(R.id.loginFragment__et_email);
        etPassword = v.findViewById(R.id.loginFragment_et_password);
        tvError = v.findViewById(R.id.loginFragment_et_error_login);
        mAuth = FirebaseAuth.getInstance(); // to firebase authentication
        sp=getContext().getSharedPreferences("user",0); // to save user in this device

        //connect to sp
        if(sp != null){
            String strEmail=sp.getString("email",null);
            String strPass=sp.getString("password",null );
            if(strEmail != null && strPass != null){
                etEmail.setText(strEmail);
                etPassword.setText(strPass);
            }
        }

        // on click to log in
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // check if the user is exist
                tvError.setVisibility(View.INVISIBLE); // invisible error log in
                mAuth.signInWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("fitness", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("fitness", "signInWithEmail:failure", task.getException());
                                    updateUI(null);
                                }
                            }
                        });
            }// end onClick function
        });
        return v;
    }

    private void updateUI(FirebaseUser user){
        if(user != null){ // the log in is successful

            // shared Preference - save user in this device
            if(sp != null) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("email", etEmail.getText().toString());
                editor.putString("password", etPassword.getText().toString());
                editor.apply();
            }

            // pass to user in activity
            Intent i = new Intent(getActivity(), UserInActivity.class);
            startActivity(i);
           getActivity().finish(); // finish the activity and go to user in activity
        }
        else{ // log in failed!
            tvError.setVisibility(View.VISIBLE);
            etPassword.setText("");
        }
    }
}