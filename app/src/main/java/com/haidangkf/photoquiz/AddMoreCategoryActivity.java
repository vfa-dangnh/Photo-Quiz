package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddMoreCategoryActivity extends Activity {

    EditText etCategory;
    Button btnAdd, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_more_category);

        etCategory = (EditText) findViewById(R.id.etCategory);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        // Don't finish it when the background activity is clicked
        setFinishOnTouchOutside(false);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etCategory.getText().toString().isEmpty()){
                    Toast.makeText(AddMoreCategoryActivity.this, getString(R.string.msg_enter_category), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("newCategory", etCategory.getText().toString().trim());
                intent.putExtra("DATA_CATEGORY", bundle); // Đưa dữ liệu bundle vào intent
                setResult(2, intent); // Gửi dữ liệu về activity chờ bên dưới có mã nhận 2
                finish(); // Kết thúc Activity hiện tại
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
