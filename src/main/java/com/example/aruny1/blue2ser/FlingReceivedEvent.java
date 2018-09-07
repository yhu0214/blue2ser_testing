package com.example.aruny1.blue2ser;

import android.util.Log;

/**
 * Created by arun.y1 on 2018-02-22.
 */

public class FlingReceivedEvent {

    public enum FLING_TYPE{FLING_LEFT, FLING_RIGHT, FLING_UP, FLING_DOWN};
    FLING_TYPE myFlingType;


    public FlingReceivedEvent(String myString){

        switch(myString){

            case "fling_left":
                myFlingType = FLING_TYPE.FLING_LEFT;
                break;
            case "fling_right":
                myFlingType = FLING_TYPE.FLING_RIGHT;
                break;
            case "fling_up":
                myFlingType = FLING_TYPE.FLING_UP;
                break;
            case "fling_down":
                myFlingType = FLING_TYPE.FLING_DOWN;
                break;
            default :
                Log.d("FlingReceivedEvent", " receieved unrecognisable fling type "+ myString);
        }

    }

    public FLING_TYPE getFlingType(){
        return myFlingType;
    }



}
