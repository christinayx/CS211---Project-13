package com.example.haoshengzou.sms_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.gsm.SmsMessage;
import android.util.Base64;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;


public class SmsReceiver extends BroadcastReceiver
{
    private static final String TAG = SMS.class.getSimpleName();
    private static String send_time="";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        String temp = "";
        long recv_time = 0;

        try {
        if (bundle != null) {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                temp += msgs[i].getMessageBody().toString();

            }
            recv_time=System.currentTimeMillis();

        }


    //File
        if(temp.indexOf("#File Transfer Start#")>-1)
        {

            send_time = temp.substring(("#File Transfer Start#").length()).trim();
        }
//        else if(temp.indexOf("#End#: ")>-1)
//        {
//
//        }
        else if(temp.indexOf("@@@")>-1)
        {
            String fname=temp.substring(0, temp.indexOf("@@@"));
            temp=temp.substring(temp.indexOf("@@@")+3, temp.length());
            int numfiles=Integer.parseInt(temp.substring(0, temp.indexOf("@@@")));
            temp=temp.substring(temp.indexOf("@@@")+3, temp.length());
            String seriesnumber = temp.substring(0, temp.indexOf("####"));
            String content = temp.substring(temp.indexOf("####") + 4);
            byte[] towrite = Base64.decode(content,Base64.DEFAULT);


            File file = new File(Environment.getExternalStorageDirectory() + File.separator +fname+"-"+ seriesnumber );


            if (!file.exists()) {
                file.createNewFile();

                FileOutputStream fos=new FileOutputStream(file);
                fos.write(towrite);
                fos.close();
            }

            boolean complete=true;

            for(int i=0; i<numfiles; i++)
            {
                File chunk = new File(Environment.getExternalStorageDirectory() + File.separator + fname+"-"+i);

                if(!chunk.exists()) {
                    complete = false;
                    break;
                }
            }

            if(complete)
            {
                File wfile=new File(Environment.getExternalStorageDirectory() + File.separator +"recv_"+fname);
                if (!wfile.exists()) {
                    wfile.createNewFile();
                }
                FileOutputStream fos1 = new FileOutputStream(wfile);
                for(int i=0; i<numfiles; i++)
                {
                    File subfile = new File(Environment.getExternalStorageDirectory() + File.separator+fname+"-"+i);
                    FileInputStream inputStream = new FileInputStream(subfile);
                    long file_size = subfile.length();
                    byte[] buffer = new byte[(int)file_size];
                    inputStream.read(buffer);
                    fos1.write(buffer);
                    subfile.delete();
                }
                fos1.close();
              //  long recv_time = System.currentTimeMillis();
                long diff = recv_time - Long.parseLong(send_time);
                File file_latency = new File(Environment.getExternalStorageDirectory()+ File.separator+"file_latency.txt");
                if(!file_latency.exists())
                {
                    file_latency.createNewFile();
                }
               String towritelatency = fname+": "+String.valueOf(diff);
//                PrintStream out = new PrintStream(file_latency);
//                out.println(towritelatency);

                BufferedWriter bW;
                bW = new BufferedWriter(new FileWriter(file_latency, true));
                bW.write(towritelatency);
                bW.newLine();
                bW.flush();
                bW.close();

                str="File received: "+fname;
                Toast toast=Toast.makeText(context, str, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 750);
                toast.show();

            }

        }
        //Message
            else{
            str=temp.substring(0, temp.indexOf("***"));
            String send_time=temp.substring(temp.indexOf("***")+3, temp.length());

            long diff = recv_time-Long.parseLong(send_time,10);
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "msg_latency.txt");
            String data=String.valueOf(diff);

            str="Message received : "+temp.substring(0, temp.indexOf("***"));
            Toast toast= Toast.makeText(context, str, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 750);
            toast.show();


            if (file.exists()) {

                BufferedWriter bW;

                bW = new BufferedWriter(new FileWriter(file, true));
                bW.write(data);
                bW.newLine();
                bW.flush();
                bW.close();

            }
            else{
                file.createNewFile();
                BufferedWriter bW;
                bW = new BufferedWriter(new FileWriter(file, true));
                bW.write(data);
                bW.newLine();
                bW.flush();
                bW.close();
            }
        }




        }catch(Exception e){

            System.out.println(e);
        }

            //---display the new SMS message---

        }

    /* Checks if external storage is available for read and write */





}
