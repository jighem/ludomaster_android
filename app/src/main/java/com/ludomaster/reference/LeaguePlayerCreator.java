package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONObject;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/** Adds a human player through the portal's existing permission-controlled API. */
public final class LeaguePlayerCreator {
    public interface Done {void created();}
    private LeaguePlayerCreator(){}
    public static void show(Activity activity,int leagueId,Done done) {
        if(leagueId<=0){Toast.makeText(activity,"Select league first",Toast.LENGTH_LONG).show();return;}
        LinearLayout layout=new LinearLayout(activity);layout.setOrientation(1);layout.setPadding(40,12,40,12);
        EditText name=new EditText(activity);name.setHint("Player full name");layout.addView(name);
        EditText email=new EditText(activity);email.setHint("Player email (required for invitation)");
        email.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(email);
        new AlertDialog.Builder(activity).setTitle("Add player to this league").setView(layout)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("ADD PLAYER",(dialog,which)->{
                String n=name.getText().toString().trim(),e=email.getText().toString().trim();
                if(n.isEmpty()||!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()){
                    Toast.makeText(activity,"Enter a name and valid email",Toast.LENGTH_LONG).show();return;
                }
                create(activity,leagueId,n,e,done);
            }).show();
    }
    private static void create(Activity activity,int leagueId,String name,String email,Done done){
        new Thread(()->{
            HttpsURLConnection connection=null;String error=null;
            try{
                String token=SecureLogin.token(activity);
                if(token==null)throw new IllegalStateException("Sign in first");
                connection=(HttpsURLConnection)new URL("https://ludohojaye.com/api/players").openConnection();
                connection.setRequestMethod("POST");connection.setDoOutput(true);
                connection.setConnectTimeout(15000);connection.setReadTimeout(15000);
                connection.setRequestProperty("Authorization","Bearer "+token);
                connection.setRequestProperty("Content-Type","application/json");
                byte[] body=new JSONObject().put("league_id",leagueId).put("full_name",name)
                    .put("email",email).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=connection.getOutputStream()){out.write(body);}
                int status=connection.getResponseCode();
                InputStream source=status<400?connection.getInputStream():connection.getErrorStream();
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                if(source!=null)try(InputStream in=source){
                    byte[] buf=new byte[2048];int read;
                    while((read=in.read(buf))!=-1 && out.size()<32768)out.write(buf,0,read);
                }
                JSONObject response=new JSONObject(out.toString("UTF-8"));
                if(status<200||status>=300)
                    throw new IllegalStateException(response.optString("error","Player creation rejected (HTTP "+status+")"));
            }catch(Exception exception){error=exception.getMessage();}
            finally{if(connection!=null)connection.disconnect();}
            final String message=error;
            activity.runOnUiThread(()->{
                if(message==null){
                    new AlertDialog.Builder(activity).setTitle("Player added")
                        .setMessage("Player request accepted by Ludohojaye. An email invitation or authorization step may be required before the player can join.")
                        .setPositiveButton("REFRESH PLAYERS",(d,w)->done.created()).show();
                }else new AlertDialog.Builder(activity).setTitle("Unable to add player")
                    .setMessage(message).setPositiveButton("OK",null).show();
            });
        }).start();
    }
}
