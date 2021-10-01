package com.example.mask_info.repository;

import com.example.mask_info.model.StoreInfo;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MaskService {
//    String BASE_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";

    String BASE_URL = "https://gist.githubusercontent.com/junsuk5/bb7485d5f70974deee920b8f0cd1e2f0/raw/063f64d9b343120c2cb01a6555cf9b38761b1d94/";
//    "sample.json?lat=37.266389&lng=126.999333&m=5000"

    @GET("sample.json?")
    Call<StoreInfo> fetchStoreInfo();
}
