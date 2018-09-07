package com.example.aruny1.blue2ser;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;
import com.macroyau.blue2serial.BluetoothSerialRawListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener, BluetoothSerialRawListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;


    //private ScrollView svTerminal;
    private TextView tvTerminal;
    private TextView tvTerminal2;
    private TextView tvTerminal3;
    private TextView tvTerminal4;
    private EditText etSend;
    ToggleButton toggleButton;

    private MenuItem actionConnect, actionDisconnect;

    private boolean crlf = false;

    String totalString = "";

    TextView textView2;

    long lastMessageEmittedTime = System.currentTimeMillis();

    ControllerPacket lastControllerPacket = null;




    String serverIP = null;
    // Member elements



    //////////
    float x_coord = 0.0f;
    float y_coord = 0.0f;

    int last_x_coord_int;
    int last_y_coord_int;

    View.OnClickListener myButtonClickListener;

    ImageView primaryCircleHoverView;
    ImageView primaryCircleTouchView;
    ViewGroup mRootLayout;

    int last_action = MotionEvent.INVALID_POINTER_ID;
    int current_action = MotionEvent.INVALID_POINTER_ID;

    TextView myTextView;
    public android.os.Handler Handler;

    //////
    HoverCursorManager myHoverCursorManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        // Find UI views and set listeners
        //svTerminal = (ScrollView) findViewById(R.id.terminal);
        tvTerminal = (TextView) findViewById(R.id.tv_terminal1);
        tvTerminal2 = findViewById(R.id.tv_terminal2);
        tvTerminal3 = findViewById(R.id.tv_terminal3);
        tvTerminal4 = findViewById(R.id.tv_terminal4);

        toggleButton = findViewById(R.id.toggleButton);

        etSend = (EditText) findViewById(R.id.et_send);
        etSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String send = etSend.getText().toString().trim();
                    if (send.length() > 0) {
                        bluetoothSerial.write(send, crlf);
                        etSend.setText("");
                    }
                }
                return false;
            }
        });

        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);

        processPacket("00 00 00 00 00 00 07 00 03 00 00 00 00 3C 6D 4A 99 BE FE B5 80 3F 5E 09 EA BB B0 06 80");


        ///////////////

        ///setup the network stuff


        ///////
        mRootLayout = (ViewGroup) findViewById(R.id.root);

        primaryCircleHoverView = findViewById(R.id.primary_icon_hover);
        primaryCircleTouchView = findViewById(R.id.primary_icon_touch);
        primaryCircleTouchView.setVisibility(View.INVISIBLE);

        myHoverCursorManager = new HoverCursorManager(primaryCircleHoverView, primaryCircleTouchView, mRootLayout, this, true);


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        //bluetoothSerial.stop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terminal, menu);

        actionConnect = menu.findItem(R.id.action_connect);
        actionDisconnect = menu.findItem(R.id.action_disconnect);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            showDeviceListDialog();
            return true;
        } else if (id == R.id.action_disconnect) {
            bluetoothSerial.stop();
            return true;
        } else if (id == R.id.action_crlf) {
            crlf = !item.isChecked();
            item.setChecked(crlf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        if (bluetoothSerial == null)
            return;

        // Show or hide the "Connect" and "Disconnect" buttons on the app bar
        if (bluetoothSerial.isConnected()) {
            if (actionConnect != null)
                actionConnect.setVisible(false);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(true);
        } else {
            if (actionConnect != null)
                actionConnect.setVisible(true);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = getString(R.string.status_connecting);
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = getString(R.string.status_connected, bluetoothSerial.getConnectedDeviceName());
                break;
            default:
                subtitle = getString(R.string.status_disconnected);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle(R.string.paired_devices);
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    /* Implementation of BluetoothSerialListener */

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.no_bluetooth)
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        Log.d("MainActivity", " onBluetoothDeviceConnected "+ name + " address " + address);

        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        //Log.d("MainActivity", " onBluetoothSerialRead "+ message);
        // Print the incoming message on the terminal screen
        /*tvTerminal.append(getString(R.string.terminal_message_template,
                bluetoothSerial.getConnectedDeviceName(),
                message));
        svTerminal.post(scrollTerminalToBottom);*/



        //Log.d("MainActivity", toHex(message));

    }



    @Override
    public void onBluetoothSerialWrite(String message) {
        // Print the outgoing message on the terminal screen
        /*tvTerminal.append(getString(R.string.terminal_message_template,
                bluetoothSerial.getLocalAdapterName(),
                message));
        svTerminal.post(scrollTerminalToBottom);*/

    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

    /* End of the implementation of listeners */

    /*private final Runnable scrollTerminalToBottom = new Runnable() {
        @Override
        public void run() {
            // Scroll the terminal screen to the bottom
            svTerminal.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };*/

    public String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }


    @Override
    public void onBluetoothSerialReadRaw(byte[] bytes) {
        //Log.d("MainActivity", " onBluetoothSerialReadRaw "+ bytes.toString());



        String s="";
        try {
            s = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        //Log.d("MainActivity",  "before totalString = "+ totalString + " length = "+ totalString.length() );

        //Log.d("MainActivity", "incoming bytes = " + sb.toString() + " length = "+ sb.toString().length() );

        tvTerminal.setText(sb.toString());

        totalString += sb.toString();

        //Log.d("MainActivity",  "after totalString = "+ totalString + " length = "+ totalString.length() );


        if(totalString.length() >= 66){//No point in processing this string until it has atleast the size of a packet

            String[] strings_post_header = totalString.split("FA 5F ");

            //Log.d("MainActivity", " number of strings_post_header = "+ strings_post_header.length);


            for(int i = 0; i < strings_post_header.length; i++){

                String string_post_header = strings_post_header[i];
                //Log.d("MainActivity", " at index " + i + " string_post_header = " + string_post_header + "  length = " + string_post_header.length());


                if( string_post_header.length() >= 90){//Only consider a string_post_header if it has adequate bytes

                    //Log.d("MainActivity ", " char at 87 = "+ string_post_header.charAt(87) + " char at 88 = "+ string_post_header.charAt(88)  + " char at 89 = "+ string_post_header.charAt(89) );

                    if(string_post_header.charAt(87) == 'E' && string_post_header.charAt(88) == 'E'){

                        String[] packets = new String[2];
                        packets[0] = string_post_header.substring(0,86);
                        packets[1] = string_post_header.substring(89,string_post_header.length()-1);


                        for(int j = 0; j < packets.length; j++){
                            String packet = packets[j];
                            //Log.d("MainActivity", " at index " + j +" packet = " + packet + "  packet = " + packet.length());



                            processPacket(packet);


                            //If you are the last substring and not a complete packet
                            if(j == packets.length -1){

                                if(packet.length()<90){
                                    //Log.d("MainActivity", " replacing totalString " + totalString + " with  " + packet );
                                    totalString = packet;
                                }
                            }


                        }




                    }else {
                            //Log.d("MainActivity", " string_post_header = " + string_post_header + " does not contain EE 00");

                            if (i == strings_post_header.length - 1) {

                                //Log.d("MainActivity", " replacing totalString " + totalString + "with  " + string_post_header);
                                totalString = string_post_header;

                            }

                    }


                }else{



                    //If I'm the last string_post_header and my size is not long enough for a packet, make me totalString so I can carry on to next frame
                    if(i == strings_post_header.length-1 ){

                        //Log.d("MainActivity", " replacing totalString " + totalString + "with  " + string_post_header );
                        totalString = string_post_header;

                    }
                }


            }


        }



    }

    @Override
    public void onBluetoothSerialWriteRaw(byte[] bytes) {

    }

    void processPacket(String packet){


        if(packet.length() == 86) {

            String[] byte_strings = packet.split(" ");
            //Log.d("MainActivity", " number of byte_strings = "+ byte_strings.length);

            for( String byte_string : byte_strings){
                //Log.d("MainActivity", " byte_string = "+ byte_string + " byte_string length = "+ byte_string.length() );
            }

            //char[] chars = packet.toCharArray();
            //char header1 = chars[0];
            //char header2 = chars[1];

            //Integer.decode(packet.substring(0, ))
            //char status = chars[1];
            //char imu = chars[2];
            //char tail = chars[5];

            //Log.d("MainActivity", " header1 = "+ header1 + " header2 = "+ header2 + " position = "+ position + " status = "+ status + " imu = "+ imu + " tail = "+ tail);

            //Log.d("MainActivity", " position = "+ position + " status = "+ status + " imu = "+ imu );

            String byte_string_pos_x_lsb  = byte_strings[0];
            String byte_string_pos_x_msb = byte_strings[1];
            String byte_string_pos_y_lsb  = byte_strings[2];
            String byte_string_pos_y_msb = byte_strings[3];
            String byte_string_pos_z = byte_strings[4];
            String byte_string_pressure = byte_strings[5];
            String byte_string_status = byte_strings[6];
            String byte_string_acc_x_lsb = byte_strings[7];
            String byte_string_acc_x_msb = byte_strings[8];
            String byte_string_acc_y_lsb = byte_strings[9];
            String byte_string_acc_y_msb = byte_strings[10];
            String byte_string_acc_z_lsb = byte_strings[11];
            String byte_string_acc_z_msb = byte_strings[12];
            String byte_string_q_w_lsb = byte_strings[13];
            String byte_string_q_w_2 = byte_strings[14];
            String byte_string_q_w_3 = byte_strings[15];
            String byte_string_q_w_msb = byte_strings[16];

            String byte_string_q_x_lsb = byte_strings[17];
            String byte_string_q_x_2 = byte_strings[18];
            String byte_string_q_x_3 = byte_strings[19];
            String byte_string_q_x_msb = byte_strings[20];

            String byte_string_q_y_lsb = byte_strings[21];
            String byte_string_q_y_2 = byte_strings[22];
            String byte_string_q_y_3 = byte_strings[23];
            String byte_string_q_y_msb = byte_strings[24];

            String byte_string_q_z_lsb = byte_strings[25];
            String byte_string_q_z_2 = byte_strings[26];
            String byte_string_q_z_3 = byte_strings[27];
            String byte_string_q_z_msb = byte_strings[28];

            /*Log.d("MainActivity",
                    " byte_string_pos_x_lsb = " + byte_string_pos_x_lsb
                    + " byte_string_pos_x_msb = " + byte_string_pos_x_msb
                    + " byte_string_pos_y_lsb = " + byte_string_pos_y_lsb
                    + " byte_string_pos_y_msb = " + byte_string_pos_y_msb
                    + " byte_string_pos_z = " + byte_string_pos_z
                    + " byte_string_pressure = " + byte_string_pressure
                    + " byte_string_status = " + byte_string_status
                    + " byte_string_acc_x_lsb = " + byte_string_acc_x_lsb
                    + " byte_string_acc_x_msb = " + byte_string_acc_x_msb
                    + " byte_string_acc_y_lsb = " + byte_string_acc_y_lsb
                    + " byte_string_acc_y_msb = " + byte_string_acc_y_msb
                    + " byte_string_acc_z_lsb = " + byte_string_acc_z_lsb
                    + " byte_string_acc_z_msb = " + byte_string_acc_z_msb

                    + " byte_string_q_w_lsb = " + byte_string_q_w_lsb
                    + " byte_string_q_w_2 = " + byte_string_q_w_2
                    + " byte_string_q_w_3 = " + byte_string_q_w_3
                    + " byte_string_q_w_msb = " + byte_string_q_w_msb

                    + " byte_string_q_x_lsb = " + byte_string_q_x_lsb
                    + " byte_string_q_x_2 = " + byte_string_q_x_2
                    + " byte_string_q_x_3 = " + byte_string_q_x_3
                    + " byte_string_q_x_msb = " + byte_string_q_x_msb


                    + " byte_string_q_y_lsb = " + byte_string_q_y_lsb
                    + " byte_string_q_y_2 = " + byte_string_q_y_2
                    + " byte_string_q_y_3 = " + byte_string_q_y_3
                    + " byte_string_q_y_msb = " + byte_string_q_y_msb

                    + " byte_string_q_z_lsb = " + byte_string_q_z_lsb
                    + " byte_string_q_z_2 = " + byte_string_q_z_2
                    + " byte_string_q_z_3 = " + byte_string_q_z_3
                    + " byte_string_q_z_msb = " + byte_string_q_z_msb


            );*/

            int posX = Integer.parseInt(byte_string_pos_x_msb + byte_string_pos_x_lsb, 16);
            int posY = Integer.parseInt(byte_string_pos_y_msb + byte_string_pos_y_lsb, 16);
            int posZ = Integer.parseInt(byte_string_pos_z, 16);

            tvTerminal3.setText("p "+ posX + " , " + posY + " , "+ posZ);


            //Log.d("MainActivity", " posX = "+ posX + " posY = "+ posY + " posZ = "+ posZ + " coords = ("+ (float)posX/1300.0f+" , "+ (float)posY/1500.0f+")" );
            int pressure = Integer.parseInt(byte_string_pressure, 16);

            int status = Integer.parseInt(byte_string_status, 16);
            //Log.d("MainActivity", " pressure = "+ pressure + " status = "+ status );

            int isTriggerOn =  getBit(status, 0);
            int isRightButtonOn = getBit(status, 1);
            int isLeftButtonOn = getBit(status, 2);
            //bit 3 is nothing
            int isTouchMoving = getBit(status, 4);
            int isTouching = getBit(status, 5);
            int isHoverMoving = getBit(status, 6);
            int isHovering = getBit(status, 7);


            /*Log.d("MainActivity", " isTriggerOn = "+ isTriggerOn
                    + " isRightButtonOn = "+ isRightButtonOn
                    + " isLeftButtonOn = "+ isLeftButtonOn
                    + " isTouchMoving = "+ isTouchMoving
                    + " isTouching = "+ isTouching
                    + " isHoverMoving = "+ isHoverMoving
                    + " isHovering = "+ isHovering);*/

            float acc_x = (float)(Integer.parseInt(byte_string_acc_x_msb + byte_string_acc_x_lsb, 16))/65535.0f;
            float acc_y = (float)(Integer.parseInt(byte_string_acc_y_msb + byte_string_acc_y_lsb, 16))/65535.0f;
            float acc_z = (float)(Integer.parseInt(byte_string_acc_y_msb + byte_string_acc_y_lsb, 16))/65535.0f;

            //float gyr_x = Float.intBitsToFloat(safeLongToInt(Long.parseLong(byte_string_gyr_x_msb + byte_string_gyr_x_lsb, 16)));
            //float gyr_y = Float.intBitsToFloat(safeLongToInt(Long.parseLong(byte_string_gyr_y_msb + byte_string_gyr_y_lsb, 16)));
            //float gyr_z = Float.intBitsToFloat(safeLongToInt(Long.parseLong(byte_string_gyr_y_msb + byte_string_gyr_y_lsb, 16)));


            Long q_w_i = Long.parseLong(byte_string_q_w_lsb + byte_string_q_w_2 + byte_string_q_w_3 + byte_string_q_w_msb, 16);
            Float q_w_f = Float.intBitsToFloat(q_w_i.intValue());
            //Log.d("MainActivity", " q_w_f = "+ q_w_f);

            Long q_x_i = Long.parseLong(byte_string_q_x_lsb + byte_string_q_x_2 + byte_string_q_x_3 + byte_string_q_x_msb, 16);
            Float q_x_f = Float.intBitsToFloat(q_x_i.intValue());
            //Log.d("MainActivity", " q_x_f = "+ q_x_f);

            Long q_y_i = Long.parseLong(byte_string_q_y_lsb + byte_string_q_y_2 + byte_string_q_y_3 + byte_string_q_y_msb, 16);
            Float q_y_f = Float.intBitsToFloat(q_y_i.intValue());
            //Log.d("MainActivity", " q_y_f = "+ q_y_f);

            Long q_z_i = Long.parseLong(byte_string_q_z_lsb + byte_string_q_z_2 + byte_string_q_z_3 + byte_string_q_z_msb, 16);
            Float q_z_f = Float.intBitsToFloat(q_z_i.intValue());
            //Log.d("MainActivity", " q_z_f = "+ q_z_f);

            float q = (q_w_f * q_w_f + q_x_f * q_x_f + q_y_f * q_y_f + q_z_f * q_z_f);

            //Log.d("MainActivity", " q = "+ q);

            //Log.d("MainActivity", " quaternion = "+ q_x_f + " , "+ q_y_f + " "+ q_z_f + " "+ q_w_f+ " magnitude = "+ q);

            tvTerminal2.setText("q "+ roundTwoDecimals(q_x_f) + " , "+ roundTwoDecimals(q_y_f) + " "+ roundTwoDecimals(q_z_f) + " "+ roundTwoDecimals(q_w_f)+ " m = "+ roundTwoDecimals(q));



            /*int q_w_msb = Integer.parseInt(byte_string_q_w_msb, 16);
            int q_w_3 = Integer.parseInt(byte_string_q_w_3,   16);
            int q_w_2 = Integer.parseInt(byte_string_q_w_2,   16);
            int q_w_lsb = Integer.parseInt(byte_string_q_w_lsb, 16);

            float q_w = Float.intBitsToFloat(   q_w_msb << 24 |
                                                q_w_3 << 16 |
                                                q_w_2 << 8  |
                                                q_w_lsb      );

            Log.d("MainActivity", " q_w_msb = "+ q_w_msb + " q_w_3 = "+ q_w_3 + " q_w_2 = "+ q_w_2 + " q_w_lsb = "+ q_w_lsb + " q_w = "+ q_w);

            int q_x_msb = Integer.parseInt(byte_string_q_x_msb, 16);
            int q_x_3 = Integer.parseInt(byte_string_q_x_3,   16);
            int q_x_2 = Integer.parseInt(byte_string_q_x_2,   16);
            int q_x_lsb = Integer.parseInt(byte_string_q_x_lsb, 16);

            float q_x = Float.intBitsToFloat(   q_x_msb << 24 |
                                                q_x_3 << 16 |
                                                q_x_2 << 8  |
                                                q_x_lsb        );

            Log.d("MainActivity", " q_x_msb = "+ q_x_msb + " q_x_3 = "+ q_x_3 + " q_x_2 = "+ q_x_2 + " q_x_lsb = "+ q_x_lsb + " q_x = "+ q_x);

            int q_y_msb = Integer.parseInt(byte_string_q_y_msb, 16);
            int q_y_3 = Integer.parseInt(byte_string_q_y_3,   16);
            int q_y_2 = Integer.parseInt(byte_string_q_y_2,   16);
            int q_y_lsb = Integer.parseInt(byte_string_q_y_lsb, 16);

            float q_y = Float.intBitsToFloat(   q_y_msb << 24 |
                                                q_y_3 << 16 |
                                                q_y_2 << 8  |
                                                q_y_lsb        );

            Log.d("MainActivity", " q_y_msb = "+ q_y_msb + " q_y_3 = "+ q_y_3 + " q_y_2 = "+ q_y_2 + " q_y_lsb = "+ q_y_lsb + " q_y = "+ q_y);

            int q_z_msb = Integer.parseInt(byte_string_q_z_msb, 16);
            int q_z_3 = Integer.parseInt(byte_string_q_z_3,   16);
            int q_z_2 = Integer.parseInt(byte_string_q_z_2,   16);
            int q_z_lsb = Integer.parseInt(byte_string_q_z_lsb, 16);

            float q_z = Float.intBitsToFloat(   q_z_msb << 24 |
                                                q_z_3 << 16 |
                                                q_z_2 << 8  |
                                                q_z_lsb        );

            float q = (q_w * q_w + q_x * q_x + q_y * q_y + q_z * q_z);

            Log.d("MainActivity", " q_z_msb = "+ q_z_msb + " q_z_3 = "+ q_z_3 + " q_z_2 = "+ q_z_2 + " q_z_lsb = "+ q_z_lsb + " q_z = "+ q_z);

            Log.d("MainActivity", " q_w = "+ q_w + " q_x = "+ q_x + " q_y = "+ q_y + " q_z = "+ q_z + " q = "+ q);*/

            //Log.d("MainActivity", " acc_x = "+ acc_x + " acc_y = "+ acc_y + " acc_z = "+ acc_z);
            //Log.d("MainActivity", " gyr_x = "+ gyr_x + " gyr_y = "+ gyr_y + " gyr_z = "+ gyr_z);

            ControllerPacket controllerPacket = new ControllerPacket(
                   // 1.0f - (float)posX/1300.0f,
                   // 1.0f - (float)posY/1500.0f,
                    (float)posX/1000.0f,
                    (float)posY/1000.0f,
                    (float) posZ/10.0f,
                     pressure,
                    isTriggerOn,
                    isRightButtonOn,
                    isLeftButtonOn,
                    isTouchMoving,
                    isTouching,
                    isHoverMoving,
                    isHovering,
                    acc_x,
                    acc_y,
                    acc_z,
                    q_w_f,
                    q_x_f,
                    q_y_f,
                    q_z_f

            );

            JSONObject jsonObject = PacketsInputManager.getInstance().updateControllerPacket(controllerPacket);

            long currentTimeMillis = System.currentTimeMillis();
            long delta = currentTimeMillis - lastMessageEmittedTime;
            //Log.d("MainActivity", " time elapsed since last message emit = "+ delta + " currentTimeMillis = "+currentTimeMillis + " lastMessageEmittedTime = "+ lastMessageEmittedTime);


            //Log.d("MainActivity", " toggleButton.isActivated() = "+ toggleButton.isActivated());

            if(jsonObject!=null) {
                //Log.d("UDP", " jsonObject!=null "+ jsonObject.toString());

                if(toggleButton.isChecked()) {
                    //sendJSONAsStringToApp(jsonObject.toString());
                    //myClient.updateBufTo(String.valueOf(System.currentTimeMillis()));


                    int action = -1;
                    try {
                        action = jsonObject.getInt("action");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /*if(action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_MOVE){

                        if(delta> 30){
                            myClient.updateBufTo(jsonObject.toString());
                            //new Thread(myClient).start();
                            myClientThread.run();
                        }
                    }else{
                        myClient.updateBufTo(jsonObject.toString());
                        //new Thread(myClient).start();
                        myClientThread.run();
                    }*/


                }else{
                    //SocketIOManager.getInstance().emit(this, "new message", jsonObject);
                    //SocketIOManager.getInstance().emit(this, "message", jsonObject);
                }

                //Log.d("UDP", " about to send jsonObject "+ jsonObject.toString() + " via UDP");

                onJSONObjectMessageReceived(jsonObject);

            }else{
                //Log.d("MainActivity", " jsonObject is null ");
            }

            JSONObject imuDataJsonObject = PacketsInputManager.getInstance().getTrackingInfoAsJsonObject(controllerPacket);
            if(imuDataJsonObject!=null) {

                if(toggleButton.isChecked()){
                    //sendJSONAsStringToApp(imuDataJsonObject.toString());
                }else{
                    //SocketIOManager.getInstance().emit(this, "new message", imuDataJsonObject);
                    //SocketIOManager.getInstance().emit(this, "message", imuDataJsonObject);
                }


            }


            /*JSONObject buttonsJsonObject = PacketsInputManager.getInstance().getButtonEventAsJsonObject(controllerPacket);
            if(buttonsJsonObject!=null){

                SocketIOManager.getInstance().emit(this, "new message", buttonsJsonObject);
                SocketIOManager.getInstance().emit(this, "message", buttonsJsonObject);
            }*/


            lastMessageEmittedTime = currentTimeMillis;
            if(tvTerminal4!=null)
            tvTerminal4.setText(Long.toString(delta));






        }else{


            //Log.e("MainActivity", " packet size incorrect "+ packet.length());
        }


    }

    int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static int toLittleEndian(final String hex) {
        int ret = 0;
        String hexLittleEndian = "";
        if (hex.length() % 2 != 0) return ret;
        for (int i = hex.length() - 2; i >= 0; i -= 2) {
            hexLittleEndian += hex.substring(i, i + 2);
        }
        ret = Integer.parseInt(hexLittleEndian, 16);
        return ret;
    }


    float roundTwoDecimals(float d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(d));
    }


    /////////////////////////////////




    public void onJSONObjectMessageReceived(JSONObject jsonObject) {

        //Log.d("MainActivity", "onEvent JSONObjectMessageReceivedEvent event = " + jsonObject.toString());

        try {

            String label = jsonObject.getString("label");

            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;

            //Log.d("rht2d", " onJSONObjectMessageReceivedEvent screen width =  "+width + " height = "+ height );

            myHoverCursorManager.processMessage(jsonObject, width, height);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    };

}