package com.haidangkf.photoquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    private TextView tvSelectCategory;
    private TextView tvSelectAll;
    private TableRow tableRowSelectAll;
    public static CheckBox chkSelectAll;
    public static boolean isSelectAll = false;
    private EditText etNumOfQuestion;
    private Button btnCreate;
    private Button btnCancel;
    private boolean isExpandCategory = true;
    private int chkCount;

    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> matchQuestions = new ArrayList<>();

    private ArrayList<Category> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        findViewById();
        // set font for TextView tvSelectCategory
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/victoria.ttf");
        tvSelectCategory.setTypeface(face);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        categoryList = getCategoryDataFromDB();
        if (categoryList.size() < 1) { // in case Database is empty
            recyclerView.setVisibility(View.INVISIBLE);
            tableRowSelectAll.setVisibility(View.INVISIBLE);
            etNumOfQuestion.setVisibility(View.INVISIBLE);
            btnCreate.setVisibility(View.GONE);
            tvSelectCategory.setText(getString(R.string.msg_no_question_found));
            Toast.makeText(CreateTestActivity.this, getString(R.string.msg_no_question_found2), Toast.LENGTH_LONG).show();
            return;
        }

        mAdapter = new CategoryAdapter(this, categoryList);
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
                if (categoryList.get(position).isSelected()) {
                    categoryList.get(position).setSelected(false);
                    mAdapter.notifyDataSetChanged();
                } else {
                    categoryList.get(position).setSelected(true);
                    mAdapter.notifyDataSetChanged();
                }

                setCheckForSelectAllBox();

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
                    isSelectAll = false;
                    actionSelectAll(isSelectAll);
                } else {
                    chkSelectAll.setChecked(true);
                    isSelectAll = true;
                    actionSelectAll(isSelectAll);
                }
            }
        });

        chkSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkSelectAll.isChecked()) {
                    isSelectAll = true;
                    actionSelectAll(isSelectAll);
                } else {
                    isSelectAll = false;
                    actionSelectAll(isSelectAll);
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

                Log.d(TAG, "getChildCount = " + recyclerView.getChildCount()); // count visible items on screen
                Log.d(TAG, "getItemCount = " + mAdapter.getItemCount()); // count all items in Adapter
                Log.d(TAG, "chkCount = " + chkCount);

                //-----------------------------------------------
                allQuestions = MyApplication.db.getQuestionList();
                for (String category : selectedItems) {
                    for (Question question : allQuestions) {
                        if (question.getCategory().equalsIgnoreCase(category)) {
                            matchQuestions.add(question);
                        }
                    }
                }

                Log.d(TAG, matchQuestions.size() + " questions match.");
                if (Integer.parseInt(numberOfQuestion) > matchQuestions.size()) {
                    String msg = getString(R.string.msg_not_enough_ques) + "\n";
                    msg += getString(R.string.msg_max_of_ques_is) + " " + matchQuestions.size() + "\n";
                    msg += getString(R.string.msg_edit_to_continue);
                    Toast.makeText(CreateTestActivity.this, msg, Toast.LENGTH_LONG).show();
                    matchQuestions.removeAll(allQuestions); // delete all added questions in list
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

    }

    private void actionSelectAll(boolean isSelectAll) {
        for (Category category : categoryList) {
            category.setSelected(isSelectAll);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setCheckForSelectAllBox() {
        int count = 0;
        for (Category category : categoryList) {
            if (category.isSelected()) count++;
        }

        if (count == recyclerView.getAdapter().getItemCount()) {
            CreateTestActivity.chkSelectAll.setChecked(true);
            CreateTestActivity.isSelectAll = true;
        } else {
            CreateTestActivity.chkSelectAll.setChecked(false);
            CreateTestActivity.isSelectAll = false;
        }
    }

    private ArrayList<Category> getCategoryDataFromDB() {
        ArrayList<Question> list = MyApplication.db.getQuestionList();
        ArrayList<Category> categoryList = new ArrayList<>();

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

        return categoryList;
    }

    private void findViewById() {
        tvSelectCategory = (TextView) findViewById(R.id.tvSelectCategory);
        tvSelectAll = (TextView) findViewById(R.id.tvSelectAll);
        tableRowSelectAll = (TableRow) findViewById(R.id.tableRowSelectAll);
        chkSelectAll = (CheckBox) findViewById(R.id.chkSelectAll);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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