package com.example.myapplication;

// ReviewActivity.java

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myapplication.model.Meal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReviewActivity extends AppCompatActivity {

    private Spinner locationSpinner;
    private ImageView foodImageView;
    private EditText foodNameEditText, reviewEditText, timeEditText, dateEditText, costEditText;

    private RadioGroup mealTypeRadioGroup;
    private Uri selectedImageUri; // 이미지 URI를 저장할 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // UI 요소 초기화
        locationSpinner = findViewById(R.id.locationSpinner);
        foodImageView = findViewById(R.id.foodImageView);
        foodNameEditText = findViewById(R.id.foodNameEditText);
        reviewEditText = findViewById(R.id.reviewEditText);
        timeEditText = findViewById(R.id.timeEditText);
        dateEditText = findViewById(R.id.dateEditText);
        costEditText = findViewById(R.id.costEditText);
        mealTypeRadioGroup = findViewById(R.id.mealTypeRadioGroup);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        // 시간 선택 팝업 설정
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        // 날짜 선택 팝업 설정
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        // 저장 버튼 설정
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 내용을 가져와서 저장하는 코드 추가
                saveMeal(v);
            }
        });
    }

    // 날짜 선택 팝업 메소드
    public void showDatePickerDialog(View view) {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog를 생성하고 날짜 선택 리스너를 설정
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 선택된 날짜를 dateEditText에 설정
                        dateEditText.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                    }
                },
                currentYear,
                currentMonth,
                currentDay);

        // 팝업을 띄우기
        datePickerDialog.show();
    }

    // 시간 선택 팝업 메소드
    public void showTimePickerDialog(View view) {
        final Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog를 생성하고 시간 선택 리스너를 설정
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 선택된 시간을 timeEditText에 설정
                        timeEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                },
                currentHour,
                currentMinute,
                true);

        // 팝업을 띄우기
        timePickerDialog.show();
    }
    private static final int PICK_IMAGE_REQUEST = 1;
    // 음식 사진 선택 메소드
    public void selectFoodImage(View view) {
        // 갤러리 열기
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 갤러리에서 선택한 이미지 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // 선택한 이미지를 URI에서 가져오기
            selectedImageUri = data.getData();
            Log.d("ReviewActivity", "Selected Image URI: " + selectedImageUri);

            // URI에서 이미지를 로드하고 ImageView에 설정
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                foodImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ReviewActivity", "Error loading image");
            }
        }
    }



    // 저장 버튼 클릭 시 호출되는 메소드
    public void saveMeal(View view) {
        // 사용자가 입력한 데이터 가져오기
        String foodName = foodNameEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String review = reviewEditText.getText().toString();
        String costString = costEditText.getText().toString();
        String selectedLocation = locationSpinner.getSelectedItem().toString();
        Log.e("ReviewActivity", selectedLocation);
        // 추가: 필수 입력값이 비어 있는지 확인
        if (TextUtils.isEmpty(foodName) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(review) || TextUtils.isEmpty(costString)) {
            Toast.makeText(this, "모든 필수 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return; // 필수 입력값이 비어 있으면 저장하지 않음
        }

        int cost = Integer.parseInt(costString);

        // 라디오 버튼에서 선택된 항목 가져오기
        RadioButton selectedRadioButton = findViewById(mealTypeRadioGroup.getCheckedRadioButtonId());
        String mealType = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";

        // 추가: 이미지를 선택하지 않은 경우
        if (selectedImageUri == null) {
            Toast.makeText(this, "음식 이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
            return; // 이미지를 선택하지 않았으면 저장하지 않음
        }

        // 이미지 파일 경로 가져오기
        String imagePath = selectedImageUri != null ? getRealPathFromURI(selectedImageUri) : "";

        // 내부 저장소에 이미지 복사
        String destinationImagePath = copyImageToInternalStorage(imagePath);

        Integer calory = RandomCalories();

        // 저장된 데이터 불러오기
        List<Meal> mealList = loadMeals();

        // 새로운 Meal 객체 생성
        Log.e("ReviewActivity", selectedLocation);
        Meal meal = new Meal(foodName, date, time, mealType, review, cost, imagePath, calory, selectedLocation);


        // 리스트에 추가
        mealList.add(meal);
        // 변경된 리스트를 저장
        saveMeals(mealList);

        // 다른 작업 수행...
        Log.d("ReviewActivity", "Navigating to MainActivity");

        // ReviewActivity 종료
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Log.d("ReviewActivity", "Selected Image URI: " + selectedImageUri);
        Log.d("ReviewActivity", "Selected Image Path: " + getRealPathFromURI(selectedImageUri));

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
    // Meal 리스트 저장하기
    private void saveMeals(List<Meal> mealList) {
        SharedPreferences sharedPreferences = getSharedPreferences("meal_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mealList);
        editor.putString("meal_list", json);
        editor.apply();

    }

    // 이미지 URI로부터 실제 파일 경로 가져오기
    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return contentUri.getPath(); // 갤러리에서 직접 선택한 경우
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index);
            cursor.close();
            return path;
        }
    }
    // 이미지를 내부 저장소로 복사하는 메소드
    private String copyImageToInternalStorage(String sourceImagePath) {
        try {
            // 이미지 파일의 이름을 추출
            String fileName = sourceImagePath.substring(sourceImagePath.lastIndexOf("/") + 1);

            // 내부 저장소의 files 디렉터리에 새로운 파일 생성
            File destinationFile = new File(getFilesDir(), fileName);

            // 파일 복사
            InputStream inputStream = new FileInputStream(sourceImagePath);
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

            // 내부 저장소에 저장된 이미지의 경로 반환
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer RandomCalories() {
        // Random 객체 생성
        Random random = new Random();

        // 200에서 500까지의 랜덤한 칼로리 값 생성
        int randomCalories = random.nextInt(301) + 200;

        // 결과 출력
        return randomCalories;
    }
}