package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public final class MatchUploader {
    private MatchUploader(){}
    public static void prompt(Activity activity,String uuid) {
        File file=new File(new File(activity.getFilesDir(),"offline-matches"),uuid+".json");
        if(!file.isFile()) {
            Toast.makeText(activity,"Match history missing; cannot upload",Toast.LENGTH_LONG).show();return;
        }
        if(SecureLogin.token(activity)==null){
            new AlertDialog.Builder(activity).setTitle("Match saved on device")
                .setMessage("Log in to Ludohojaye to submit this match for league approval. You can continue playing offline.")
                .setPositiveButton("LOGIN",(d,w)->activity.startActivity(new android.content.Intent(activity,WebLoginActivity.class)))
                .setNegativeButton("LATER",null).show();return;
        }
        try{
            byte[] content=readLimited(new FileInputStream(file),100000);
            JSONObject match=new JSONObject(new String(content,StandardCharsets.UTF_8));
            int league=match.optInt("leagueId",0);
            JSONArray players=match.getJSONArray("players");
            if(league<1) {
                new AlertDialog.Builder(activity).setTitle("Match saved locally")
                 .setMessage("No league was selected for this match. Select a league and registered players before starting your next league match.")
                 .setPositiveButton("OK",null).show();return;
            }
            for(int i=0;i<players.length();i++)
                if(players.getJSONObject(i).optInt("leaguePlayerId",0)<=0){
                    new AlertDialog.Builder(activity).setTitle("Match saved locally")
                      .setMessage("All seats must be mapped to registered league players before submission. This match remains saved locally.")
                      .setPositiveButton("OK",null).show();return;
                }
            new AlertDialog.Builder(activity).setTitle("Submit match to Ludohojaye?")
              .setMessage("Upload this finished match to the selected league? Scores will remain pending until a league administrator approves them.")
              .setPositiveButton("SUBMIT",(d,w)->submit(activity,match))
              .setNegativeButton("LATER",null).show();
        }catch(Exception e){
            Toast.makeText(activity,"Unable to read saved match",Toast.LENGTH_LONG).show();
        }
    }
    private static byte[] readLimited(InputStream in,int max) throws Exception {
        try(InputStream stream=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] chunk=new byte[4096];int n;
            while((n=stream.read(chunk))!=-1){
                out.write(chunk,0,n);
                if(out.size()>max)throw new java.io.IOException("Too large");
            }
            return out.toByteArray();
        }
    }
    private static void submit(Activity activity,JSONObject match) {
        new Thread(()->{
            HttpsURLConnection connection=null;String message;
            try{
                String token=SecureLogin.token(activity);
                if(token==null)throw new IllegalStateException("Login expired");
                connection=(HttpsURLConnection)new URL("https://ludohojaye.com/api/matches/mobile-offline").openConnection();
                connection.setRequestMethod("POST");connection.setDoOutput(true);
                connection.setConnectTimeout(15000);connection.setReadTimeout(15000);
                connection.setRequestProperty("Authorization","Bearer "+token);
                connection.setRequestProperty("Content-Type","application/json");
                byte[] payload=match.toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream output=connection.getOutputStream()){output.write(payload);}
                int http=connection.getResponseCode();
                if(http!=201&&http!=200)throw new IllegalStateException("Portal refused match (HTTP "+http+"). Match remains saved.");
                JSONObject response=new JSONObject(new String(readLimited(connection.getInputStream(),100000),StandardCharsets.UTF_8));
                message="Match received by portal. Status: "+response.optString("status","pending_approval")+". League admin approval required.";
            }catch(Exception e){message=e.getMessage()==null?"Upload failed. Match remains saved locally.":e.getMessage();}
            finally{if(connection!=null)connection.disconnect();}
            final String result=message;
            activity.runOnUiThread(()->new AlertDialog.Builder(activity).setTitle("Ludohojaye score submission")
              .setMessage(result).setPositiveButton("OK",null).show());
        }).start();
    }
}
