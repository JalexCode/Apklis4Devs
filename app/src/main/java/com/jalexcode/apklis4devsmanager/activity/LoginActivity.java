package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jalexcode.apklis4devsmanager.R;
import com.kaopiz.kprogresshud.KProgressHUD;

import cu.kareldv.apklis.api2.Session;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameEditText, passwordEditText;
    private ImageView loginBackButton;
    private KProgressHUD progressHUD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //
        usernameEditText = findViewById(R.id.loginUserEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginBackButton = findViewById(R.id.loginBackButton);
        loginBackButton.setOnClickListener(v -> onBackPressed());
        //
        initProgressView();
    }

    private void initProgressView(){
        progressHUD = new KProgressHUD(this);
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        progressHUD.setCancellable(true);
        progressHUD.setAnimationSpeed(2);
        progressHUD.setDimAmount(0.5f);
    }
    
    public void letTheUserLoggedIn(View v){
        new logInThread().execute();
    }

    private class logInThread extends AsyncTask<Void, Void, Session> { //AsyncTask<File, Void, TreeNode<DataNode>>
        private String errorMessage = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD.setLabel("Entrando...");
            progressHUD.show();
            
        }

        @Override
        protected void onPostExecute(Session session) {
            super.onPostExecute(session);
            progressHUD.dismiss();
            //
            if (session != null){
                // save data
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", usernameEditText.getText().toString());
                editor.putString("password", passwordEditText.getText().toString());
                editor.apply();
                // launch main activity
                Intent mainActivityLaunch = new Intent(LoginActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("session", session);
                mainActivityLaunch.putExtras(b);
                startActivity(mainActivityLaunch);
                finish();
            }else{
                new ErrorDialog(errorMessage);
            }

        }

        public Session doInBackground(Void...voids) {
            String user = usernameEditText.getText().toString(),
                    pass = passwordEditText.getText().toString();
            Session s = null;
            try{
                s = new Session(user, pass);
                s.updateCache();
            }catch(Throwable e){
                e.printStackTrace();
                errorMessage = e.getMessage();
            }
            return s;
        }
    }

    public void callSignUpScreen(View view) {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.goToSignUp), "transition_signup");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    public class ErrorDialog {
        public ErrorDialog(String ex){
            StringBuilder errorMessage = new StringBuilder();
            //
            errorMessage.append(ex);
            //
            AlertDialog.Builder errorMessageDialog = new AlertDialog.Builder(LoginActivity.this);
            errorMessageDialog.setCancelable(true)
                    .setMessage(errorMessage)
                    .setTitle("Ha ocurrido un error")
                    .setIcon(R.drawable.ic_error)
                    .setPositiveButton("Copiar", (dialogInterface, i) -> {
                        ClipboardManager clipboard = (ClipboardManager) LoginActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("feedback", errorMessage.toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(LoginActivity.this, "Registro copiado al portapapeles", Toast.LENGTH_SHORT).show();
//                            Intent share = new Intent(Intent.ACTION_SEND);
//                            share.setType("text/plain");
//                            share.putExtra(Intent.EXTRA_SUBJECT, "Registro de actividades de Portal Usuario");
//                            share.putExtra(Intent.EXTRA_TEXT, log);
//                            startActivity(Intent.createChooser(share, "Enviar registro de Portal Usuario"));
                    })
                    .setNegativeButton("Atrás", null);
            errorMessageDialog.show();
        }

    }
}