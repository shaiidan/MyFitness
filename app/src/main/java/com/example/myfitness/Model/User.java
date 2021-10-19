/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness.Model;

import androidx.annotation.Keep;

import java.util.Calendar;


@Keep
public class User {
    private String birthday; // format dd.mm.yyyy
    private double weight; // kg
    private double height; // meter
    private double targetWeight;
    private String activityLevel;


    public User(){
        // initialization all attributes by defaults
        this.birthday = "";
        this.activityLevel = "";
        this.weight = -1;
        this.height = -1;
        this.targetWeight = -1;
    }

    public String getBirthday() {
        return birthday;
    }

    public double getHeight() {
        return height;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public double getWeight() {
        return weight;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int calculateAge() {
        if(birthday != null && !birthday.equals("")){
            String temp = birthday.split("\\.")[2];
            int year = Integer.parseInt(temp);
            Calendar c = Calendar.getInstance();
            int yearToday = c.get(Calendar.YEAR);
            return yearToday - year;
        }
        return -1;
    }

    public double calculateBmi() {
        if(weight != -1 && height != -1){
            return weight / (height*0.01 * height*0.01);
        }
        return -1;
    }
}
