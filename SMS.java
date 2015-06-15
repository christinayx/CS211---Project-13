package com.example.haoshengzou.sms_test;

import android.app.Activity;
import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.gsm.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
//import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;


import static android.widget.Toast.LENGTH_SHORT;

public class SMS extends Activity {
    private static final String TAG = SMS.class.getSimpleName();

    Button btnSendSMS;
    CheckBox File_transfer;
    EditText txtPhoneNo;
    EditText txtMessage;
    EditText txtFileName;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
        File_transfer = (CheckBox) findViewById(R.id.File_transfer);
        txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        txtFileName = (EditText) findViewById(R.id.txtFileName);

        btnSendSMS.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String phoneNo = txtPhoneNo.getText().toString();
                String message = txtMessage.getText().toString();
                String filename= txtFileName.getText().toString();
                if(File_transfer.isChecked()){
                    if(phoneNo.length()>0 && filename.length()>0)
                    {
                        long cur_time=System.currentTimeMillis();
                        sendSMS(phoneNo, message+cur_time, filename);
                    }
                    else
                    {
                       Toast toast= Toast.makeText(getBaseContext(),
                               "Please enter both phone number and file name.",
                               LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 750);
                        toast.show();
                    }
                }
                else{
                    if(phoneNo.length()>0 && message.length()>0){
                        long cur_time=System.currentTimeMillis();
                        sendSMS(phoneNo, message+"***"+cur_time, "");
                    }
                    else {
                        Toast toast=Toast.makeText(getBaseContext(),
                                "Please enter both phone number and message.",
                                LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 750);
                        toast.show();
                    }
                }

            }
        });
    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message, String filename)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        if(filename.length()==0) {


            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }
        else
        {
            Log.d(TAG, "Send File...");
            File file = new File(Environment.getExternalStorageDirectory() + File.separator +filename);

            if(file.exists())
            {
                Log.d(TAG, "Start Splitting...");
                int partCounter = 0;
                int sizeOfFiles = 80;//140 bytes
                byte[] buffer = new byte[sizeOfFiles];
                try{
                   FileInputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator +filename);

                   int numfiles= (int)(file.length() / 80)+1;
                   SmsManager sms=SmsManager.getDefault();
                   long send_time = System.currentTimeMillis();
                   sms.sendTextMessage(phoneNumber,null,"#File Transfer Start#"+send_time, sentPI, deliveredPI);
                    Toast.makeText(getBaseContext(),"File Transfer Start", LENGTH_SHORT).show();
                   int nRead = 0;



                  while((nRead = inputStream.read(buffer))!=-1){
                       String buffermsg=Base64.encodeToString(buffer,0,nRead,Base64.DEFAULT);
                       String filemsg=filename+"@@@"+String.valueOf(numfiles)+"@@@"+String.valueOf(partCounter)+"####"+buffermsg;
                       SmsManager sms1=SmsManager.getDefault();
                       sms1.sendTextMessage(phoneNumber, null,filemsg, sentPI, deliveredPI);
                      // Toast.makeText(getBaseContext(),"sent"+String.valueOf(partCounter), LENGTH_SHORT).show();
                       partCounter++;
                   }
                   //SmsManager sms2=SmsManager.getDefault();
                   //sms2.sendTextMessage(phoneNumber, null,"#End#", sentPI, deliveredPI);
                   //Toast.makeText(getBaseContext(),"File Transfer Finish", LENGTH_SHORT).show();


               }catch(Exception e){}

            }else {
                Toast toast=Toast.makeText(getBaseContext(), "File does not exist", LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 750);
                toast.show();
            }

        }





    }



}

