package com.example.mask_info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mask_info.model.Store;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainViewModel viewModel;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                performAction();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

    }

    @SuppressLint("MissingPermission")
    private void performAction() {
        fusedLocationClient.getLastLocation()
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "performAction: " + e.getCause());
                })
                .addOnSuccessListener(this, location -> {
                    Log.d(TAG, "performAction: " + location);

                    //??????,?????? ?????? get
                    if (location != null) {
                        Log.d(TAG, "getLatitude: " + location.getLatitude());
                        Log.d(TAG, "getLongitude: " + location.getLongitude());

                        //???????????? ??? ??????(????????? ??????)
//                        location.setLatitude(37.266389);
//                        location.setLongitude(126.999333);

                        viewModel.location = location;
                        viewModel.fetchStoreInfo();
                    }
                });

        RecyclerView recyclerView = findViewById(R.id.rv_store);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        final StoreAdapter adapter = new StoreAdapter();
        recyclerView.setAdapter(adapter);

        //UI ?????? ?????? ????????????
        viewModel.itemLiveData.observe(this, stores -> {
            adapter.updateItems(stores);
            getSupportActionBar().setTitle("????????? ?????? ?????? ??? : " + stores.size() + "???");
        });

        //??????
        viewModel.loadingLiveData.observe(this, isLoading -> {
            if (isLoading) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //refresh ??????
                viewModel.fetchStoreInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private List<Store> mItems = new ArrayList<>();

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_addr;
        TextView tv_distance;
        TextView tv_remain;
        TextView tv_count;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_addr = itemView.findViewById(R.id.tv_addr);
            tv_distance = itemView.findViewById(R.id.tv_distance);
            tv_remain = itemView.findViewById(R.id.tv_remain);
            tv_count = itemView.findViewById(R.id.tv_count);
        }
    }

    //UI ??????
    public void updateItems(List<Store> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);

        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = mItems.get(position);

        holder.tv_name.setText(store.getName());
        holder.tv_addr.setText(store.getAddr());
        holder.tv_distance.setText(String.format("%.2fkm", store.getDistance()));

        String remainStat = "??????";
        String countStat = "100??? ??????";
        int color = Color.GREEN;
        switch (store.getRemainStat()) {
            case "plenty":
                remainStat = "??????";
                countStat = "100??? ??????";
                color = Color.GREEN;
                break;
            case "some":
                remainStat = "??????";
                countStat = "30??? ??????";
                color = Color.BLUE;
                break;
            case "few":
                remainStat = "?????? ??????";
                countStat = "2??? ??????";
                color = Color.RED;
                break;
            case "empty":
                remainStat = "?????? ??????";
                countStat = "1??? ??????";
                color = Color.GRAY;
                break;
            default:
        }
        holder.tv_remain.setText(remainStat);
        holder.tv_count.setText(countStat);

        holder.tv_remain.setTextColor(color);
        holder.tv_count.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}