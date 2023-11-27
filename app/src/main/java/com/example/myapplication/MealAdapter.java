package com.example.myapplication;


// MealAdapter.java

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Meal;
import com.google.gson.Gson;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private OnItemClickListener onItemClickListener;

    // 생성자에서 Meal 리스트와 OnItemClickListener를 전달받도록 수정
    public MealAdapter(List<Meal> mealList, OnItemClickListener onItemClickListener) {
        this.mealList = mealList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        // onBindViewHolder 메소드 내에서 각 항목에 대한 설정
        Meal meal = mealList.get(position);
        holder.foodNameTextView.setText(meal.getFoodName());
        holder.dateTextView.setText(meal.getDate());


        // 이미지 불러오기
        if (!TextUtils.isEmpty(meal.getImagePath())) {
            Glide.with(holder.itemView.getContext())
                    .load(meal.getImagePath())
                    .into(holder.foodImageView);
        } else {
            // 이미지가 없는 경우 기본 이미지 설정 또는 아무 작업을 하지 않습니다.
        }


        // '삭제' 버튼 클릭 리스너 설정
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 해당 항목 삭제
                int currentPosition = holder.getAdapterPosition();
                mealList.remove(currentPosition);
                notifyItemRemoved(currentPosition);

                // SharedPreferences에서도 삭제
                saveMealsToSharedPreferences(holder.itemView.getContext());
            }
        });

        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(mealList.get(holder.getAdapterPosition()));
            }
        });
    }




    // 저장된 Meal 리스트를 SharedPreferences에 저장하는 메소드
    private void saveMealsToSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("meal_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mealList);
        editor.putString("meal_list", json);
        editor.apply();
    }




    public interface OnItemClickListener {
        void onItemClick(Meal meal);
//        void onDeleteClick(Meal meal, int position);
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    // ViewHolder 클래스 정의
    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView;
        TextView dateTextView;
        ImageView foodImageView; // Add this line
        Button deleteButton;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            foodImageView = itemView.findViewById(R.id.foodImageView); // Add this line
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

}
