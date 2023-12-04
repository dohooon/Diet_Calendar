package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;



import com.example.myapplication.model.Meal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;






public class MainActivity extends AppCompatActivity {

    private MealAdapter mealAdapter;
    private List<Meal> mealList;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setSelectedDate(CalendarDay.today());
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

    // 식사 리뷰 작성 버튼 클릭 시 호출되는 메소드
    public void goToReviewActivity(View view) {
        Intent intent = new Intent(this, ReviewActivity.class);
        startActivity(intent);
    }

    // 식사 리뷰 작성 버튼 클릭 시 호출되는 메소드
    public void goToReviewListActivity(View view) {
        Intent intent = new Intent(this, ReviewlistActivity.class);
        startActivity(intent);
    }



}
