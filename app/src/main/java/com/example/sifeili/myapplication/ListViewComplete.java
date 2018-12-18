package com.example.sifeili.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewComplete extends ListView {

    public ListViewComplete(Context context) {
        super(context);
    }

    public ListViewComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);

    }
}
