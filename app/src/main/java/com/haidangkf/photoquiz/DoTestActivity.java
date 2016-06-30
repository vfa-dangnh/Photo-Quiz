package com.haidangkf.photoquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import github.chenupt.springindicator.SpringIndicator;

public class DoTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;
    CirclePageIndicator mIndicator;
    SpringIndicator springIndicator;

    // -----------------------------------------------
    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> matchQuestions = new ArrayList<>();
    ArrayList<Question> myTestQuestions;
    int numberOfQuestion;
    public static HashMap<Integer, Integer> answerMap;
    // -----------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_test);

        selectedItems = getIntent().getStringArrayListExtra("categoryList");
        numberOfQuestion = getIntent().getIntExtra("numberOfQuestion", 0);
        Log.d(TAG, "selectedItems = " + selectedItems.toString());
        Log.d(TAG, "numberOfQuestion = " + numberOfQuestion);

        allQuestions = MyApplication.db.getQuestionList();
        for (String category : selectedItems) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    matchQuestions.add(question);
                }
            }
        }

        myTestQuestions = randomMyTestQuestions(matchQuestions, numberOfQuestion);
        answerMap = new HashMap<>();
        Log.d(TAG, "answerMap.size() = " + answerMap.size());
        for (int i = 0; i < numberOfQuestion; i++) {
            answerMap.put(i, -1); // mặc định -1 tức là chưa trả lời, đúng là 1, sai là 0
            Log.d(TAG, "answerMap.size() = " + answerMap.size());
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(this, myTestQuestions);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setCurrentItem(0); // set the item to view first
        viewPager.addOnPageChangeListener(pageChangeListener);

        // ViewPager JakeWharton Indicator
//        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
//        mIndicator.setStrokeColor(Color.RED); // màu viền cho ký hiệu
//        mIndicator.setFillColor(Color.BLUE); // màu nền cho ký hiệu đang chọn
//        mIndicator.setPageColor(Color.GREEN); // màu nền cho ký hiệu không được chọn
//        mIndicator.setBackgroundColor(Color.YELLOW); // màu nền Indicator Bar
//        mIndicator.setViewPager(viewPager);

        // ViewPager SpringIndicator
        springIndicator = (SpringIndicator) findViewById(R.id.indicator);
        springIndicator.setViewPager(viewPager);

    }

    public ArrayList<Question> randomMyTestQuestions(ArrayList<Question> questionList,
                                                     int quantity) {
        ArrayList<Question> returnQuestions = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Random rand = new Random();
            int n = rand.nextInt(questionList.size());
            returnQuestions.add(questionList.get(n));
            Log.d(TAG, "Question " + (i + 1) + ": " + questionList.get(n).toString());
            questionList.remove(n); // delete this question in list after getting it
        }

        return returnQuestions;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume DoTest");
    }


    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        boolean isShowDialog = false;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //This method will be invoked when the current page is scrolled,
            //either as part of a programmatically initiated smooth scroll
            //or a user initiated touch scroll.
//            Log.d(TAG, "onPageScrolled " + position);
        }

        @Override
        public void onPageSelected(int position) {
            //This method will be invoked when a new page becomes selected.
//            Log.d(TAG, "ON PAGE SELECTED " + position);

            /*int lastIndex = viewPager.getAdapter().getCount() - 1;
            if (position == lastIndex) { // when on last page
                Toast.makeText(DoTestActivity.this, getString(R.string.msg_reach_last_page), Toast.LENGTH_SHORT).show();
                // can start new Activity here
            }*/
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Called when the scroll state changes.
//            Log.d(TAG, "onPageScrollStateChanged " + state);

            int lastIndex = viewPager.getAdapter().getCount() - 1;
            int penultIndex = viewPager.getAdapter().getCount() - 2; // index áp chót
            int currentItem = viewPager.getCurrentItem();

            if (currentItem == penultIndex) {
                isShowDialog = false;
            }

            if (currentItem == lastIndex && state == 0) { // handle last page change
                if(lastIndex==0){ // only 1 page
                    handleExitDoTest();
                } else { // 2 pages or more
                    if (!isShowDialog) {
                        // on first swipe to last page, just set the flag to true
                        // don't show dialog at this time
                        isShowDialog = true;
                    } else {
                        // user is on last page and try to swipe next
                        handleExitDoTest();
                    }
                }
            }
        }
    };

    public void handleExitDoTest() {
        for (int i = 0; i < numberOfQuestion; i++) {
            if (answerMap.get(i) == -1) { // -1 là chưa trả lời
                showNotFinishedDialog(i);
                return;
            }
        }

        showFinishedDialog();
    }

    public void showFinishedDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.msg_finish_test));
        b.setMessage(getString(R.string.msg_have_answer_all));

        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(DoTestActivity.this, ResultActivity.class);
                i.putExtra("numberOfQuestion", myTestQuestions.size());
                i.putExtra("answerMap", answerMap);
                startActivity(i);
                finish();
            }
        });

        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(DoTestActivity.this, getString(R.string.msg_feel_free_to_edit), Toast.LENGTH_SHORT).show();
            }
        });

        b.create().show();
    }

    public void showNotFinishedDialog(final int index) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.msg_not_finish_test));
        b.setMessage(getString(R.string.msg_have_not_answer_all));

        b.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(DoTestActivity.this, getString(R.string.msg_go_back_to_question) + (index + 1), Toast.LENGTH_SHORT).show();
            }
        });

        b.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        b.create().show();
    }

}