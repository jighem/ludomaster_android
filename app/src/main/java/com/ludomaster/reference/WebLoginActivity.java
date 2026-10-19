package com.ludomaster.reference;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** First-party HTTPS login. No Facebook, Google, Play Games or old game servers. */
public final class WebLoginActivity extends Activity {
    private Button signIn;
    private TextView status;
    private EditText username, password;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(30),dp(24),dp(30),dp(24));
        root.setBackgroundResource(R.drawable.lhj_menu_sky);
        ImageView shield = new ImageView(this);
        shield.setImageResource(R.drawable.lhj_emblem_approved);
        root.addView(shield,new LinearLayout.LayoutParams(dp(150),dp(150)));
        TextView title = new TextView(this);
        title.setText("Ludo Ho Jaye!!!");
        title.setTypeface(null,Typeface.BOLD);
        title.setTextSize(26);
        title.setTextColor(0xff172B54);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText("Sign in with your ludohojaye.com account");
        subtitle.setTextColor(0xff516481);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(12),0,dp(20));
        root.addView(subtitle);
        username = new EditText(this);
        username.setSingleLine(true);
        username.setHint("Username or email");
        username.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        styleInput(username);
        root.addView(username,new LinearLayout.LayoutParams(-1,dp(58)));
        password = new EditText(this);
        password.setSingleLine(true);
        password.setHint("Password");
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        styleInput(password);
        root.addView(password,new LinearLayout.LayoutParams(-1,dp(58)));
        signIn = new Button(this);
        signIn.setText("Sign In");
        signIn.setAllCaps(false);
        signIn.setTextColor(Color.WHITE);
        signIn.setTypeface(null,Typeface.BOLD);
        signIn.setBackgroundResource(R.drawable.lhj_button_blue);
        LinearLayout.LayoutParams signInParams=new LinearLayout.LayoutParams(-1,dp(60));
        signInParams.topMargin=dp(18);
        root.addView(signIn,signInParams);
        status = new TextView(this);
        status.setGravity(Gravity.CENTER);
        status.setTextColor(0xff172B54);
        root.addView(status);
        Button offline = new Button(this);
        offline.setText("Back to offline play");
        offline.setOnClickListener(v -> finish());
        offline.setAllCaps(false);
        offline.setBackgroundResource(R.drawable.lhj_button_white);
        offline.setTextColor(0xff172B54);
        root.addView(offline);
        Button register=new Button(this);
        register.setText("DON'T HAVE AN ACCOUNT? REGISTER AT LUDOHOJAYE.COM");
        register.setAllCaps(false);
        register.setOnClickListener(v->startActivity(new android.content.Intent(
            android.content.Intent.ACTION_VIEW,android.net.Uri.parse("https://ludohojaye.com/"))));
        register.setBackgroundResource(R.drawable.lhj_button_white);
        register.setTextColor(0xff172B54);
        root.addView(register);
        TextView powered=new TextView(this);
        powered.setText("Powered by ludohojaye.com");
        powered.setTextColor(0xff2586F5);
        powered.setGravity(Gravity.CENTER);
        root.addView(powered);
        signIn.setOnClickListener(v -> submit());
        setContentView(root);
    }
    private void styleInput(EditText field) {
        field.setTextColor(0xff172B54);
        field.setHintTextColor(0xff64748B);
        field.setPadding(dp(12),dp(4),dp(12),dp(4));
        field.setBackgroundResource(R.drawable.lhj_button_white);
    }
    private int dp(int n) {return Math.round(n*getResources().getDisplayMetrics().density);}
    private void submit() {
        String id=username.getText().toString().trim();
        String pass=password.getText().toString();
        if(id.isEmpty() || pass.isEmpty()) {status.setText("Enter username and password.");return;}
        signIn.setEnabled(false);
        status.setText("Signing in securely...");
        new Thread(() -> {
            HttpsURLConnection connection=null;
            try {
                URL url=new URL("https://ludohojaye.com/api/auth/login");
                connection=(HttpsURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                byte[] payload=new JSONObject().put("username",id).put("password",pass)
                    .toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=connection.getOutputStream()){out.write(payload);}
                int code=connection.getResponseCode();
                if(code != 200)throw new IllegalStateException("Login unsuccessful ("+code+"). Verify your Ludohojaye credentials.");
                StringBuilder body=new StringBuilder();
                try(InputStream input=connection.getInputStream()) {
                    byte[] bytes=new byte[4096];int n;
                    while((n=input.read(bytes))!=-1 && body.length()<65536)
                        body.append(new String(bytes,0,n,StandardCharsets.UTF_8));
                }
                JSONObject reply=new JSONObject(body.toString());
                JSONObject user=reply.getJSONObject("user");
                if(reply.optString("token").isEmpty())throw new IllegalStateException("Server returned no session token.");
                String name=user.optString("name",id);
                SecureLogin.save(this, reply.getString("token"), name);
                // Keep only display name and login status in non-sensitive preferences.
                getSharedPreferences("LudoModUser",MODE_PRIVATE).edit()
                    .putString("username",name).putBoolean("logged",true).apply();
                runOnUiThread(() -> {
                    password.setText("");
                    status.setText("Welcome, "+name);
                    signIn.setEnabled(true);
                    setResult(RESULT_OK);
                    finish();
                });
            } catch(Exception e) {
                String message=e.getMessage()==null?"Check your connection and try again.":e.getMessage();
                runOnUiThread(()->{status.setText(message);signIn.setEnabled(true);});
            } finally {if(connection!=null)connection.disconnect();}
        }).start();
    }
}
