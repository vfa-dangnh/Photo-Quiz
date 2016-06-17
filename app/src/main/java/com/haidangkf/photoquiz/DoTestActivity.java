package com.haidangkf.photoquiz;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Random;

public class DoTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;
    CirclePageIndicator mIndicator;

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

        myTestQuestions = randomMyTestQuestions(matchQuestions, numberOfQuestion);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(this, myTestQuestions);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setCurrentItem(0); // set the item to view first
        viewPager.setOnPageChangeListener(pageChangeListener);

        // ViewPager Indicator
//        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
//        mIndicator.setStrokeColor(Color.RED); // màu viền cho ký hiệu
//        mIndicator.setFillColor(Color.BLUE); // màu nền cho ký hiệu đang chọn
//        mIndicator.setPageColor(Color.GREEN); // màu nền cho ký hiệu không được chọn
//        mIndicator.setBackgroundColor(Color.YELLOW); // màu nền Indicator Bar
//        mIndicator.setViewPager(viewPager);

    }

    public ArrayList<Question> randomMyTestQuestions(ArrayList<Question> questionList,
                                                     int quantity) {
        ArrayList<Question> returnQuestions = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Random rand = new Random();
            int n = rand.nextInt(questionList.size());
            returnQuestions.add(questionList.get(n));
            questionList.remove(n); // delete this question in list after getting it
        }
        for (Question question : returnQuestions){
            Log.i(TAG, "question: "+question.toString());
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
        public void onPageSelected(final int position) {
            //This method will be invoked when a new page becomes selected.
            if (position == viewPager.getAdapter().getCount() - 1) {
                // start next Activity
                Toast.makeText(DoTestActivity.this, getString(R.string.msg_finish_test), Toast.LENGTH_SHORT).show();
            }
        }
    };

}