package com.example.aruny1.blue2ser;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arun.y1 on 2018-02-07.
 */

public class HoverCursorManager {

    private ImageView primaryCircleHoverView;
    private ImageView primaryCircleTouchView;
    private ViewGroup mRootLayout;
    private Activity myActivity;


    private int last_action_down_x_coord;
    private int last_action_down_y_coord;
    private long last_action_down_time;

    private int last_action_up_x_coord;
    private int last_action_up_y_coord;
    private long last_action_up_time;

    private int current_cursor_position_x = 1000;
    private int current_cursor_position_y = 1000;

    private int cursor_position_x_on_action_up;
    private int cursor_position_y_on_action_up;

    private int cursor_position_x_on_action_down;
    private int cursor_position_y_on_action_down;


    enum INTERACTION_MODE{HOVER, DRAG_TAP};
    INTERACTION_MODE myInteractionMode = INTERACTION_MODE.HOVER;

    public HoverCursorManager(ImageView pCHV, ImageView pCTV, ViewGroup mRL, Activity mA, boolean hoverOn){

        primaryCircleHoverView = pCHV;
        primaryCircleTouchView = pCTV;
        mRootLayout = mRL;
        myActivity = mA;
        if(hoverOn){
            myInteractionMode = INTERACTION_MODE.HOVER;
            Log.d("HCM", " myInteractionMode set to HOVER");
        }else{
            myInteractionMode = INTERACTION_MODE.DRAG_TAP;
            Log.d("HCM", " myInteractionMode set to DRAG TAP");
        }

        updateCirclePositionTo(primaryCircleHoverView, current_cursor_position_x, current_cursor_position_y, 100, 100, 1.0f);
        updateCirclePositionTo(primaryCircleTouchView, current_cursor_position_x, current_cursor_position_y, 100, 100, 1.0f);

    }

    void updateCirclePositionTo(View circleView, float x, float y, int display_width, int display_height, float distance_ratio){

        //Log.d("HCM", " updateCirclePositionTo x =" + x + " y " + y);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) circleView.getLayoutParams();
        float circle_width = circleView.getWidth();
        float circle_height = circleView.getHeight();
        float circle_half_width = circle_width*0.5f;
        float circle_half_height = circle_height*0.5f;

        int resulting_x = (int) (x - circle_half_width);
        int resulting_y = (int) (y - circle_half_height);

        //Log.d("HCM", " resulting_x = "+ resulting_x + " resulting_y = "+ resulting_y);

        //We need to do this in order for the center of the circle to get to the top left corner
        //if(resulting_x <0)
        layoutParams.leftMargin = resulting_x;

        //if(resulting_y < 0)
        layoutParams.topMargin = resulting_y;

        //if(resulting_x > display_width)
        // layoutParams.rightMargin = (int) (-1*circle_half_width);

        //if(resulting_y > display_height)
        //layoutParams.bottomMargin = (int) (-1*circle_half_height);

        circleView.setLayoutParams(layoutParams);
        //circleView.setScaleX(distance_ratio);
        //circleView.setScaleY(distance_ratio);

        //circleView.setLayoutParams(layoutParams);
        //mRootLayout.invalidate();

    }


    public synchronized void processMessage(JSONObject jsonObject, final int width, final int height){

        //Log.d("HoverCursorManager", " processMessage " + jsonObject.toString() );


        try {

            //DisplayMetrics metrics = myActivity.getApplicationContext().getResources().getDisplayMetrics();
            //final int width = metrics.widthPixels;
            //final int height = metrics.heightPixels;

            //Log.d("HCM", " width = "+ width + " height = "+ height);

            if(jsonObject!=null) {
                //Log.d("SessionManager", " jsonObject!=null ");
                String label = jsonObject.getString("label");
                //Log.d("HCM", " label = "+ label);


                if(label.contentEquals("hover_touch_data")) {
                    float x_coord_ratio = (float) jsonObject.getDouble("x_coord_ratio");
                    float y_coord_ratio = (float) jsonObject.getDouble("y_coord_ratio");

                    final int current_action = jsonObject.getInt("action");
                    //MotionEvent me = (MotionEvent) jsonObject.get("motion_event");
                    final long downTime = jsonObject.getLong("down_time");
                    final long eventTime = jsonObject.getLong("event_time");
                    final int meta_state = jsonObject.getInt("meta_state");
                    int axis_distance = jsonObject.getInt("axis_distance");
                    final float distance_ratio = 1 - (float) (axis_distance) / 256.0f;

                    //Log.d("HoverCursorManager", " current_action = "+ current_action + " x_coord_ratio = "+ x_coord_ratio + " y_coord_ratio = "+ y_coord_ratio);



                    //MotionEvent motionEvent = (MotionEvent) me;

                    //Log.d("HoverCursorManager", " me = " + me.toString());


                    //Log.d("HCM", " label = " + label + " x_coord_ratio = " + x_coord_ratio + " y_coord_ratio = " + y_coord_ratio + " current_action = " + current_action);

                    final int x_coord_int = (int) (width * x_coord_ratio);
                    final int y_coord_int = (int) (height * y_coord_ratio);

                    //Log.d("HCM", " x_coord_int = " + x_coord_int + " y_coord_int = " + y_coord_int + " width = " + width + " height = " + height);




                    myActivity.runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //Rect circleViewRect = new Rect();
                            //primaryCircleHoverView.getHitRect(circleViewRect);

                            //Log.d("HCM", " inside thread finalx_coord_secondary = " + finalx_coord_secondary + " finaly_coord_secondary = " + finaly_coord_secondary);
                            //Log.d("HoverCursorManager", " current_action = " + current_action + " x_coord = " + x_coord_int + " y_coord = " + y_coord_int);



                            if (myInteractionMode == INTERACTION_MODE.HOVER) {

                                /*if(current_action == MotionEvent.ACTION_MOVE) {

                                }else*/
                                {

                                    current_cursor_position_x = x_coord_int;
                                    current_cursor_position_y = y_coord_int;
                                    updateCirclePositionTo(primaryCircleHoverView, current_cursor_position_x, current_cursor_position_y, width, height, distance_ratio);
                                    updateCirclePositionTo(primaryCircleTouchView, current_cursor_position_x, current_cursor_position_y, width, height, distance_ratio);

                                    MotionEvent me = MotionEvent.obtain(downTime, eventTime, current_action, current_cursor_position_x, current_cursor_position_y, meta_state);
                                    me.setAction(current_action);
                                    //Log.d("HoverCursorManager", "for current_action " + current_action + " recreated motion event me = " + me.toString());

                                    // Dispatch touch event to view
                                    mRootLayout.dispatchTouchEvent(me);
                                }

                                //if(current_action == MotionEvent.ACTION_DOWN)
                                    //Log.d("HoverCursorManager", " ACTION_DOWN time  = "+ System.currentTimeMillis());


                            } else {

                                if (current_action == MotionEvent.ACTION_HOVER_ENTER || current_action == MotionEvent.ACTION_HOVER_MOVE || current_action == MotionEvent.ACTION_HOVER_EXIT) {

                                } else if(current_action == MotionEvent.ACTION_MOVE){

                                        int delta_x_from_down = x_coord_int - last_action_down_x_coord;
                                        int delta_y_from_down = y_coord_int - last_action_down_y_coord;

                                        //Log.d("HoverCursorManager", " delta_x_from_down = " + delta_x_from_down + " delta_y_from_down = "+ delta_y_from_down);


                                        current_cursor_position_x = cursor_position_x_on_action_down + delta_x_from_down;
                                        current_cursor_position_y = cursor_position_y_on_action_down + delta_y_from_down;

                                        if(current_cursor_position_x < 0)
                                            current_cursor_position_x = 0;
                                        else if(current_cursor_position_x > width)
                                            current_cursor_position_x = width;
                                        if(current_cursor_position_y <0)
                                            current_cursor_position_y = 0;
                                        else if(current_cursor_position_y > height)
                                            current_cursor_position_y = height;


                                        updateCirclePositionTo(primaryCircleTouchView, current_cursor_position_x, current_cursor_position_y, width, height, distance_ratio);

                                        MotionEvent me = MotionEvent.obtain(downTime, eventTime, current_action, current_cursor_position_x, current_cursor_position_y, meta_state);
                                        //Log.d("HoverCursorManager", "recreated motion event me = " + me.toString());
                                        // Dispatch touch event to view
                                        mRootLayout.dispatchTouchEvent(me);


                                } else if(current_action == MotionEvent.ACTION_DOWN){

                                    last_action_down_x_coord = x_coord_int;
                                    last_action_down_y_coord = y_coord_int;
                                    last_action_down_time = System.currentTimeMillis();

                                    cursor_position_x_on_action_down = current_cursor_position_x;
                                    cursor_position_y_on_action_down = current_cursor_position_y;


                                } else if(current_action == MotionEvent.ACTION_UP){
                                    last_action_up_time = System.currentTimeMillis();


                                    long current_time = System.currentTimeMillis();
                                    long time_elapsed_between_down_and_up = current_time - last_action_down_time;
                                    //Log.d("HoverCursorManager", "time_elapsed_between_down_and_up = " + time_elapsed_between_down_and_up);

                                    if( time_elapsed_between_down_and_up < 150){//Put it back to where it was
                                        updateCirclePositionTo(primaryCircleTouchView, cursor_position_x_on_action_up, cursor_position_y_on_action_up, width, height, distance_ratio);
                                    }

                                    last_action_up_x_coord = x_coord_int;
                                    last_action_up_y_coord = y_coord_int;

                                    cursor_position_x_on_action_up = current_cursor_position_x;
                                    cursor_position_y_on_action_up = current_cursor_position_y;

                                }

                            }

                            if (myInteractionMode == INTERACTION_MODE.HOVER) {

                                if (current_action == MotionEvent.ACTION_HOVER_ENTER || current_action == MotionEvent.ACTION_HOVER_MOVE || current_action == MotionEvent.ACTION_HOVER_EXIT) {
                                    primaryCircleTouchView.setVisibility(View.INVISIBLE);
                                    primaryCircleHoverView.setVisibility(View.VISIBLE);
                                } else {
                                    primaryCircleTouchView.setVisibility(View.VISIBLE);
                                    primaryCircleHoverView.setVisibility(View.INVISIBLE);
                                }

                            } else {

                                if (current_action == MotionEvent.ACTION_HOVER_ENTER || current_action == MotionEvent.ACTION_HOVER_MOVE) {
                                    primaryCircleTouchView.setVisibility(View.VISIBLE);
                                } else {
                                    primaryCircleTouchView.setVisibility(View.VISIBLE);
                                    primaryCircleHoverView.setVisibility(View.INVISIBLE);
                                }
                            }


                        }


                    }


                    ));


                }else if(label.contentEquals("restart_study")){


                }else if(label.contentEquals("toggle_hover_on_off")){

                    boolean state = jsonObject.getBoolean("state");

                    if(state){
                        myInteractionMode = INTERACTION_MODE.HOVER;
                        Log.d("HCM", " myInteractionMode set to Hover");
                    }else{
                        myInteractionMode = INTERACTION_MODE.DRAG_TAP;
                        Log.d("HCM", " myInteractionMode set to TAP");
                    }

                }else if(label.contentEquals("single_tap_up_event")){

                    final long downTime = jsonObject.getLong("down_time");
                    final long eventTime = jsonObject.getLong("event_time");
                    final int meta_state = jsonObject.getInt("meta_state");



                    myActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MotionEvent me = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, current_cursor_position_x, current_cursor_position_y, meta_state);
                            mRootLayout.dispatchTouchEvent(me);
                            MotionEvent me2 = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, current_cursor_position_x, current_cursor_position_y, meta_state);
                            mRootLayout.dispatchTouchEvent(me2);
                        }
                    });

                }else if(label.contentEquals("fling_data")){

                    String fling_type = jsonObject.getString("fling_type");

                    //Gson gson = new Gson();
                    //String e1 = jsonObject.getString("motionevent_e1");
                    //String e2 = jsonObject.getString("motionevent_e2");
                    //Log.d("HCM", " e1 = "+ e1 + " e2 = "+ e2);

                    //MotionEvent motionevent_e1 = gson.fromJson(e1, MotionEvent.class);
                    //MotionEvent motionevent_e2 = gson.fromJson(e2, MotionEvent.class);


                    float velocityX = (float) jsonObject.getDouble("velocityX");
                    float velocityY = (float) jsonObject.getDouble("velocityY");

                    //Log.d("HCM", " motionevent_e1 = "+ motionevent_e1.toString() + " motionevent_e2 = "+ motionevent_e2.toString() + " velocityX = "+ velocityX + " velocityY = "+ velocityY);


                    //JSONObject motionevent_e1_json = new JSONObject(motionevent_e1);
                    //JSONObject motionevent_e2_json = new JSONObject(motionevent_e2);
                    //JSONObject.wrap()


                    //If we are in drag tap mode, forward these fling events to other activities/fragments
                    if(myInteractionMode == INTERACTION_MODE.DRAG_TAP){
                        EventBus.getDefault().post( new FlingReceivedEvent(fling_type));
                    }

                }else if(label.contentEquals("session_start_summary")) {

                    Log.d("HCM", " label contains session_start_summary ");

                    //first make sure the previous session is saved.
                    //To-DO

                    //Now replace the current session
                    int participantId = jsonObject.getInt("participant_id");
                    String participantGroup = jsonObject.getString("participant_group");
                    int trialId = jsonObject.getInt("trial_id");
                    String inputType = jsonObject.getString("input_type");
                    String taskType = jsonObject.getString("task_type");
                    String logFileName;

                    Log.d("HCM", " inputType = "+ inputType);


                    if(inputType.contentEquals("drag_tap")){
                        myInteractionMode = INTERACTION_MODE.DRAG_TAP;
                        Log.d("HCM", " myInteractionMode set to TAP");
                    }else if(inputType.contentEquals("hover_touch")){
                        myInteractionMode = INTERACTION_MODE.HOVER;
                        Log.d("HCM", " myInteractionMode set to Hover");
                    }



                }

            }




        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
