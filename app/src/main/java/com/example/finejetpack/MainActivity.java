package com.example.finejetpack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.example.finejetpack.view.AppBottomBar;
import com.example.finejetpack.utils.NavGraphBuilder;
import com.example.libnetwork.api.ApiResponse;
import com.example.libnetwork.callback.JsonCallback;
import com.example.libnetwork.request.GetRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController = null;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();*/

        /*NavController是页面导航，跳转的核心类，入口类
        * NavHostFragment在做内容切换时将此功能委托给NavController*/
        // navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
        /*NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);*/

        // 用了这个之后，就不需要activity_main中的app:navGraph="@navigation/mobile_navigation"
        NavGraphBuilder.build(navController, this, fragment.getId());
        navView.setOnNavigationItemSelectedListener(this);

        // 测试网络请求接口
        /*GetRequest<JSONObject> request = new GetRequest<>("https://www.mooc.com");
        request.execute();
        request.execute(new JsonCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                super.onSuccess(response);
            }
        });*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        navController.navigate(menuItem.getItemId());
        return !TextUtils.isEmpty(menuItem.getTitle());
    }
}
