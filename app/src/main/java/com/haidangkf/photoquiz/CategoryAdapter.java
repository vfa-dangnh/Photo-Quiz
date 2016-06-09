package com.haidangkf.photoquiz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private List<String> categoryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategory;
        public CheckBox chkCategory;

        public MyViewHolder(View view) {
            super(view);
            tvCategory = (TextView) view.findViewById(R.id.tvCategory);
            chkCategory = (CheckBox) view.findViewById(R.id.chkCategory);
        }
    }


    public CategoryAdapter(List<String> categoryList) {
        this.categoryList = categoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_category_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.tvCategory.setText(category);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}