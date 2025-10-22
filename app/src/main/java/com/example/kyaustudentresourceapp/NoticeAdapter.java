package com.example.kyaustudentresourceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private Context context;
    private List<Notice> noticeList;

    public NoticeAdapter(Context context, List<Notice> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.tvNoticeTitle.setText(notice.getTitle());
        holder.tvNoticeBody.setText(notice.getBody());
        holder.tvNoticeDate.setText(notice.getDate());
        holder.tvNoticeCategory.setText(notice.getCategory());
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoticeTitle, tvNoticeBody, tvNoticeDate, tvNoticeCategory;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoticeTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvNoticeBody = itemView.findViewById(R.id.tvNoticeBody);
            tvNoticeDate = itemView.findViewById(R.id.tvNoticeDate);
            tvNoticeCategory = itemView.findViewById(R.id.tvNoticeCategory);
        }
    }
}
