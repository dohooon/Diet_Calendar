package com.example.myapplication.model;

// Meal.java

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Meal implements Comparable<Meal> {
    private String foodName;
    private String date;
    private String time;
    private String review;
    private Integer cost;
    private String mealType;
    private String imagePath;
    private Integer calory;
    private String location;

    public Meal(String foodName, String date, String time, String mealType, String review,
                Integer cost, String imagePath, Integer calory, String location) {
        this.foodName = foodName;
        this.date = date;
        this.time = time;
        this.mealType = mealType;
        this.review = review;
        this.cost = cost;
        this.imagePath = imagePath;
        this.calory = calory;
        this.location = location;

    }

    public String getFoodName() {
        return foodName;
    }

    public String getDate() {
        return date;
    }

    public String getMealType() {
        return mealType;
    }

    public String getTime() {
        return time;
    }

    public String getReview() {
        return review;
    }

    public Integer getCost() {
        return cost;
    }

    public String getImagePath() {
        return imagePath;
    } // 추가: 이미지 파일 경로 반환

    public Integer getCalory() {
        return calory;
    }

    public String getLocation() {
        return location;
    }


    @Override
    public int compareTo(Meal otherMeal) {
        // 최근 날짜가 먼저 나오도록 정렬
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date thisDate = dateFormat.parse(this.date);
            Date otherDate = dateFormat.parse(otherMeal.date);
            return otherDate.compareTo(thisDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // 날짜를 비교할 수 없는 경우
        }
    }


}
