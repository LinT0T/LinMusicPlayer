package com.lint0t.linmusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lint0t.linmusicplayer.bean.Bean;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<Bean> datas = new ArrayList<>();
    private Context context;
    onItemClickListener onItemClickListener;

    public void setOnItemClickListener(MyAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface onItemClickListener {
        public void onItemClick(View view, int position);
    }

    public MyAdapter(List<Bean> datas, Context context) {
        this.datas = datas;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Bean bean = datas.get(position);
        holder.tv_name.setText(bean.getName());
        holder.tv_singer.setText(bean.getSinger());
        holder.tv_time.setText(bean.getTime());
        holder.tv_id.setText(bean.getId());
        holder.img_cover.setImageBitmap(bean.getBitmap());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name, tv_singer, tv_time, tv_id;
        private ImageView img_cover;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_singer = itemView.findViewById(R.id.tv_singer);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_id = itemView.findViewById(R.id.tv_id);
            img_cover = itemView.findViewById(R.id.img_cover);
        }
    }
}
