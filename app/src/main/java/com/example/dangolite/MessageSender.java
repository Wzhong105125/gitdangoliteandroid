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
    DataOutputStream dos;
    PrintWriter pw;
    BufferedReader br;
    BufferedWriter bw;
    String read = null;

    @Override
    protected Void doInBackground(String... voids) {
        if(MainActivity.Information.equals("K")){
            String ip = voids[0];
            Integer port = Integer.parseInt(voids[1]);
            Log.e("MessageSender","ip in messageSender"+ip+"port"+port);
            try{
             s = new Socket(ip,port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                pw = new PrintWriter(s.getOutputStream());
                pw.write(MainActivity.encryptedread);
                pw.flush();
                Log.e("OutPut:",MainActivity.encryptedread);
                pw.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }else{
            try{

                String message = "123";
                Log.v("tag","do in background");
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
                pw.write(message);
                pw.flush();
                Log.e("OutPut:","output 123");
                read = br.readLine();
                Log.e("input",read);

                MainActivity.Read = read;
                MainActivity.newread.set(read);
//            Log.v("new read value",MainActivity.newread.get());
                pw.close();
                br.close();
                s.close();
                return null;
            }catch (IOException e){
                Log.e("my app", e.toString());
            }
        }

        return null;
    }


}

