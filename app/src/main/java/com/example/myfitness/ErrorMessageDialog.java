/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

public class ErrorMessageDialog extends AlertDialog {

    public enum ErrorMessageType{
        FINISH,USER_HOME, HERE
    }

    String errorMessage1, errorMessage2;
    ErrorMessageType goTo;
    FragmentActivity activity;

    public ErrorMessageDialog(@NonNull Context context, FragmentActivity activity, String errorMessage1, String errorMessage2, ErrorMessageType goTo) {
        super(context);
        this.activity = activity;
        this.errorMessage1 = errorMessage1;
        this.errorMessage2 = errorMessage2;
        this.goTo = goTo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_error_message);


        TextView tvErrorMessage1 = findViewById(R.id.errorMessageDialog_error_message1);
        TextView tvErrorMessage2 = findViewById(R.id.errorMessageDialog_error_message2);
        tvErrorMessage1.setText(errorMessage1);
        tvErrorMessage2.setText(errorMessage2);

        ImageView imvExit = findViewById(R.id.errorMessageDialog_ic_exit);

        imvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(goTo == ErrorMessageType.FINISH){
                    activity.finish();
                }
                else if(goTo == ErrorMessageType.USER_HOME){
                    // go to activity main
                    Intent i = new Intent(getContext(), UserInActivity.class);
                    activity.startActivity(i);
                    Objects.requireNonNull(activity).finish(); // finish the activity and go to main activity
                }

                // else stat here!!
            }
        });
    }
}
