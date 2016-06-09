package com.haidangkf.photoquiz;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CreateTestActivity extends AppCompatActivity {

    private List<String> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;
    private TextView tvCategory;
    private CheckBox chkCategory;
    private boolean isExpandCategory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.VISIBLE);

        mAdapter = new CategoryAdapter(categoryList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // display the divider
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String category = categoryList.get(position);
//                Toast.makeText(getApplicationContext(), category + " is selected", Toast.LENGTH_SHORT).show();

                chkCategory = (CheckBox) recyclerView.getChildAt(position).findViewById(R.id.chkCategory);
                if (chkCategory.isChecked()) {
                    chkCategory.setChecked(false);
                } else {
                    chkCategory.setChecked(true);
                }
                /*for (int x = 0; x < recyclerView.getChildCount(); x++) {
                    chkCategory = (CheckBox) recyclerView.getChildAt(x).findViewById(R.id.chkCategory);
                    if (chkCategory.isChecked()) {
                        // do something
                    }
                }*/
            }

            @Override
            public void onLongClick(View view, int position) {
                String category = categoryList.get(position);
//                Toast.makeText(getApplicationContext(), "Long click on " + category, Toast.LENGTH_SHORT).show();
            }
        }));

        addSampleCategoryData();

        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExpandCategory) {
                    recyclerView.setVisibility(View.VISIBLE);
                    isExpandCategory = true;
                } else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    isExpandCategory = false;
                }
            }
        });

    }

    private void addSampleCategoryData() {
        categoryList.add("Object");
        categoryList.add("Scenery");
        categoryList.add("Human");
        categoryList.add("Actor");
        categoryList.add("Singer");
        categoryList.add("Device");
        mAdapter.notifyDataSetChanged();
    }

    //--------------------------------------------------------------------------------

    // RecyclerView doesn’t have OnItemClickListener method to identify item click
    // Need to write your own class extending RecyclerView.OnItemTouchListener
    // The below code is using for RecyclerView Item Click Listener

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private CreateTestActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final CreateTestActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}