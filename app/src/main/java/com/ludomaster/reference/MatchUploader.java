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
    public static String localStatus(Activity activity,String uuid) {
        try {
            File file=new File(new File(activity.getFilesDir(),"offline-matches"),uuid+".json");
            JSONObject match=new JSONObject(new String(readLimited(new FileInputStream(file),100000),StandardCharsets.UTF_8));
            if("uploaded".equals(match.optString("syncStatus")))
                return "Uploaded | Match #"+match.optString("serverMatchId")+" | "+match.optString("serverStatus");
            return "Saved locally — NOT uploaded | Session "+uuid;
        } catch(Exception e){return "Match receipt unavailable";}
    }
    public static void showSavedMatches(Activity activity) {
        File directory=new File(activity.getFilesDir(),"offline-matches");
        File[] files=directory.listFiles((dir,name)->name.endsWith(".json"));
        if(files==null || files.length==0){
            new AlertDialog.Builder(activity).setTitle("Saved matches").setMessage("No completed games saved on this device.")
                .setPositiveButton("OK",null).show();return;
        }
        java.util.Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));
        int count=Math.min(files.length,30);
        String[] labels=new String[count];
        for(int i=0;i<count;i++){
            String uuid=files[i].getName().replace(".json","");
            labels[i]=localStatus(activity,uuid);
        }
        new AlertDialog.Builder(activity).setTitle("Saved match uploads")
            .setItems(labels,(d,idx)->{
                String uuid=files[idx].getName().replace(".json","");
                prompt(activity,uuid);
            })
            .setNeutralButton("REFRESH STATUS",(d,w)->refreshSavedStatuses(activity,files,count))
            .setPositiveButton("CLOSE",null).show();
    }
    private static void refreshSavedStatuses(Activity activity,File[] files,int count){
        final String token=SecureLogin.token(activity);
        if(token==null){
            Toast.makeText(activity,"Log in to refresh approval status",Toast.LENGTH_LONG).show();return;
        }
        new Thread(()->{
            int updated=0,failures=0;
            for(int i=0;i<count;i++){
                try{
                    File file=files[i];
                    JSONObject match=new JSONObject(new String(readLimited(new FileInputStream(file),100000),StandardCharsets.UTF_8));
                    if(!"uploaded".equals(match.optString("syncStatus")))continue;
                    final String uuid=match.optString("clientMatchId","");
                    if(!uuid.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))continue;
                    HttpsURLConnection conn=null;
                    try{
                        conn=(HttpsURLConnection)new URL("https://ludohojaye.com/api/matches/mobile-offline/status/"+uuid).openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(10000);
                        conn.setRequestProperty("Authorization","Bearer "+token);
                        if(conn.getResponseCode()!=200)throw new java.io.IOException("HTTP "+conn.getResponseCode());
                        JSONObject response=new JSONObject(new String(readLimited(conn.getInputStream(),100000),StandardCharsets.UTF_8));
                        String newStatus=response.getString("status");
                        if(!java.util.Arrays.asList("pending_approval","approved","rejected").contains(newStatus))
                            throw new java.io.IOException("Unexpected server status");
                        String id=response.getString("matchId");
                        if(!id.equals(match.optString("serverMatchId")))
                            throw new java.io.IOException("Receipt match ID mismatch");
                        if(!newStatus.equals(match.optString("serverStatus"))){
                            match.put("serverStatus",newStatus);
                            File temp=new File(file.getParentFile(),file.getName()+".tmp");
                            try(java.io.FileOutputStream output=new java.io.FileOutputStream(temp)){
                                output.write(match.toString(2).getBytes(StandardCharsets.UTF_8));
                                output.getFD().sync();
                            }
                            if(!temp.renameTo(file)){
                                if(!file.delete() || !temp.renameTo(file))
                                    throw new java.io.IOException("Unable to persist refreshed receipt");
                            }
                            updated++;
                        }
                    }finally{if(conn!=null)conn.disconnect();}
                }catch(Exception ignored){failures++;}
            }
            final int changed=updated,failed=failures;
            activity.runOnUiThread(()->{
                Toast.makeText(activity,"Status refreshed: "+changed+" changed"+(failed>0?", "+failed+" unavailable":""),Toast.LENGTH_LONG).show();
                showSavedMatches(activity);
            });
        }).start();
    }
    public static void prompt(Activity activity,String uuid) { prompt(activity,uuid,null); }
    public static void prompt(Activity activity,String uuid,Runnable refresh) {
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
            if ("uploaded".equals(match.optString("syncStatus"))) {
                new AlertDialog.Builder(activity).setTitle("Already submitted")
                    .setMessage("Match ID: "+match.optString("serverMatchId")+"; status: "+match.optString("serverStatus"))
                    .setPositiveButton("OK",null).show();return;
            }
            int league=match.optInt("leagueId",0);
            JSONArray players=match.getJSONArray("players");
            if(league<1) {
                new AlertDialog.Builder(activity).setTitle("Match saved locally")
                 .setMessage("No league was selected for this match. Select a league and registered players before starting your next league match.")
                 .setPositiveButton("OK",null).show();return;
            }
            for(int i=0;i<players.length();i++)
                if(players.getJSONObject(i).optInt("leaguePlayerId",0)<=0 && !players.getJSONObject(i).optBoolean("isBot",false)){
                    new AlertDialog.Builder(activity).setTitle("Match saved locally")
                      .setMessage("Every human player must be selected from the league roster before submission. Computer 1-3 are supported automatically. Match remains saved locally.")
                      .setPositiveButton("OK",null).show();return;
                }
            new AlertDialog.Builder(activity).setTitle("Submit match to Ludohojaye?")
              .setMessage("Upload this finished match to the selected league? Scores will remain pending until a league administrator approves them.")
              .setPositiveButton("SUBMIT",(d,w)->submit(activity,match,uuid,refresh))
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
    private static void submit(Activity activity,JSONObject match,String uuid,Runnable refresh) {
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
                if(http!=201&&http!=200) {
                    InputStream error=connection.getErrorStream();
                    String detail=error==null?"":new String(readLimited(error,100000),StandardCharsets.UTF_8);
                    throw new IllegalStateException("Portal refused match (HTTP "+http+"): "+detail);
                }
                JSONObject response=new JSONObject(new String(readLimited(connection.getInputStream(),100000),StandardCharsets.UTF_8));
                String status=response.optString("status","unknown");
                String id=response.optString("matchId","not returned");
                String leagueName=match.optString("leagueName","League #"+match.optInt("leagueId"));
                match.put("syncStatus","uploaded");
                match.put("serverMatchId",id);
                match.put("serverStatus",status);
                File file=new File(new File(activity.getFilesDir(),"offline-matches"),uuid+".json");
                File temp=new File(file.getParentFile(),uuid+".tmp");
                try(java.io.FileOutputStream output=new java.io.FileOutputStream(temp)){
                    output.write(match.toString(2).getBytes(StandardCharsets.UTF_8));output.getFD().sync();
                }
                if(!temp.renameTo(file)){
                    if(!file.delete() || !temp.renameTo(file)) throw new java.io.IOException("Upload succeeded but local receipt could not be saved. Match ID: "+id);
                }
                if("approved".equals(status))
                    message="Scores published to "+leagueName+". Match ID: "+id+". Sign in at ludohojaye.com to view standings.";
                else if("pending_approval".equals(status))
                    message="Scores submitted to "+leagueName+". Match ID: "+id+". Awaiting league admin approval. Sign in at ludohojaye.com to check.";
                else message="Portal received match "+id+" with status "+status+". Check ludohojaye.com.";
            }catch(Exception e){message=e.getMessage()==null?"Upload failed. Match remains saved locally.":e.getMessage();}
            finally{if(connection!=null)connection.disconnect();}
            final String result=message;
            activity.runOnUiThread(()->{
                if(refresh!=null)refresh.run();
                new AlertDialog.Builder(activity).setTitle("Ludohojaye score submission")
                  .setMessage(result).setPositiveButton("OK",null).show();
            });
        }).start();
    }
}
