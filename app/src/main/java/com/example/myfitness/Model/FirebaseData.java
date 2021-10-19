/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness.Model;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseData extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // save in  cache synchronized
    }
}
