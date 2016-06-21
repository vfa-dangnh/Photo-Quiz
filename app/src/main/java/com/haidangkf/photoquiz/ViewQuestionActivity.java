package com.haidangkf.photoquiz;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ArrayList<Category> categoryList2 = new ArrayList<>();
    private List<String> categoryListString = new ArrayList<>();
    private ArrayList<String> questionNameList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);

        findViewById();
        addCategoryData();
        addCategoryDataString();
        // set font for TextView tvSelectCategory
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/victoria.ttf");
        tvSelectCategory.setTypeface(face);


        String[] array = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
        MultiSelectionSpinner multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.multiSpinner);
        multiSelectionSpinner.setItems(categoryListString);
//        multiSelectionSpinner.setSelection(new int[]{2, 3});
        multiSelectionSpinner.setListener(this);


//        mAdapter = new CategoryAdapter(categoryList2);
//        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        // display the divider between rows
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        // set the adapter
//        recyclerView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();


        questionNameList = getQuestionNameList(categoryListString);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter<QuestionNameViewHolder>() {

            @Override
            public QuestionNameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false);
                QuestionNameViewHolder vh = new QuestionNameViewHolder(v);
                return vh;
            }

            @Override
            public void onBindViewHolder(QuestionNameViewHolder vh, int position) {
                TextView tv = (TextView) vh.itemView;
                tv.setText(questionNameList.get(position));
                tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stars_black, 0, 0, 0);
            }

            @Override
            public int getItemCount() {
                return questionNameList.size();
            }
        });


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

    private void addCategoryDataString() {
        for (Category c : categoryList) {
            categoryListString.add(c.getCategory());
        }
    }

    private ArrayList<String> getQuestionNameList(List<String> categoryListString) {
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Question> allQuestions = MyApplication.db.getQuestionList();
        ArrayList<Question> matchQuestions = new ArrayList<>();

        for (String category : categoryListString) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    matchQuestions.add(question);
                }
            }
        }

        for (Question question : matchQuestions) {
            nameList.add(question.getComment());
        }

        return nameList;
    }

    private void findViewById() {
        tvSelectCategory = (TextView) findViewById(R.id.tvSelectCategory);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btnBack = (Button) findViewById(R.id.btnBack);
    }

    //--------------------------------------------------------------------------

    private class QuestionNameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public QuestionNameViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "You have clicked " + ((TextView) v).getText(), Toast.LENGTH_LONG).show();
        }
    }

}