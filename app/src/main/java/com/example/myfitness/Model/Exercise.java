/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness.Model;

import androidx.annotation.Keep;

import java.util.Objects;

@Keep
public class Exercise {

    public enum  Type{
        Legs, Hands_Shoulders,Back, Chest,Belly
    }
    public enum Difficulty{
        E,M,H
    }

    private long exerciseID;
    private String exerciseName;
    private String picName;	
    private Difficulty difficulty;
    private long quantity;
    private long setQuantity;
    private long time;
    private Type type;

    public Exercise(){
        // default, because this fields maybe empty
        this.quantity = -1;
        this.time = -1;
        this.exerciseID = -1;
    }

    public long getExerciseID() {
        return exerciseID;
    }

    public void setExerciseID(long exerciseID) {
        this.exerciseID = exerciseID;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getSetQuantity() {
        return setQuantity;
    }

    public long getTime() {
        return time;
    }

    public String getDifficulty() {
        if(difficulty != null){
         return difficulty.name();
        }
        else{
            return null;
        }
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getPicName() {
        return picName;
    }

    public String getType() {
        if(type != null){
            return type.name();
        }
        else{
            return null;
        }
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "exerciseName='" + exerciseName + '\'' +
                ", picName='" + picName + '\'' +
                ", difficulty=" + difficulty +
                ", quantity=" + quantity +
                ", setQuantity=" + setQuantity +
                ", time=" + time +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return Objects.equals(exerciseName, exercise.exerciseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exerciseName);
    }
}
