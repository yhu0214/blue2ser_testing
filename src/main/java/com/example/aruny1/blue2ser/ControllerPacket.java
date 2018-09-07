package com.example.aruny1.blue2ser;

/**
 * Created by arun.y1 on 2018-05-26.
 */

public class ControllerPacket {

    public float myPosX;
    public float myPosY;
    public float myPosZ;
    public int myPressure;

    //bits from Status
    public boolean myIsTriggerOn;
    public boolean myIsRightButtonOn;
    public  boolean myIsLeftButtonOn;
    //bit 3 is nothing
    public boolean myIsTouchMoving;
    public boolean myIsTouching;
    public boolean myIsHoverMoving ;
    public boolean myIsHovering;

    public float myAccX;
    public float myAccY;
    public float myAccZ;
    public float myQW;
    public float myQX;
    public float myQY;
    public float myQZ;

    public ControllerPacket(float p_x,
                            float p_y,
                            float p_z,
                            int pres,
                            int is_trig_on,
                            int is_right_button_on,
                            int is_left_button_on,
                            int is_touch_moving,
                            int is_touching,
                            int is_hover_moving,
                            int is_hovering,
                            float acc_x,
                            float acc_y,
                            float acc_z,
                            float q_w,
                            float q_x,
                            float q_y,
                            float q_z) {
        super();

        myPosX = p_x;
        myPosY = p_y;
        myPosZ = p_z;

        myPressure = pres;

        myIsTriggerOn = is_trig_on == 1? true: false;
        myIsRightButtonOn = is_right_button_on== 1? true: false;
        myIsLeftButtonOn = is_left_button_on== 1? true: false;

        myIsTouchMoving = is_touch_moving== 1? true: false;
        myIsTouching = is_touching== 1? true: false;
        myIsHoverMoving = is_hover_moving== 1? true: false;
        myIsHovering = is_hovering== 1? true: false;

        myAccX = acc_x;
        myAccY = acc_y;
        myAccZ = acc_z;

        myQW = q_w;
        myQX = q_x;
        myQY = q_y;
        myQZ = q_z;


    }
}
