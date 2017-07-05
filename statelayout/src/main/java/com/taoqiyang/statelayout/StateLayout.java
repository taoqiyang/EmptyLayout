package com.taoqiyang.statelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StateLayout extends FrameLayout {
    private int mEmptyResource;
    private int mErrorResource;
    private int mLoadingResource;

    private View mEmptyView;
    private View mContentView;
    private View mErrorView;
    private View mLoadingView;

    private TextView mTvErrorText;
    private TextView mTvEmptyText;
    private TextView mTvLoadingText;

    private View mRetryClickView;
    private LayoutInflater mInflater;
    private OnRetryClickListener mRetryClickListener;

    public StateLayout(Context context) {
        this(context, null);
    }

    public StateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StateLayout, 0, 0);
        mEmptyResource = ta.getResourceId(R.styleable.StateLayout_slEmptyLayout, R.layout.layout_empty);
        mLoadingResource = ta.getResourceId(R.styleable.StateLayout_slLoadingLayout, R.layout.layout_loading);
        mErrorResource = ta.getResourceId(R.styleable.StateLayout_slErrorLayout, R.layout.layout_error);
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() == 1){
            //如果布局中只有一个子view，则直接用这个view当作contentView，如果子view个数为0或者>1则需要自己手动设置
            mContentView = getChildAt(0);
        }
    }

    public void setContentView(View view) {
        mContentView = view;
    }

    public void setEmptyView(int resId) {
        if(this.mEmptyResource == resId) return;
        this.mEmptyResource = resId;
        mEmptyView = null;
    }

    public void setLoadingView(int resId) {
        if(this.mLoadingResource == resId) return;
        this.mLoadingResource = resId;
        mLoadingView = null;
    }

    public void setErrorView(int resId) {
        if(this.mErrorResource == resId) return;
        this.mErrorResource = resId;
        mErrorView = null;
    }

    private View addView(int resID){
        View view = mInflater.inflate(resID, this, false);
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    private void setText(TextView tv, CharSequence text){
        if(tv != null && text != null){
            tv.setText(text);
        }
    }

    public void showEmpty(){
        showEmpty(null);
    }

    public void showEmpty(CharSequence text) {
        if (mEmptyView == null) {
            mEmptyView = addView(mEmptyResource);
            mTvEmptyText = (TextView) mEmptyView.findViewById(R.id.sl_tv_empty);
        }
        setGone(mEmptyView);
        mEmptyView.setVisibility(View.VISIBLE);
        setText(mTvEmptyText, text);
    }

    public void showError() {
        showError(null);
    }

    public void showError(CharSequence text) {
        if (mErrorView == null) {
            mErrorView = addView(mErrorResource);
            mTvErrorText = (TextView) mErrorView.findViewById(R.id.sl_tv_error);
            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null != mRetryClickListener){
                        v.setClickable(false);//避免重复点击，按理说点击重试后会进入其它状态，当重新进行error时在设置为clickable
                        mRetryClickListener.onRetryClick();
                    }else{
                        Toast.makeText(getContext(), "nothing to do!", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            mRetryClickView = mTvErrorText == null ? mErrorView : mTvErrorText;
            mRetryClickView.setOnClickListener(listener);
        }
        setGone(mErrorView);
        mErrorView.setVisibility(View.VISIBLE);
        mRetryClickView.setClickable(true);
        setText(mTvErrorText, text);
    }

    public void showLoading(CharSequence text) {
        if (mLoadingView == null) {
            mLoadingView = addView(mLoadingResource);
            mTvLoadingText = (TextView) mLoadingView.findViewById(R.id.sl_tv_loading);
        }
        setGone(mLoadingView);
        mLoadingView.setVisibility(View.VISIBLE);
        setText(mTvLoadingText, text);
    }

    public void showLoading() {
        showLoading(null);
    }

    public void showSuccess() {
        setGone(mContentView);
        if (mContentView != null){
            mContentView.setVisibility(View.VISIBLE);
        }else{
            throw new RuntimeException("没有指定contentView,通过setContentView设置或者在布局中作为StateLayout的唯一子view");
        }
    }

    private void setGone(View excludeView) {
        setVisibility(mEmptyView == excludeView ? null : mEmptyView, View.GONE);
        setVisibility(mErrorView == excludeView ? null : mErrorView, View.GONE);
        setVisibility(mLoadingView == excludeView ? null : mLoadingView, View.GONE);
        setVisibility(mContentView == excludeView ? null : mContentView, View.GONE);
    }

    private void setVisibility(View view, int visibility){
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 监听重试
     * @param listener {@link OnRetryClickListener}
     */
    public void setOnRetryClickListener(OnRetryClickListener listener){
        this.mRetryClickListener = listener;
    }

    public interface OnRetryClickListener{
        void onRetryClick();
    }
}
