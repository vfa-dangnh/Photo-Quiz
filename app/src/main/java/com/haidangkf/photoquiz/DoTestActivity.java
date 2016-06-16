package com.haidangkf.photoquiz;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class DoTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;

    // -----------------------------------------------
    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> matchQuestions = new ArrayList<>();
    ArrayList<Question> myTestQuestions;
    int numberOfQuestion;
    // -----------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_test);

        selectedItems = getIntent().getStringArrayListExtra("categoryList");
        numberOfQuestion = getIntent().getIntExtra("numberOfQuestion", 0);
        Log.i(TAG, "selectedItems = " + selectedItems.toString());
        Log.i(TAG, "numberOfQuestion = " + numberOfQuestion);

        allQuestions = MyApplication.db.getQuestionList();
        for (String category : selectedItems) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    matchQuestions.add(question);
                }
            }
        }

        myTestQuestions = randomMyTestQuestions(matchQuestions,numberOfQuestion);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(this,myTestQuestions);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setCurrentItem(0); // set the item to view first
        viewPager.setOnPageChangeListener(pageChangeListener);
//        viewPager.refreshDrawableState(); // don't know what it is

    }

    public ArrayList<Question> randomMyTestQuestions(ArrayList<Question> questionList,
                                                     int quantity){
        ArrayList<Question> returnQuestions = new ArrayList<>();
        for (int i=0; i<quantity; i++){
            Random rand = new Random();
            int n = rand.nextInt(questionList.size());
            returnQuestions.add(questionList.get(n));
            questionList.remove(n); // delete this question in list after getting it
        }

        return returnQuestions;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume DoTest");
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int state) {
            //Called when the scroll state changes.
        }

        @Override
        public void onPageScrolled(int position,
                                   float positionOffset, int positionOffsetPixels) {
            //This method will be invoked when the current page is scrolled,
            //either as part of a programmatically initiated smooth scroll
            //or a user initiated touch scroll.
        }

        @Override
        public void onPageSelected(int position) {
            //This method will be invoked when a new page becomes selected.
            Toast.makeText(DoTestActivity.this, "Question " + (position+1), Toast.LENGTH_SHORT).show();
            if (position==viewPager.getAdapter().getCount()-1){
//start next Activity
            }
            Toast.makeText(DoTestActivity.this, "", Toast.LENGTH_SHORT).show();
        }
    };

}