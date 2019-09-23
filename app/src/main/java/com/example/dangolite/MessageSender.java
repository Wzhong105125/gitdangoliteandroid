package com.example.dangolite;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.UnknownHostException;


public class MessageSender extends AsyncTask <String, String ,Void> {
    Socket s;
    PrintWriter pw;
    BufferedReader br;
    String read = null;

    @Override
    protected Void doInBackground(String... voids) {

            try{
                String ip = voids[0];
                Integer port = Integer.parseInt(voids[1]);
                Log.e("MessageSender","ip in messageSender"+ip+"port"+port);
                try {
                    s = new Socket(ip,port);
                }catch (NumberFormatException e){
                    Log.e("Socket Error","Error"+e.toString());
                    return null;
                }

                pw = new PrintWriter(s.getOutputStream());
                br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                pw.write(MainActivity.signrandom);
                pw.flush();
                Log.v("send message",MainActivity.signrandom);
                read = br.readLine();
                pw.close();
                br.close();
                s.close();
                return null;
            }catch (IOException e){
                Log.e("my app", e.toString());

        }

        return null;
    }


}

