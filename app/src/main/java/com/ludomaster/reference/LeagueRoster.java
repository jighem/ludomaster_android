package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/** League and roster lookup, authorized with the user's encrypted website session. */
public final class LeagueRoster {
    public interface Callback { void loaded(JSONArray array); void failed(String message); }
    private LeagueRoster() {}
    public static void fetch(Activity activity, String resource, Callback callback) {
        String token=SecureLogin.token(activity);
        if(token==null) {
            callback.failed("Sign in to ludohojaye.com to select league players.");
            return;
        }
        new Thread(() -> {
            HttpsURLConnection conn=null;
            try {
                URL url=new URL("https://ludohojaye.com/api/"+resource);
                conn=(HttpsURLConnection)url.openConnection();
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);
                conn.setRequestProperty("Authorization","Bearer "+token);
                if(conn.getResponseCode()!=200)
                    throw new IllegalStateException("League lookup failed ("+conn.getResponseCode()+").");
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                try(InputStream input=conn.getInputStream()){
                    byte[] buf=new byte[4096];int n;
                    while((n=input.read(buf))!=-1) {
                        bytes.write(buf,0,n);
                        if(bytes.size()>1024*1024)throw new IllegalStateException("Roster response too large.");
                    }
                }
                JSONObject obj=new JSONObject(bytes.toString("UTF-8"));
                JSONArray data=obj.getJSONArray(resource.startsWith("leagues")?"leagues":"players");
                activity.runOnUiThread(()->callback.loaded(data));
            } catch(Exception e) {
                String msg=e.getMessage()==null?"Check connection and login.":e.getMessage();
                activity.runOnUiThread(()->callback.failed(msg));
            } finally {if(conn!=null)conn.disconnect();}
        }).start();
    }
}
