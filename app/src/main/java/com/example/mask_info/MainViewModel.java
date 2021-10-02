package com.example.mask_info;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mask_info.model.Store;
import com.example.mask_info.model.StoreInfo;
import com.example.mask_info.repository.MaskService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class MainViewModel extends ViewModel {
    public static final String TAG = MainViewModel.class.getSimpleName();
    //변경 가능한 LiveData -> 임시로 public(getter, setter로 하는 게 정석)
    public MutableLiveData<List<Store>> itemLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public Location location;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MaskService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build();

    private final MaskService service = retrofit.create(MaskService.class);

    //LiveData 사용하면 Callback 대체 가능
    public void fetchStoreInfo() {
        //로딩 시작 -> LiveData 활용
        loadingLiveData.setValue(true);

        //위도 경도 값 전달
        service.fetchStoreInfo(location.getLatitude(), location.getLongitude())
                .enqueue(new Callback<StoreInfo>() {
            @Override
            public void onResponse(Call<StoreInfo> call, Response<StoreInfo> response) {
                Log.d(TAG, "onResponse: refresh");

                //remainStat 널값 필터링
                List<Store> items = response.body().getStores()
                        .stream()
                        .filter(item -> item.getRemainStat() != null)
                        .filter(item -> !item.getRemainStat().equals("empty"))
                        .collect(Collectors.toList());

                for (Store store: items) {
                    double distance = LocationDistance.distance(
                            location.getLatitude(), location.getLongitude(), store.getLat(), store.getLng(), "k");
                    store.setDistance(distance);
                }

                Collections.sort(items);

                itemLiveData.postValue(items);

                //로딩 끝
                loadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(Call<StoreInfo> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                //에러 발생 시 빈 리스트 세팅
                itemLiveData.postValue(Collections.emptyList());

                //로딩 끝
                loadingLiveData.postValue(false);
            }
        });
    }
}
