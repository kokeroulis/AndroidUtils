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

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

public class SegmentedShape {
    private int mColorPrimary;
    private int mColorAccent;
    private float mStokeRadius;
    private ColorStateList mColorStateList;

    public SegmentedShape(int colorPrimary, int colorAccent, float strokeRadius) {
        mColorPrimary = colorPrimary;
        mColorAccent = colorAccent;
        mStokeRadius = strokeRadius;
        generateColorStateList();
    }

    private void generateColorStateList() {
        final int[][] states = new int[][] {
            new int[] { android.R.attr.state_checked},
            new int[] { -android.R.attr.state_checked},
        };
        final int[] colors = new int[] { mColorAccent, mColorPrimary };

        mColorStateList = new ColorStateList(states, colors);
    }

    public StateListDrawable generateSelectorFromDrawables(Drawable checked, Drawable unchecked) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{ -android.R.attr.state_checked}, unchecked);
        states.addState(new int[]{android.R.attr.state_checked}, checked);

        return states;
    }

    public ColorStateList getColorStateList() {
        return mColorStateList;
    }

    public Drawable generateShape(int color, boolean hasLeftRadius, boolean hasRightRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setStroke(2, mColorPrimary);

        // Look at the comments of the setCornerRadii for more details
        if (hasLeftRadius) {
            drawable.setCornerRadii(new float[] {mStokeRadius, mStokeRadius,
                                                 0, 0, 0, 0,
                                                 mStokeRadius, mStokeRadius}); // left radius
        } else if (hasRightRadius) {
            drawable.setCornerRadii(new float[] {0, 0,
                                                 mStokeRadius, mStokeRadius,
                                                 mStokeRadius, mStokeRadius,
                                                 0, 0}); // right radius
        }

        return drawable;
    }

    public Drawable buildSelectorShapeFromColors(boolean hasLeftRadius, boolean hasRightRadius) {

        Drawable checked = generateShape(mColorPrimary, hasLeftRadius, hasRightRadius);
        Drawable unchecked = generateShape(mColorAccent, hasLeftRadius, hasRightRadius);
        return generateSelectorFromDrawables(checked, unchecked);
    }
}
