package com.haidangkf.photoquiz;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ViewQuestionActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener {

    final String TAG = "my_log";
    private TextView tvSelectCategory;
    private Button btnBack;

    private ArrayList<Category> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);

        findViewById();
        addCategoryData();
        // set font for TextView tvSelectCategory
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/victoria.ttf");
        tvSelectCategory.setTypeface(face);


        String[] array = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
        MultiSelectionSpinner multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.multiSpinner);
        multiSelectionSpinner.setItems(array);
//        multiSelectionSpinner.setSelection(new int[]{2, 6});
        multiSelectionSpinner.setListener(this);


        mAdapter = new CategoryAdapter(categoryList);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // display the divider between rows
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        Toast.makeText(this, strings.toString(), Toast.LENGTH_SHORT).show();
    }

    private void addCategoryData() {
        ArrayList<Question> list = MyApplication.db.getQuestionList();

        for (Question question : list) {
            Category category = new Category(question.getCategory());
            boolean isExisted = false;
            for (Category c : categoryList) {
                if (c.getCategory().equalsIgnoreCase(category.getCategory())) {
                    isExisted = true;
                    break;
                }
            }
            if (!isExisted) {
                categoryList.add(category);
            }
        }
    }

    private void findViewById() {
        tvSelectCategory = (TextView) findViewById(R.id.tvSelectCategory);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btnBack = (Button) findViewById(R.id.btnBack);
    }

}