package com.demologin.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.demologin.BaseActivity;
import com.demologin.R;
import com.demologin.ui.login.LoginActivity;
import com.demologin.ui.signup.SignUpActivity;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends BaseActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_logout) {
                if (prefs.isFacebookLoggedIn()) {
                    fbSignOut();
                } else if (prefs.isGoogleLoggedIn()) {
                    signOut();
                } else {
                    logOut();
                }
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                if (prefs.isFacebookLoggedIn()) {
                    fbSignOut();
                } else if (prefs.isGoogleLoggedIn()) {
                    signOut();
                } else {
                    logOut();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void fbSignOut() {
        if (null != mAuth)
            mAuth.signOut();
        LoginManager.getInstance().logOut();

        logOut();
    }

    private void signOut() {
        // Firebase sign out
        if (null != mAuth)
            mAuth.signOut();

        // Google sign out
        if (null != mGoogleSignInClient)
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        prefs.clearAll();
                        logOut();
                    });
    }

    private void logOut() {
        prefs.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
