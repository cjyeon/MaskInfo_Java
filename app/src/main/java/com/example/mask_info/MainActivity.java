package com.example.mask_info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.mask_info.model.Store;
import com.example.mask_info.model.StoreInfo;
import com.example.mask_info.repository.MaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_store);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        StoreAdapter adapter = new StoreAdapter();
        recyclerView.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MaskService.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        MaskService service = retrofit.create(MaskService.class);

        Call<StoreInfo> storeInfoCall = service.fetchStoreInfo();

        //enqueue -> 비동기
        storeInfoCall.enqueue(new Callback<StoreInfo>() {
            @Override
            public void onResponse(Call<StoreInfo> call, Response<StoreInfo> response) {
                Log.d(TAG, "onResponse: refresh");
                List<Store> items = response.body().getStores();
                adapter.updateItems(items);

//                //아래 코드와 같은 의미
//                List<Store> result = new ArrayList<>();
//                for (int i = 0; i < items.size(); i++) {
//                    Store store = items.get(i);
//                    if (store.getRemainStat() != null) {
//                        result.add(store);
//                    }
//                }

                //remainStat 널값 필터링 후 리스트로
                adapter.updateItems(items
                        .stream()
                        .filter(item -> item.getRemainStat() != null)
                        .collect(Collectors.toList()));

                //액션바 타이틀 변경
                getSupportActionBar().setTitle("마스크 재고 있는 곳 : " + items.size() + "곳");
            }

            @Override
            public void onFailure(Call<StoreInfo> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
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
                //refresh
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

    //UI 갱신
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
        holder.tv_distance.setText("1.0km");

        String remainStat = "충분";
        String countStat = "100개 이상";
        int color = Color.GREEN;
        switch (store.getRemainStat()) {
            case "plenty":
                remainStat = "충분";
                countStat = "100개 이상";
                color = Color.GREEN;
                break;
            case "some":
                remainStat = "여유";
                countStat = "30개 이상";
                color = Color.BLUE;
                break;
            case "few":
                remainStat = "매진 임박";
                countStat = "2개 이상";
                color = Color.RED;
                break;
            case "empty":
                remainStat = "재고 없음";
                countStat = "1개 이하";
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