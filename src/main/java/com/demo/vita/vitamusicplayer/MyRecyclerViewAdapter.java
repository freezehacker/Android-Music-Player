package com.demo.vita.vitamusicplayer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sjk on 2016/3/19.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    RvOnItemClickListener mListener;
    List<MusicBean> musics;
    Context mContext;

    private int lastSelectItem = -1;
    private int selectItem = -1;    // 作用：用来标记需要高亮的item

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    // constructor
    public MyRecyclerViewAdapter(Context context, List<MusicBean> _musics) {
        mContext = context;
        musics = _musics;
    }

    // setOnListener
    public void setOnItemClickListener(RvOnItemClickListener listener) {
        mListener = listener;
    }

    // 列表试图中的添加
    public void addMusic(int pos, MusicBean music) {
        musics.add(pos, music);
        notifyItemInserted(pos);
    }

    // 列表视图中的删除
    public void removeMusic(int pos) {
        musics.remove(pos);
        notifyItemRemoved(pos);
    }

    public void highlightItem(int position) {
        setSelectItem(position);
        //notifyItemChanged(position);
        notifyDataSetChanged(); // high cost?
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_item, parent, false);
        MyViewHolder ret = new MyViewHolder(view);
        ret.tv_title = (TextView) view.findViewById(R.id.rv_item_title);
        ret.tv_artist=(TextView)view.findViewById(R.id.rv_item_artist);
        return ret;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        MusicBean item = musics.get(position);
        holder.tv_title.setText(item.getTitle());
        holder.tv_artist.setText(item.getArtist());

        if (selectItem == position) {   // 高亮：字体变红粗
            holder.tv_title.setTextColor(Color.BLUE);
            holder.tv_title.getPaint().setFakeBoldText(true);
            holder.tv_artist.setVisibility(View.VISIBLE);
        } else {                        // 非高亮，不定义可能会出错
            holder.tv_title.setTextColor(Color.BLACK);
            holder.tv_title.getPaint().setFakeBoldText(false);
            holder.tv_artist.setVisibility(View.GONE);
        }

        // 设置监听
        if (mListener != null) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // position和holder.getLayoutPosition()的区别在哪？？？？
                    //MyLogger.log("getLayoutPos=" + holder.getLayoutPosition() + ", pos=" + position);
                    //mListener.onItemClick(holder.getLayoutPosition());
                    mListener.onItemClick(position);
                }
            };
            holder.tv_title.setOnClickListener(clickListener);
            holder.tv_artist.setOnClickListener(clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title, tv_artist;

        public MyViewHolder(View v) {
            super(v);
        }
    }
}
