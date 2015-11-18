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
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RadioButton;

public class SegmentedButton extends RadioButton {
    public SegmentedButton(Context context) {
        this(context, null, false, false);
    }

    public SegmentedButton(Context context, AttributeSet attrs) {
        this(context, attrs, false, false);
    }

    public SegmentedButton(Context context, AttributeSet attrs, boolean hasLeftRadius, boolean hasRightRadius) {
        super(context, attrs, R.attr.buttonStyle);
        draw(hasLeftRadius, hasRightRadius);
    }

    private void draw(boolean hasLeftRadius, boolean hasRightRadius) {
        final int colorPrimary = getColorFromAttr(R.attr.colorPrimary);
        final int colorAccent = getColorFromAttr(R.attr.colorAccent);

        SegmentedShape shape = new SegmentedShape(colorPrimary, colorAccent, pxToDp(10));
        setBackground(shape.buildSelectorShapeFromColors(hasLeftRadius, hasRightRadius));
        setTextColor(shape.getColorStateList());
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, pxToDp(35)));
    }

    private int getColorFromAttr(int attr) {
        final TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    private int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) ((px * displayMetrics.density));
    }
}
