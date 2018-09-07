package com.example.aruny1.blue2ser;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arun.y1 on 2018-05-26.
 */

public class PacketsInputManager {

    // static variable single_instance of type Singleton
    private static PacketsInputManager single_instance = null;

    // private constructor restricted to this class itself
    private PacketsInputManager()
    {
        lastControllerPacket = null;
    }

    // static method to create instance of Singleton class
    public static PacketsInputManager getInstance()
    {
        if (single_instance == null)
            single_instance = new PacketsInputManager();

        return single_instance;
    }

    ControllerPacket lastControllerPacket;

    public JSONObject updateControllerPacket(ControllerPacket controllerPacket){

        JSONObject jsonObject = null;

        if(lastControllerPacket == null){
            lastControllerPacket = controllerPacket;
            //return null;
        }else{


            if(!lastControllerPacket.myIsTouching && !lastControllerPacket.myIsHovering){ // finger outside of hover or touch zones

                if(controllerPacket.myIsHovering ){
                    //Hover Enter
                    //Log.d("PacketsInputManager", " Hover Enter");


                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_HOVER_ENTER);

                }else if(controllerPacket.myIsTouching){

                    //Log.d("PacketsInputManager", " ACTION_DOWN ");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_DOWN);

                }

            }else if(!lastControllerPacket.myIsTouching && lastControllerPacket.myIsHovering ){// finger in hover zone


                if(controllerPacket.myIsTouching){

                    //Log.d("PacketsInputManager", " Touch Enter ");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_DOWN);

                }else{

                    //Log.d("PacketsInputManager", " Hovering ");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_HOVER_MOVE);
                }


            }else if(lastControllerPacket.myIsTouching){


                if(controllerPacket.myIsTouching){

                    //Log.d("PacketsInputManager", " ACTION_MOVE ");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_MOVE);

                }else if(!controllerPacket.myIsTouching){

                    //Log.d("PacketsInputManager", " ACTION_UP ");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_UP);

                }


            }else if(lastControllerPacket.myIsHovering){

                if(controllerPacket.myIsHovering){

                    //Log.d("PacketsInputManager", " Hover Move");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_HOVER_MOVE);

                }else if(!controllerPacket.myIsHovering && !controllerPacket.myIsTouching){

                   //Log.d("PacketsInputManager", " Hover Exit");

                    jsonObject = getMotionEventAsJson( controllerPacket.myPosX, controllerPacket.myPosY, MotionEvent.ACTION_HOVER_EXIT);
                }
            }


            lastControllerPacket = controllerPacket;

        }

        return jsonObject;

    }

    MotionEvent generateMotionEvent(float x_coord_ratio, float y_coord_ratio, int action){


        final int width  = 1440;
        final int height = 2960;
        //Log.d("Unity", " width = "+ width + " height = " +height);

        final int x_coord_int = (int) (width * x_coord_ratio);
        final int y_coord_int = (int) (height * y_coord_ratio);
        //Log.d("Unity", " x_coord_int = "+ x_coord_int + " y_coord_int = " +y_coord_int);


        int downTime = (int) SystemClock.uptimeMillis();
        int eventTime = (int) SystemClock.uptimeMillis()+10;
        int meta_state  = 0;
        MotionEvent me = MotionEvent.obtain(downTime, eventTime, action, x_coord_int, y_coord_int, meta_state);
        me.setAction(action);
        me.setSource(InputDevice.SOURCE_MOUSE);

        //Log.d("HoverCursorManager", "for current_action " + action + " recreated motion event me = " + me.toString());

        return me;

    }


    JSONObject getMotionEventAsJson( float x_coord, float y_coord, int action){

        MotionEvent motionEvent = generateMotionEvent( x_coord, y_coord, action);

        if(motionEvent!=null){

            JSONObject myJSONObject = new JSONObject();
            try {
                myJSONObject.put("label", "hover_touch_data");
                //hack for testing phonecastVR
                myJSONObject.put("x_coord_ratio", x_coord);
                myJSONObject.put("y_coord_ratio", y_coord);
                //myJSONObject.put("x_coord_ratio", 0.93f);
                //myJSONObject.put("y_coord_ratio", 0.99f);

                myJSONObject.put("action", motionEvent.getAction());
                myJSONObject.put("down_time", motionEvent.getDownTime());
                myJSONObject.put("event_time", motionEvent.getEventTime());
                myJSONObject.put("meta_state", motionEvent.getMetaState());
                myJSONObject.put("axis_distance", motionEvent.getAxisValue(motionEvent.AXIS_DISTANCE));

                //myJSONObject.put("motion_event", motionEvent);

                long currentTime = System.currentTimeMillis();

                myJSONObject.put("time", currentTime);


                return myJSONObject;

            } catch (JSONException e) {
                String s = "stack trace " + e.getStackTrace();
                Log.d("MainActivity", s);

                e.printStackTrace();
            }

            return null;
        }


        return null;
        //Log.d("MainActivity", " broadcastState sending myJSONObject "+ myJSONObject.toString());

    }


    JSONObject getTrackingInfoAsJsonObject(ControllerPacket controllerPacket){

        JSONObject myJSONObject = new JSONObject();

        try {
            myJSONObject.put("label", "imu_data");
            //hack for testing phonecastVR
            myJSONObject.put("q_w", controllerPacket.myQW);
            myJSONObject.put("q_x", controllerPacket.myQX);
            myJSONObject.put("q_y", controllerPacket.myQY);
            myJSONObject.put("q_z", controllerPacket.myQZ);

            myJSONObject.put("a_x", controllerPacket.myAccX);
            myJSONObject.put("a_y", controllerPacket.myAccY);
            myJSONObject.put("a_z", controllerPacket.myAccZ);

            long currentTime = System.currentTimeMillis();

            myJSONObject.put("time", currentTime);


            return myJSONObject;

        } catch (JSONException e) {
            String s = "stack trace " + e.getStackTrace();
            Log.d("MainActivity", s);

            e.printStackTrace();
        }

        return  myJSONObject;
    }


    JSONObject getButtonEventAsJsonObject(ControllerPacket controllerPacket){


        JSONObject myJSONObject = new JSONObject();

        try {

            if(controllerPacket.myIsTriggerOn){
                myJSONObject.put("label", "phone_orientation_compensate");
            }

            long currentTime = System.currentTimeMillis();

            myJSONObject.put("time", currentTime);


            return myJSONObject;

        } catch (JSONException e) {
            String s = "stack trace " + e.getStackTrace();
            Log.d("MainActivity", s);

            e.printStackTrace();
        }

        return  myJSONObject;
    }



}
