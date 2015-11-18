/*
 * Copyright (C) 2015 Antonis Tsiapaliokas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kokeroulis.gr.segmentedcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class SegmentedControl extends RadioGroup
    implements Observable.OnSubscribe<Integer> {

    private List<String> mItems;
    private AttributeSet mAttrs;
    private SegmentedLayoutImpl mLayout;
    private boolean mUseCustomLayout;

    public SegmentedControl(Context context) {
        this(context, null);
    }

    public SegmentedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void setEntries(List<String> items) {
        // we are adding the same list.do nothing.
        // we need to use that for the viewHolders...
        if (mItems.size() == items.size()) {
            return;
        }

        mItems = items;
        for (int i = 0; i < mItems.size(); i++) {
            boolean hasLeftRadius = i == 0;
            boolean hasRightRadius = (i + 1) == mItems.size();

            SegmentedButton sb = new SegmentedButton(getContext(), mAttrs,
                                                     hasLeftRadius, hasRightRadius);
            sb.setId(i + 1);
            sb.setText(mItems.get(i));
            addView(sb);
        }

        invalidate();
    }

    public void setUseCustomLayout(boolean useCustomLayout) {
        mUseCustomLayout = useCustomLayout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mUseCustomLayout) {
            mLayout.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mUseCustomLayout) {
            mLayout.onLayout(changed, l, t, r, b);
        } else {
            super.onLayout(changed, l, t, r, b);
        }
    }

    public SegmentedButton findButtonById(int id) {
        return (SegmentedButton) findViewById(id);
    }



    public SegmentedButton findButtonBySlug(final String slug) {
        for (int i = 0; i< getChildCount(); i++) {
            SegmentedButton sb = (SegmentedButton) getChildAt(i);
            if (sb.getText().toString().equalsIgnoreCase(slug)) {
                return sb;
            }
        }
        return null;
    }

    public void setClickable(boolean clickable) {
        for (int i = 0; i< getChildCount(); i++) {
            SegmentedButton sb = (SegmentedButton) getChildAt(i);
            sb.setClickable(clickable);
        }
    }

    private void init(AttributeSet attrs) {
        mAttrs = attrs;
        mLayout = new SegmentedLayoutImpl();
        mItems = new ArrayList<>();
    }

    public Observable<Integer> selectionChanged() {
        return Observable.create(this);
    }

    @Override
    public void call(final Subscriber<? super Integer> subscriber) {
        SegmentedControl.this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (subscriber != null && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(checkedId);
                }
            }
        });

        subscriber.add(new Subscription() {
            @Override
            public void unsubscribe() {
                SegmentedControl.this.setOnCheckedChangeListener(null);
            }

            @Override
            public boolean isUnsubscribed() {
                return subscriber != null && subscriber.isUnsubscribed();
            }
        });
    }


    private class SegmentedLayoutImpl {

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

            int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
            final int count = getChildCount();
            int line_height = 0;

            int xpos = getPaddingLeft();
            int ypos = getPaddingTop();

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {

                    child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(pxToDp(35), MeasureSpec.EXACTLY));
                    final int childw = child.getMeasuredWidth();
                    if (xpos + childw > width) {
                        xpos = getPaddingLeft();
                        ypos += line_height;
                    }

                    xpos += childw;
                    line_height = child.getMeasuredHeight();
                }
            }

            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
                height = ypos + line_height;
            } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                if (ypos + line_height < height) {
                    height = ypos + line_height;
                }
            }
            setMeasuredDimension(width, height);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int count = getChildCount();
            final int width = r - l;
            int xpos = getPaddingLeft();
            int ypos = getPaddingTop();
            int lineHeight = 0;

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    final int childw = child.getMeasuredWidth();
                    final int childh = child.getMeasuredHeight();

                    if (xpos + childw > width) {
                        xpos = getPaddingLeft();
                        ypos += lineHeight;
                    }

                    lineHeight = child.getMeasuredHeight();

                    child.layout(xpos, ypos, xpos + childw, ypos + childh);
                    xpos += childw;
                }
            }
        }
    }

    private int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) ((px * displayMetrics.density));
    }
}
