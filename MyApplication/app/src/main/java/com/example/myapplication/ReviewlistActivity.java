package com.example.myapplication;

// ReviewListActivity.java

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.example.myapplication.model.Meal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewlistActivity extends AppCompatActivity {

    // 메인 액티비티에서 런타임 권한 요청
    private static final int REQUEST_PERMISSION_CODE = 123;

    private RecyclerView recyclerView;
    private Button addReviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlist);

        recyclerView = findViewById(R.id.recyclerView);
        addReviewButton = findViewById(R.id.addReviewButton);

        // 저장된 Meal 리스트 불러오기
        List<Meal> mealList = loadMeals();

        // 최근 날짜 순으로 정렬 (역순)
        Collections.sort(mealList);

        // RecyclerView 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // 런타임 퍼미션 체크 및 요청




        // OnItemClickListener를 설정하고 전달
        MealAdapter adapter = new MealAdapter(mealList, new MealAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Meal meal) {
                showMealDialog(meal);
            }
        });

        recyclerView.setAdapter(adapter);

        // 뒤로 가기 버튼 설정
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 식사 리뷰 작성 버튼 설정
        addReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToReviewActivity();
            }
        });


    }




    // 저장된 Meal 리스트 불러오기
    private List<Meal> loadMeals() {
        SharedPreferences sharedPreferences = getSharedPreferences("meal_preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("meal_list", null);
        Type type = new TypeToken<ArrayList<Meal>>() {}.getType();

        if (json != null) {
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    // 식사 리뷰 작성 액티비티로 이동
    private void goToReviewActivity() {
        Intent intent = new Intent(this, ReviewActivity.class);
        startActivity(intent);
    }



    private void showMealDialog(Meal meal) {
        // Use AlertDialog to show details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Meal Details");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        // Set meal details in dialog
        TextView foodNameTextView = dialogView.findViewById(R.id.dialogFoodNameTextView);
        TextView dateTextView = dialogView.findViewById(R.id.dialogDateTextView);
        TextView timeTextView = dialogView.findViewById(R.id.dialogTimeTextView);
        TextView reviewTextView = dialogView.findViewById(R.id.dialogReviewTextView);
        TextView costTextView = dialogView.findViewById(R.id.dialogCostTextView);
        TextView reviewTypeTextView = dialogView.findViewById(R.id.dialogReviewTypeTextView);
        // dialog_layout.xml에서 ImageView 추가한 부분과 매칭되는 ID 사용
        ImageView foodImageView = dialogView.findViewById(R.id.dialogFoodImageView);
        TextView caloryTextView = dialogView.findViewById(R.id.dialogCaloryTextView);


        // 런타임 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            } else {
                // meal.getImagePath()는 이미지 파일의 경로입니다.
                if (!TextUtils.isEmpty(meal.getImagePath())) {
                    // Glide를 사용하여 이미지 로드 및 설정
                    Glide.with(this)
                            .load(meal.getImagePath())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("Glide", "Image load failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("Glide", "Image loaded successfully");
                                    return false;
                                }

                            })
                            .into(foodImageView);

                } else {
                    // 이미지가 없는 경우 기본 이미지 설정 또는 아무 작업을 하지 않습니다.
                }
            }
        }


        foodNameTextView.setText("음식 이름 : " + meal.getFoodName());
        dateTextView.setText("날짜 : " + meal.getDate());
        timeTextView.setText("시간 : " + meal.getTime());

        reviewTextView.setText("리뷰 : " + meal.getReview());
        costTextView.setText("비용 : " + meal.getCost());
        reviewTypeTextView.setText("타입 : "+ meal.getMealType());
        caloryTextView.setText("칼로리 : " + meal.getCalory());

        // Add more details...

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click if needed
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //이 부분은 모르겠는 걸
    public void onItemClick(Meal meal) {
        // Handle item click, show a dialog with meal details
        showMealDialog(meal);
    }
}
