package com.haidangkf.photoquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewQuestionActivity extends AppCompatActivity {

    final String TAG = "my_log";
    private TextView tvSelectCategory;
    private TextView tvSelectAll;
    private CheckBox chkSelectAll;
    private Button btnView;
    private Button btnCancel;
    private boolean isExpandCategory = true;
    private int chkCount;

    private ArrayList<Category> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_question);

        getControls();
        addCategoryData();
        // set font for TextView tvSelectCategory
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/victoria.ttf");
        tvSelectCategory.setTypeface(face);

        mAdapter = new CategoryAdapter(categoryList);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // display the divider between rows
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new CreateTestActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
//                View childView = mLayoutManager.findViewByPosition(position);
//                tvCategory = (TextView) childView.findViewById(R.id.tvCategory);
//                Toast.makeText(CreateTestActivity.this, "position " + position + " - " + tvCategory.getText().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
//                Category category = categoryList.get(position);
//                Toast.makeText(getApplicationContext(), "Long click on " + category.getCategory(), Toast.LENGTH_SHORT).show();
            }
        }));

        tvSelectCategory.setOnClickListener(new View.OnClickListener() {
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

        tvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkSelectAll.isChecked()) {
                    chkSelectAll.setChecked(false);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    chkSelectAll.setChecked(true);
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });

        chkSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkSelectAll.isChecked()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkSelectAll.isChecked()) {
                    ArrayList<String> selectedItems = new ArrayList<String>();
                    for (int x = 0; x < categoryList.size(); x++) {
                        selectedItems.add(categoryList.get(x).getCategory());
                    }

                    Intent i = new Intent();
                    i.putStringArrayListExtra("categoryList", selectedItems);
                    i.setClass(ViewQuestionActivity.this, QuestionListActivity.class);
                    startActivity(i);
                } else {

                    chkCount = 0;
                    ArrayList<String> selectedItems = new ArrayList<String>();
                    for (int x = 0; x < categoryList.size(); x++) {
                        if (categoryList.get(x).isSelected()) {
                            chkCount++;
                            selectedItems.add(categoryList.get(x).getCategory());
                        }
                    }

                    if (chkCount < 1) {
                        Toast.makeText(ViewQuestionActivity.this, getString(R.string.msg_select_at_least), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent i = new Intent();
                    i.putStringArrayListExtra("categoryList", selectedItems);
                    i.setClass(ViewQuestionActivity.this, QuestionListActivity.class);
                    startActivity(i);
                }

                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    private void getControls() {
        tvSelectCategory = (TextView) findViewById(R.id.tvSelectCategory);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        tvSelectAll = (TextView) findViewById(R.id.tvSelectAll);
        chkSelectAll = (CheckBox) findViewById(R.id.chkSelectAll);
        btnView = (Button) findViewById(R.id.btnView);
        btnCancel = (Button) findViewById(R.id.btnCancel);
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
