package com.haidangkf.photoquiz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Category> categoryList;

    public CategoryAdapter(Context context, ArrayList<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_category_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Category category = categoryList.get(position);
        holder.tvCategory.setText(category.getCategory());

        // in some cases, it will prevent unwanted situations
        holder.chkCategory.setOnCheckedChangeListener(null);

        // if true, your checkbox will be selected, else unselected
        holder.chkCategory.setChecked(category.isSelected());

        holder.chkCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // set your category's last status
                category.setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    // ---------------------------------------------------------
    // inner class ViewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategory;
        public CheckBox chkCategory;

        public MyViewHolder(View view) {
            super(view);
            tvCategory = (TextView) view.findViewById(R.id.tvCategory);
            chkCategory = (CheckBox) view.findViewById(R.id.chkCategory);
        }
    }

}