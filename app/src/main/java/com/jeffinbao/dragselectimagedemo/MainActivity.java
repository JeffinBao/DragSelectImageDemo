package com.jeffinbao.dragselectimagedemo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.widget.AutoScrollHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Author: jeffinbao
 * Date: 2015-11-23
 */
public class MainActivity extends AppCompatActivity implements MainAdapter.OnItemClickListener, RecyclerView.OnItemTouchListener {
    private Integer[] digitArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50};
    private Integer[] colorArray = {R.color.red_u1, R.color.orange_u1, R.color.yellow_u1,
            R.color.green_u1, R.color.cyan_u1, R.color.blue_u1,R.color.purple_u1};
    private static final int SPAN_COUNT = 4;
    private static final int FAST_SELECT_ACTIVE_X_DISTANCE = 50;
    private static final int FAST_SELECT_ACTIVE_Y_DISTANCE = 20;
    
    private RecyclerView recyclerView;
    private MainAdapter adapter;
    private MenuItem menuItemCount;
    private GestureDetector gestureDetector;

    /**
     * active drag select image mode when dragSelectEnable is true
     * */
    private boolean dragSelectEnable = false;

    private int initialPosition;
    private int lastPosition;

    /**
     * execute performGalleryItemCheck method exactly for initial position
     * if selectInitialPositionFlag is true
     * */
    private boolean selectInitialPositionFlag = false;

    /**
     * record drag position trend
     * */
    private int positionTrend;

    private boolean positionFlag = false;

    /**
     * identify the purpose of an gesture action
     */
    private boolean selectOrClearFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        initValues();
    }

    private void initValues() {
        
        List<Integer> digitList = Arrays.asList(digitArray);
        List<Integer> colorList = Arrays.asList(colorArray);
        
        adapter = new MainAdapter(digitList, colorList, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(this);
        
        RecyclerViewAutoScrollHelper helper = new RecyclerViewAutoScrollHelper(recyclerView);
        recyclerView.setOnTouchListener(helper);
        helper.setEnabled(true);

        gestureDetector = new GestureDetector(this, new FastSelectGestureListener());
        
    }

    @Override
    public void onBackPressed() {
        if (adapter.getSelectedItemCount() > 0) {
            clearSelection();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItemCount = menu.getItem(0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onClickListener(View view, int position) {
        adapter.toggleItemSelect(position);

        MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (null != holder) {
            playHolderAnimatorSet(holder, adapter.getItemSelectedState(position));
        }

        menuItemCount.setTitle(String.valueOf(adapter.getSelectedItemCount()));
    }

    private void clearSelection() {
        HashSet<Integer> mSelections = adapter.getMultiSelection();
        for (Integer position : mSelections) {
            MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            if (null != holder) {
                holder.checkableImageView.setChecked(false);
            }
        }

        adapter.clearSelectedItem();
        adapter.notifyDataSetChanged();
        menuItemCount.setTitle(String.valueOf(adapter.getSelectedItemCount()));
    }

    private void performGalleryItemCheck(int position) {
        if (adapter.getItemSelectedState(position) == selectOrClearFlag) {
            adapter.toggleItemSelect(position);

            MainAdapter.ViewHolder holder = (MainAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            if (null != holder) {
                playHolderAnimatorSet(holder, adapter.getItemSelectedState(position));
            }
        }

        menuItemCount.setTitle(String.valueOf(adapter.getSelectedItemCount()));
    }

    private void playHolderAnimatorSet(MainAdapter.ViewHolder holder, boolean selected) {
        holder.checkableImageView.setChecked(selected);

        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
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

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        if (e.getAction() == MotionEvent.ACTION_UP) {
            dragSelectEnable = false;
        }

        if (dragSelectEnable) {
            recyclerView.stopScroll();
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    //gesture listener
    private class FastSelectGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (null != view) {
                initialPosition = recyclerView.getChildAdapterPosition(view);
                lastPosition = initialPosition;
                selectInitialPositionFlag = true;
                positionFlag = false;
                positionTrend = 0;
            }

            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (null == e1 || null == e2)
                return false;

            //only horizontal gesture can active drag select image mode
            if (Math.abs(e1.getX() - e2.getX()) > FAST_SELECT_ACTIVE_X_DISTANCE && Math.abs(e1.getY() - e2.getY()) < FAST_SELECT_ACTIVE_Y_DISTANCE) {
                dragSelectEnable = true;

            } else {
                if (!dragSelectEnable)
                    return false;
            }

            View view = recyclerView.findChildViewUnder(e2.getX(), e2.getY());
            if (null != view && lastPosition != recyclerView.getChildAdapterPosition(view)) {
                int position = recyclerView.getChildAdapterPosition(view);

                //positionFlag updates when gesture direction is changed
                if (((position - lastPosition) > 0 && positionTrend <= 0)
                        || ((position - lastPosition) < 0 && positionTrend >= 0)) {
                    positionFlag = !positionFlag;
                    selectOrClearFlag = adapter.getItemSelectedState(lastPosition);
                    positionTrend = position - lastPosition;
                    selectInitialPositionFlag = true;
                }

                /**
                 * note:
                 * 1. pass different position parameter if positionFlag changes
                 * 2. three situations:
                 *    a. Math.abs(lastPosition - position) == 1
                 *    b. lastPosition - position > 1
                 *    c. lastPosition - position < 1
                 * */
                if (Math.abs(lastPosition - position) == 1) {

                    if (position == initialPosition) {
                        performGalleryItemCheck(initialPosition);
                    }

                    if (positionFlag) {
                        performGalleryItemCheck(position);

                        //check initialPosition only once when everytime starting drag select image
                        if (selectInitialPositionFlag && Math.abs(position - initialPosition) == 1) {
                            performGalleryItemCheck(initialPosition);
                            selectInitialPositionFlag = false;
                        }
                    } else {
                        performGalleryItemCheck(lastPosition);
                    }

                } else if ((lastPosition - position) > 1) {
                    if (positionFlag) {
                        for (int i = position; i < lastPosition; i++) {
                            performGalleryItemCheck(i);
                        }
                    } else {
                        for (int i = position + 1; i <= lastPosition; i++) {
                            performGalleryItemCheck(i);
                        }
                    }

                } else {
                    if (positionFlag) {
                        for (int i = lastPosition + 1; i <= position; i++) {
                            performGalleryItemCheck(i);
                        }
                    } else {
                        for (int i = lastPosition; i < position; i++) {
                            performGalleryItemCheck(i);
                        }
                    }
                }

                lastPosition = position;

            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

    //auto scroll helper
    class RecyclerViewAutoScrollHelper extends AutoScrollHelper {
        private RecyclerView target;

        public RecyclerViewAutoScrollHelper(RecyclerView target) {
            super(target);
            this.target = target;
        }

        @Override
        public void scrollTargetBy(int deltaX, int deltaY) {
            target.scrollBy(deltaX, deltaY);
        }

        @Override
        public boolean canTargetScrollHorizontally(int direction) {
            return target.getLayoutManager().canScrollHorizontally();
        }

        @Override
        public boolean canTargetScrollVertically(int direction) {
            return target.getLayoutManager().canScrollVertically();
        }

        //reduce scroll speed
        @Override
        public AutoScrollHelper setMaximumVelocity(float horizontalMax, float verticalMax) {
            return super.setMaximumVelocity(horizontalMax / 4, verticalMax / 4);
        }

        //reduce scroll speed
        @Override
        public AutoScrollHelper setMinimumVelocity(float horizontalMin, float verticalMin) {
            return super.setMinimumVelocity(horizontalMin / 4, verticalMin / 4);
        }
    }
}
