/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.myfitness.Model.Exercise;

public class ImageExerciseDialog extends AlertDialog {

    Exercise exercise;

    public ImageExerciseDialog(@NonNull Context context, Exercise exercise) {
        super(context);
        this.exercise = exercise;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(exercise == null){
            this.dismiss();
        }

        setContentView(R.layout.dialog_image_exercise);
        ImageView imvExit = findViewById(R.id.dialog_image_ic_exit);
        ImageView imvExercise = findViewById(R.id.dialog_image_imageView);

        // set image
        Uri uri = Uri.parse("android.resource://"+getContext().getPackageName()+"/drawable/" +exercise.getPicName());
        imvExercise.setImageURI(uri);

        // exit dialog
        imvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageExerciseDialog.this.dismiss();
            }
        });

    }
}
