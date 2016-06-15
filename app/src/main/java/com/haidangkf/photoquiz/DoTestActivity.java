package com.haidangkf.photoquiz;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DoTestActivity extends AppCompatActivity {

    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;
    // -----------------------------------------------
    final String TAG = "my_log";
    public static ArrayList<String> selectedItems = new ArrayList<>();
    public static int numberOfQuestion;
    public static int _position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_test);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter();
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(pageChangeListener);

        selectedItems = getIntent().getStringArrayListExtra("categoryList");
        numberOfQuestion = getIntent().getIntExtra("numberOfQuestion", 0);

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
            _position = position;
            Toast.makeText(DoTestActivity.this, "Page " + position, Toast.LENGTH_SHORT).show();
        }
    };

}