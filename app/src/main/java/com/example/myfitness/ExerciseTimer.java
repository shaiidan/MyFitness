/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

public class ExerciseTimer {

    private CountDownTimer timer; // the timer
    private TextView tvTimer; // for text view show time
    private String finishMessage; // the message of finish timer
    private long mTimeLeftInMills; // time to left
    private final long mTimeInMills; // init timer time
    private Button btnNext, btnStart, btnStop; // buttons for setEnabled

    public ExerciseTimer(long timeInMills, TextView tvTimer, String finishMessage,
                         Button btnNext, Button btnStart, Button btnStop) {
        this.tvTimer = tvTimer;
        this.mTimeLeftInMills = timeInMills;
        this.mTimeInMills = timeInMills;
        this.finishMessage = finishMessage;
        this.timer = null;
        this.btnNext = btnNext;
        this.btnStart = btnStart;
        this.btnStop = btnStop;
    }

    // start a new timer
    public void startTimer(){
        timer = new CountDownTimer(mTimeLeftInMills, 1000) {
            @Override
            public void onTick(long l) {
                mTimeLeftInMills = l;
                tvTimer.setText(Long.toString(mTimeLeftInMills/1000));
            }
            @Override
            public void onFinish() {
                tvTimer.setText(finishMessage);
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnNext.setEnabled(true);
            }
        }.start();
        btnNext.setEnabled(false);
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    // stop the timer
    public void pauseTimer(){
        if(timer != null){
            timer.cancel();
            btnNext.setEnabled(true);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    // return on this time in timer again
    public void resetTimer(){
        mTimeLeftInMills = mTimeInMills;
    }
}
