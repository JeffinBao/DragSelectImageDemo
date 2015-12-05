package com.jeffinbao.dragselectimagedemo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jeffinbao.dragselectimagedemo.wiget.CheckableImageView;

import java.util.HashSet;
import java.util.List;

/**
 * Author: jeffinbao
 * Date: 2015-11-23
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<Integer> digitList;
    private List<Integer> colorList;
    private Context context;
    private HashSet<Integer> multiSelection;
    private OnItemClickListener listener;

    public MainAdapter(List<Integer> digitList, List<Integer> colorList, Context context) {
        this.digitList = digitList;
        this.colorList = colorList;
        this.context = context;

        multiSelection = new HashSet<Integer>();
    }

    public boolean getItemSelectedState(int position) {
        return multiSelection.contains(position);
    }

    public int getSelectedItemCount() {
        return multiSelection.size();
    }

    public void toggleItemSelect(int position) {
        if (multiSelection.contains(position)) {
            multiSelection.remove(position);
        } else {
            multiSelection.add(position);
        }
    }

    public HashSet<Integer> getMultiSelection() {
        return multiSelection;
    }

    public void clearSelectedItem() {
        multiSelection.clear();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return digitList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.digitTextView.setText(String.valueOf(digitList.get(position)));
        holder.digitTextView.setBackgroundColor(ContextCompat.getColor(context, colorList.get(position % 7)));

        playHolderAnimatorSet(holder, position);
    }

    private void playHolderAnimatorSet(ViewHolder holder, int position) {
        boolean selected = getItemSelectedState(position);
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        holder.checkableImageView.setChecked(selected);
        if (selected) {
            scaleX = ObjectAnimator.ofFloat(holder.digitTextView, "scaleX", 1.0f, 0.8F);
            scaleY = ObjectAnimator.ofFloat(holder.digitTextView, "scaleY", 1.0f, 0.8F);
        } else {
            scaleX = ObjectAnimator.ofFloat(holder.digitTextView, "scaleX", 0.8f, 1.0F);
            scaleY = ObjectAnimator.ofFloat(holder.digitTextView, "scaleY", 0.8f, 1.0F);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView digitTextView;
        private MainAdapter mainAdapter;
        public CheckableImageView checkableImageView;

        public ViewHolder(View view, MainAdapter adapter) {
            super(view);
            mainAdapter = adapter;
            digitTextView = (TextView) view.findViewById(R.id.digit_text_view);
            checkableImageView = (CheckableImageView) view.findViewById(R.id.recycler_checkable_image_view);

            checkableImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != mainAdapter.listener) {
                mainAdapter.listener.onClickListener(v, getLayoutPosition());
            }

        }
    }

    public interface OnItemClickListener {
        void onClickListener(View view, int position);
    }
}
