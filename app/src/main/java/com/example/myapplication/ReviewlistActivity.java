package com.example.myapplication;

// ReviewListActivity.java

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class ReviewlistActivity extends AppCompatActivity {

    // 메인 액티비티에서 런타임 권한 요청
    private static final int REQUEST_PERMISSION_CODE = 123;

    private LineChart lineChartView;
    private RecyclerView recyclerView;
    private Button addReviewButton;
    private TextView caloryTextView;
    private TextView breakfastTextView;
    private TextView lunchTextView;
    private TextView dinnerTextView;
    private TextView coffeeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlist);


        recyclerView = findViewById(R.id.recyclerView);
        addReviewButton = findViewById(R.id.addReviewButton);
        caloryTextView = findViewById(R.id.caloryTextView);
        breakfastTextView = findViewById(R.id.breakfastTextView);
        lunchTextView = findViewById(R.id.lunchTextView);
        dinnerTextView = findViewById(R.id.dinnerTextView);
        coffeeTextView = findViewById(R.id.coffeeTextView);
        lineChartView = findViewById(R.id.lineChartView);

        // 저장된 Meal 리스트 불러오기
        List<Meal> mealList = loadMeals();

        // 최근 날짜 순으로 정렬 (역순)
        Collections.sort(mealList);

        // RecyclerView 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // 최근 30일 간의 데이터 필터링
        List<Meal> last30DaysMeals = filterMealsByDate(mealList, 30);

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

        // 총 칼로리 및 비용 표시
        displayTotalStats(last30DaysMeals);
        displayCaloriesOverTime(last30DaysMeals);
    }

    // 최근 30일 간의 데이터 필터링
    private List<Meal> filterMealsByDate(List<Meal> meals, int days) {
        List<Meal> filteredMeals = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (Meal meal : meals) {
            try {
                Date mealDate = new SimpleDateFormat("yyyy-MM-dd").parse(meal.getDate());
                calendar.setTime(mealDate);

                // 현재 날짜와 비교하여 days 이내의 데이터만 추가
                if (daysAgo(calendar.getTime()) <= days) {
                    filteredMeals.add(meal);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filteredMeals;
    }

    // 현재 날짜로부터 days 이전인지 확인
    private int daysAgo(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);

        long diffMillis = today.getTimeInMillis() - specifiedDate.getTimeInMillis();
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    // 저장된 Meal 리스트 불러오기
    private List<Meal> loadMeals() {
        SharedPreferences sharedPreferences = getSharedPreferences("meal_preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("meal_list", null);
        Type type = new TypeToken<ArrayList<Meal>>() {
        }.getType();

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

    // 총 칼로리 및 비용 표시
    private void displayTotalStats(List<Meal> meals) {
        int totalCalories = 0;
        int totalBreakfastCost = 0;
        int totalLunchCost = 0;
        int totalDinnerCost = 0;
        int totalCoffeeCost = 0;

        for (Meal meal : meals) {
            totalCalories += meal.getCalory();

            switch (meal.getMealType()) {
                case "조식":
                    totalBreakfastCost += meal.getCost();
                    break;
                case "중식":
                    totalLunchCost += meal.getCost();
                    break;
                case "석식":
                    totalDinnerCost += meal.getCost();
                    break;
                case "음료":
                    totalCoffeeCost += meal.getCost();
                    break;
            }
        }

        caloryTextView.setText("총 칼로리: " + totalCalories);
        breakfastTextView.setText("조식 비용: " + totalBreakfastCost);
        lunchTextView.setText("중식 비용: " + totalLunchCost);
        dinnerTextView.setText("석식 비용: " + totalDinnerCost);
        coffeeTextView.setText("음료 비용: " + totalCoffeeCost);
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
        reviewTypeTextView.setText("타입 : " + meal.getMealType());
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

    private void displayCaloriesOverTime(List<Meal> meals) {
        List<Entry> entries = new ArrayList<>(); // 날짜별 칼로리 섭취
        List<Entry> cumulativeEntries = new ArrayList<>(); // 누적 분포 함수
        int cumulativeCalories = 0;

        for (Meal meal : meals) {
            try {
                Date mealDate = new SimpleDateFormat("yyyy-MM-dd").parse(meal.getDate());
                int daysAgo = daysAgo(mealDate);
                int calories = meal.getCalory();
                cumulativeCalories += calories; // 누적 칼로리 합계 갱신
                entries.add(new Entry(daysAgo, meal.getCalory()));
                cumulativeEntries.add(new Entry(daysAgo, cumulativeCalories));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LineDataSet dataSet = new LineDataSet(entries, "칼로리 섭취량");
        LineData lineData = new LineData(dataSet);
        lineChartView.setData(lineData);
        lineChartView.getDescription().setEnabled(false); // chart 밑에 description 표시 없애기


        // 누적 분포 함수 데이터셋과 데이터 추가
        LineDataSet cumulativeDataSet = new LineDataSet(cumulativeEntries, "누적 칼로리");
        cumulativeDataSet.setColor(Color.RED); // 누적 분포 함수 선의 색상 설정
        cumulativeDataSet.setCircleColor(Color.RED);
        lineData.addDataSet(cumulativeDataSet);

        XAxis xAxis = lineChartView.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis yAxisLeft = lineChartView.getAxisLeft();
        yAxisLeft.setDrawLabels(false); // label 삭제
        YAxis yAxis = lineChartView.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value){
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -((int) value));
                return new SimpleDateFormat("MM-dd").format(calendar.getTime());
            }
        });
       lineChartView.invalidate();
    }
}
