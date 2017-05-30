package com.example.ray.finalex.DiaryPacket;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class MyItemAnimator extends RecyclerView.ItemAnimator {

    private ArrayList<RecyclerView.ViewHolder> mPendingAddHolders =
            new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingRemoveHolders =
            new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoveHolders =
            new ArrayList<>();

    private ArrayList<RecyclerView.ViewHolder> mAddAnimtions = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mRemoveAnimations = new ArrayList<>();
    private ArrayList<MoveInfo> mMoveAnimtions = new ArrayList<>();

    class MoveInfo {
        private RecyclerView.ViewHolder holder;
        private int fromX;
        private int fromY;
        private int toX;
        private int toY;

        public MoveInfo(RecyclerView.ViewHolder holder,
                        int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    @Override
    public void runPendingAnimations() {
        boolean isRemove = !mPendingRemoveHolders.isEmpty();
        boolean isMove = !mPendingMoveHolders.isEmpty();
        boolean isAdd = !mPendingAddHolders.isEmpty();

        if(!isRemove && !isMove && !isAdd) return;

        // first remove
        if(isRemove) {
            for(RecyclerView.ViewHolder holder : mPendingRemoveHolders) {
                animateRemoveImpl(holder);
            }
            mPendingRemoveHolders.clear();
        }

        // then move
        if(isMove) {
            ArrayList<MoveInfo> infos = new ArrayList<>();
            infos.addAll(mPendingMoveHolders);
            mPendingMoveHolders.clear();
            for(MoveInfo info : infos) {
                animateMoveImpl(info);
            }
            infos.clear();
        }

        // last add
        if(isAdd) {
            ArrayList<RecyclerView.ViewHolder> holders = new ArrayList<>();
            holders.addAll(mPendingAddHolders);
            mPendingAddHolders.clear();
            for(RecyclerView.ViewHolder holder : holders) {
                animateAddImpl(holder);
            }
            holders.clear();
        }
    }

    // 执行添加动画
    private void animateAddImpl(final RecyclerView.ViewHolder holder) {
        mAddAnimtions.add(holder);
        final View item = holder.itemView;
        ObjectAnimator animator = ObjectAnimator.ofFloat(item, "alpha", 0.f, 1.f);
        animator.setDuration(1000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchAddStarting(holder);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                item.setAlpha(1.f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAddFinished(holder);
                mAddAnimtions.remove(holder);
                if (!isRunning()) dispatchAnimationsFinished();
            }
        });
        animator.start();
    }

    // 执行移动动画
    private void animateMoveImpl(final MoveInfo info) {
        mMoveAnimtions.remove(info);
        final View view = info.holder.itemView;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view,
                "translationY", view.getTranslationY(), 0);
        animator.setDuration(1000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchMoveStarting(info.holder);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchMoveFinished(info.holder);
                mMoveAnimtions.remove(info.holder);
                if(!isRunning()) dispatchAnimationsFinished();
            }
        });
        animator.start();
    }

    // 执行移出动画
    private void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        mRemoveAnimations.add(holder);
        final View item = holder.itemView;
        ObjectAnimator animator = ObjectAnimator.ofFloat(item, "alpha", 1.f, 0.f);
        animator.setDuration(1000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchRemoveStarting(holder);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRemoveAnimations.remove(holder);
                item.setAlpha(1.f);
                dispatchRemoveFinished(holder);
                if (!isRunning()) dispatchAnimationsFinished();
            }
        });
        animator.start();
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0.f);
        mPendingAddHolders.add(holder);
        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        mPendingRemoveHolders.add(holder);
        return true;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder,
                               int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        fromY += view.getTranslationY();
        int delta = toY - fromY;
        view.setTranslationY(-delta);
        MoveInfo info = new MoveInfo(holder, fromX, fromY, toX, toY);
        mPendingMoveHolders.add(info);
        return true;
    }

    @Override
    public boolean isRunning() {
        return !(mPendingAddHolders.isEmpty()
                && mPendingMoveHolders.isEmpty()
                && mPendingRemoveHolders.isEmpty()
                && mAddAnimtions.isEmpty()
                && mRemoveAnimations.isEmpty()
                && mMoveAnimtions.isEmpty());
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                 RecyclerView.ViewHolder newHolder,
                                 int fromLeft, int fromTop, int toLeft, int toTop) {
        return false;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }
}

