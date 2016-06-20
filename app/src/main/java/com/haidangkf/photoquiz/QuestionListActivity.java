package com.haidangkf.photoquiz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class QuestionListActivity extends AppCompatActivity {

    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> matchQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        selectedItems = getIntent().getStringArrayListExtra("categoryList");
        allQuestions = MyApplication.db.getQuestionList();
        for (String category : selectedItems) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    matchQuestions.add(question);
                }
            }
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<Question> adapter = new ArrayAdapter<Question>
                (this, android.R.layout.simple_list_item_1, matchQuestions);
        listView.setAdapter(adapter);

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
