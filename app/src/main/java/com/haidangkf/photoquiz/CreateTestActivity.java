package com.haidangkf.photoquiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    private TextView tvSelectCategory;
    private EditText etNumOfQuestion;
    private Button btnCreate;
    private Button btnCancel;
    private boolean isExpandCategory = true;
    private int chkCount;

    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> myQuestions = new ArrayList<>();

    private ArrayList<Category> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        getControls();
        addCategoryData();

        mAdapter = new CategoryAdapter(categoryList);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // display the divider between rows
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
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

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberOfQuestion = etNumOfQuestion.getText().toString();
                if (numberOfQuestion.isEmpty()) {
                    Toast.makeText(CreateTestActivity.this, getString(R.string.msg_enter_num_ques), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(numberOfQuestion) < 1) {
                    Toast.makeText(CreateTestActivity.this, getString(R.string.msg_num_ques_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }

                chkCount = 0;
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int x = 0; x < categoryList.size(); x++) {
                    if (categoryList.get(x).isSelected()) {
                        chkCount++;
                        selectedItems.add(categoryList.get(x).getCategory());
                    }
                }
                if (chkCount < 1) {
                    Toast.makeText(CreateTestActivity.this, getString(R.string.msg_select_at_least), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "getChildCount = " + recyclerView.getChildCount()); // count visible items on screen
                Log.i(TAG, "getItemCount = " + mAdapter.getItemCount()); // count all items in Adapter
                Log.i(TAG, "chkCount = " + chkCount);

                //-----------------------------------------------
                allQuestions = MyApplication.db.getQuestionList();
                for (String category : selectedItems) {
                    for (Question question : allQuestions) {
                        if (question.getCategory().equalsIgnoreCase(category)) {
                            myQuestions.add(question);
                        }
                    }
                }

                Log.i(TAG, myQuestions.size() + " questions matches.");
                if (Integer.parseInt(numberOfQuestion) > myQuestions.size()) {
                    String msg = getString(R.string.msg_not_enough_ques) + "\n";
                    msg += getString(R.string.msg_max_of_ques_is) + " " + myQuestions.size() + "\n";
                    msg += getString(R.string.msg_edit_to_continue);
                    Toast.makeText(CreateTestActivity.this, msg, Toast.LENGTH_LONG).show();
                    etNumOfQuestion.requestFocus();
                    etNumOfQuestion.selectAll();
                    return;
                }
                //-----------------------------------------------

                Intent i = new Intent();
                i.putExtra("numberOfQuestion", Integer.parseInt(numberOfQuestion));
                i.putStringArrayListExtra("categoryList", selectedItems);
                i.setClass(CreateTestActivity.this, DoTestActivity.class);
                startActivity(i);
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
        etNumOfQuestion = (EditText) findViewById(R.id.etNumOfQuestion);
        btnCreate = (Button) findViewById(R.id.btnCreate);
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