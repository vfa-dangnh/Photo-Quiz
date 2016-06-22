package com.haidangkf.photoquiz;

import android.content.Intent;
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

    private List<String> categoryListString = new ArrayList<>();
    private List<String> questionNameList = new ArrayList<>();
    MultiSelectionSpinner multiSelectionSpinner;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<QuestionNameViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);

        findViewById();
        // set font for TextView tvSelectCategory
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/victoria.ttf");
        tvSelectCategory.setTypeface(face);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        categoryListString = getCategoryDataFromDB();
        if (categoryListString.size() < 1) { // in case Database is empty
            multiSelectionSpinner.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            tvSelectCategory.setText(getString(R.string.msg_no_question_found));
            return;
        }

        questionNameList = getQuestionNameList(categoryListString);
        multiSelectionSpinner.setItems(categoryListString);
//        multiSelectionSpinner.setSelection(new int[]{0});
        multiSelectionSpinner.setSelection(categoryListString);
        multiSelectionSpinner.setListener(this);

        mAdapter = new RecyclerView.Adapter<QuestionNameViewHolder>() {
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
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // display the divider between rows
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) { // after selecting category from spinner
        Toast.makeText(this, strings.toString(), Toast.LENGTH_SHORT).show();

        categoryListString.clear();
        for (String s : strings) {
            categoryListString.add(s);
        }
        questionNameList = getQuestionNameList(categoryListString);
        mAdapter.notifyDataSetChanged(); // update the list
    }


    private List<String> getCategoryDataFromDB() {
        ArrayList<Question> list = MyApplication.db.getQuestionList();
        List<String> categoryListString = new ArrayList<>();

        for (Question question : list) {
            String category = question.getCategory();
            boolean isExisted = false;
            for (String c : categoryListString) {
                if (c.equalsIgnoreCase(category)) {
                    isExisted = true;
                    break;
                }
            }
            if (!isExisted) {
                categoryListString.add(category);
            }
        }

        return categoryListString;
    }

    private List<String> getQuestionNameList(List<String> categoryListString) {
        List<String> nameList = new ArrayList<>();
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

    private Question findQuestionFromName(String name) {
        ArrayList<Question> allQuestions = MyApplication.db.getQuestionList();
        for (Question question : allQuestions) {
            if (question.getComment().equals(name)) {
                return question;
            }
        }
        return null;
    }

    private void findViewById() {
        tvSelectCategory = (TextView) findViewById(R.id.tvSelectCategory);
        multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.multiSpinner);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btnBack = (Button) findViewById(R.id.btnBack);
    }

    //---------------------------------------------------------------------------
    // inner class ViewHolder
    private class QuestionNameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public QuestionNameViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String questionName = ((TextView) v).getText().toString();
            Toast.makeText(getApplicationContext(), getString(R.string.msg_go_details) + questionName, Toast.LENGTH_LONG).show();
            Question question = findQuestionFromName(questionName);
            Intent i = new Intent(ViewQuestionActivity.this, QuestionDetailActivity.class);
            i.putExtra("questionObject", question);
            startActivity(i);
        }
    }

}