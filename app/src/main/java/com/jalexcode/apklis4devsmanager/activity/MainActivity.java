package com.jalexcode.apklis4devsmanager.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.material.navigation.NavigationView;
import com.jalexcode.apklis4devsmanager.R;
import com.jalexcode.apklis4devsmanager.adapter.AppItemAdapter;
import com.jalexcode.apklis4devsmanager.model.AppsReceivedInfo;
import com.jalexcode.apklis4devsmanager.util.Util;
import com.kaopiz.kprogresshud.KProgressHUD;

import cu.kareldv.apklis.api2.Session;
import cu.kareldv.apklis.api2.model.Application;
import cu.kareldv.apklis.api2.model.UserInfo;
import cu.kareldv.apklis.api2.model.UserPatchWizard;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private Session session;
    private KProgressHUD progressHUD;

    private RelativeLayout notificationButtonToolbar;
//    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView appsCount, downloadsCount, salesCountCount, totalMoneyCount, userNameDrawer, userPhoneNumberDrawer, cartBadge;
    private ImageView menu, notificationImage, userAvatarDrawer;
    private RecyclerView appsRecyclerView;
    private SearchView searchBar;

    private AppItemAdapter adapter;

    private DrawerLayout drawer;
    public static NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //
        setUpUI();
        //
        Intent currentSession = getIntent();
        if (currentSession != null){
            if (currentSession.hasExtra("session")) {
                Log.e("Getting intent", "Starting by previus session");
                session = (Session) currentSession.getSerializableExtra("session");
                new getDataThread().execute(session);
            }
        }
    }

    private void setUpUI(){
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setOnRefreshListener(() -> new getDataThread().execute(session));
        // DASHBOARD
        appsCount = findViewById(R.id.appsCount);
        downloadsCount = findViewById(R.id.downloadsCount);
        salesCountCount = findViewById(R.id.salesCountCount);
        totalMoneyCount = findViewById(R.id.totalMoneyCount);
        // TOOLBAR
        initNotifications();
        // RECYCLER VIEW
        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        //
        initSearchBar();
        //
        initDrawer();
        //
        initProgressView();
    }

    private void initNotifications(){
        notificationImage = findViewById(R.id.notificationImage);
        notificationButtonToolbar = findViewById(R.id.notificationButtonToolbar);
        notificationButtonToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                Toast.makeText(MainActivity.this, "Espere a que esta funcionalidad en próximas actualizaciones", Toast.LENGTH_SHORT).show();
            }
        });
        cartBadge = findViewById(R.id.cartBadge);
        //
        int notifCount = preferences.getInt("notificationsCount", 0);
        if (notifCount == 0){
            cartBadge.setVisibility(View.GONE);
            notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_none));
            return;
        }
        notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications));
        if(notifCount > 99){
            cartBadge.setText("99+");
        }else{
            cartBadge.setText(String.valueOf(notifCount));
        }
        cartBadge.setVisibility(View.VISIBLE);
    }

    private void initSearchBar(){
        searchBar = findViewById(R.id.searchBar);
        searchBar.setQueryHint("Buscar app...");
        searchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);;
                return true;
            }
        });
        searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (searchBar.getQuery() == "") {

                }
                return true;
            }
        });
    }

    private void initProgressView(){
        progressHUD = new KProgressHUD(this);
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        progressHUD.setCancellable(true);
        progressHUD.setAnimationSpeed(2);
        progressHUD.setDimAmount(0.5f);
    }

    private void initDrawer(){
        // drawer Layout
        drawer = findViewById(R.id.drawer_layout);
        // drawer Nav View
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.header_navigation_drawer);
        // DRAWER HEADER
        userNameDrawer = headerView.findViewById(R.id.userNameDrawer);
        userPhoneNumberDrawer = headerView.findViewById(R.id.userPhoneNumberDrawer);
        userAvatarDrawer = headerView.findViewById(R.id.userAvatarDrawer);
        //
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent i;
                Bundle b;
                switch (item.getItemId()){
                    case R.id.apklis_database:
                        i = new Intent(MainActivity.this, ApklisDatabaseActivity.class);
                        b = new Bundle();
                        b.putSerializable("session", session);
                        i.putExtras(b);
                        startActivity(i);
                        break;
                    case R.id.sales:
                        i = new Intent(MainActivity.this, SalesActivity.class);
                        b = new Bundle();
                        b.putSerializable("session", session);
                        i.putExtras(b);
                        startActivity(i);
                        break;
                    case R.id.feedback:
                        String debugInfo = "\n\n\n---";
                        debugInfo += "\nOS Version: " + System.getProperty("os.version") + " (" + Build.VERSION.INCREMENTAL + ")";
                        debugInfo += "\nAndroid API: " + Build.VERSION.SDK_INT;
                        debugInfo += "\nModel (Device): " + Build.MODEL + " ("+ Build.DEVICE + ")";
                        debugInfo += "\nManufacturer: " + Build.MANUFACTURER;
                        debugInfo += "\n---";

                        Intent intent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts("mailto", getString(R.string.feedback_email), null));

                        intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.feedback_email));
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, debugInfo);

                        startActivity(Intent.createChooser(intent, "Enviar Feedback usando..."));
                        break;
                    case R.id.telegram_channel:
                        String telgramUrl = ("https://t.me/portalusuario");
                        Intent telegramLauch = new Intent(Intent.ACTION_VIEW);
                        telegramLauch.setData(Uri.parse(telgramUrl));
                        startActivity(telegramLauch);
                        break;
                    // CONFIGURACION
                    case R.id.settings:
//                        i = new Intent(MainActivity.this, SettingsActivity.class);
//                        startActivity(i);
//                        break;
                    // CONFIGURACION
                    case R.id.about:
//                        i = new Intent(MainActivity.this, AboutActivity.class);
//                        startActivity(i);
//                        break;
                }
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
        menu = findViewById(R.id.menuButton);
        menu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
    }

    private class getDataThread extends AsyncTask<Session, Void, AppsReceivedInfo> { //AsyncTask<File, Void, TreeNode<DataNode>>
        private String error = "";
        @Override
        protected void onPostExecute(AppsReceivedInfo appsReceivedInfo) {
            super.onPostExecute(appsReceivedInfo);
            if (appsReceivedInfo != null) {
                UserInfo userInfo = appsReceivedInfo.getSession().getUserInfo();
                // DRAWER
                Glide.with(MainActivity.this).load(userInfo.getAvatar()).into(userAvatarDrawer);
                userAvatarDrawer.setOnClickListener(v -> showUserInfoDialog());
                userNameDrawer.setText(userInfo.getFullName());
                userPhoneNumberDrawer.setText(userInfo.getPhone());
                // DASHBOARD
                appsCount.setText(String.valueOf(appsReceivedInfo.getAppCountValue()));
                downloadsCount.setText(String.valueOf(appsReceivedInfo.getAppDownloadsValue()));
                salesCountCount.setText(String.valueOf(appsReceivedInfo.getAppSalesCountValue()));
                totalMoneyCount.setText(String.format("$ %.2f", appsReceivedInfo.getTotalMoneyValue()));
                // RECYCLER
                adapter = new AppItemAdapter(MainActivity.this);
                adapter.setApplications(session.getOwnApps());
                appsRecyclerView.setAdapter(adapter);
            }else{
                new ErrorDialog(error);
            }
//            swipeRefreshLayout.setRefreshing(false);
        }

        public AppsReceivedInfo doInBackground(Session...sessions) {
            Session session = sessions[0];
            /* INFORMACION PARA EL DASHBOARD */
            AppsReceivedInfo appsReceivedInfo = new AppsReceivedInfo();
            int appCountValue = 0;
            int appDownloadsValue = 0;
            int appSalesCountValue = 0;
            int totalMoneyValue = 0;
            try{
                for (Application app:
                        session.getOwnApps()){
                    appCountValue ++;
                    appDownloadsValue += app.getDownloadCount();
                    appSalesCountValue += app.getSaleCount();
                    int total = app.getSaleCount() * app.getPrice();
                    totalMoneyValue += total;
                }
            }catch(Throwable e){
                e.printStackTrace();
                error = e.getMessage();
            }
            //
            appsReceivedInfo.setAppCountValue(appCountValue);
            appsReceivedInfo.setAppDownloadsValue(appDownloadsValue);
            appsReceivedInfo.setAppSalesCountValue(appSalesCountValue);
            appsReceivedInfo.setTotalMoneyValue(Util.getDiscount(totalMoneyValue));
            appsReceivedInfo.setSession(session);
            //
            return appsReceivedInfo;
        }
    }

    public void showUserInfoDialog(){
        new UserInfoDialog();
    }

    public class UserInfoDialog {
        public UserInfoDialog(){
            final Dialog userInfoDialog = new Dialog(MainActivity.this);

            userInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            userInfoDialog.setCancelable(true);
            userInfoDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            userInfoDialog.setContentView(R.layout.dialog_user_details);

            CircleImageView userAvatarImage = userInfoDialog.findViewById(R.id.userAvatarImage);
            TextView userNameText = userInfoDialog.findViewById(R.id.appNameText);
            TextView userPhoneNumberText = userInfoDialog.findViewById(R.id.userPhoneNumberText);
            TextView fullNameText = userInfoDialog.findViewById(R.id.fullNameText);
            TextView emailText = userInfoDialog.findViewById(R.id.emailText);
            TextView idText = userInfoDialog.findViewById(R.id.idText);
            TextView regNumberText = userInfoDialog.findViewById(R.id.regNumberText);
            TextView provinceText = userInfoDialog.findViewById(R.id.provinceText);
            TextView cIDText = userInfoDialog.findViewById(R.id.CIDText);
            TextView businessText = userInfoDialog.findViewById(R.id.businessText);
            TextView descriptionText = userInfoDialog.findViewById(R.id.descriptionText);
            CheckBox developerCheck = userInfoDialog.findViewById(R.id.developerCheck);
            CheckBox activeProgrammerCheck = userInfoDialog.findViewById(R.id.activeProgrammerCheck);

            Button editUserButton = userInfoDialog.findViewById(R.id.editUserButton);
            editUserButton.setOnClickListener(v -> {

            });
            Button editDevButton = userInfoDialog.findViewById(R.id.editDevButton);
            editDevButton.setOnClickListener(v -> {

            });

            // PUT INFO
            UserInfo userInfo = session.getUserInfo();
            Glide.with(MainActivity.this).load(userInfo.getAvatar()).into(userAvatarImage);
            userNameText.setText(session.getUserName());
            userPhoneNumberText.setText(userInfo.getPhone());
            fullNameText.setText(userInfo.getFullName());
            emailText.setText(userInfo.getEmail());
            idText.setText(userInfo.getId());
            regNumberText.setText(userInfo.getRegNumber());
            provinceText.setText(getResources().getStringArray(R.array.provinces)[userInfo.getProvince().toNumber()-1]);
            cIDText.setText(userInfo.getCI());
            businessText.setText(userInfo.getBussiness());
            descriptionText.setText(userInfo.getDescription());
            developerCheck.setChecked(userInfo.isDeveloper());
            activeProgrammerCheck.setChecked(userInfo.isDeveloperActive());

            userInfoDialog.show();
        }
    }

    public class ErrorDialog {
        public ErrorDialog(String ex){
            StringBuilder errorMessage = new StringBuilder();
            //
            errorMessage.append(ex);
            //
            AlertDialog.Builder errorMessageDialog = new AlertDialog.Builder(MainActivity.this);
            errorMessageDialog.setCancelable(true)
                    .setMessage(errorMessage)
                    .setTitle("Ha ocurrido un error")
                    .setIcon(R.drawable.ic_error)
                    .setPositiveButton("Copiar", (dialogInterface, i) -> {
                        ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("feedback", errorMessage.toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, "Registro copiado al portapapeles", Toast.LENGTH_SHORT).show();
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