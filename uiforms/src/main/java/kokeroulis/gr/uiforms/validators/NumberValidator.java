package kokeroulis.gr.uiforms.validators;

/*  Copyright (C) 2015 Antonis Tsiapaliokas
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    */

import android.text.InputFilter;
import android.text.Spanned;

public abstract class NumberValidator<T extends Comparable<T>>
    implements InputFilter,Validator<T>  {

    private final T mMaxVal;
    private final T mMinVal;
    private InvalidInputListener mListener;

    private static final int BIGGER = 1;
    private static final int ZERO = 0;
    private static final int LOWER = -1;

    public static final int INVALID_INPUT = 2;
    public static final int OUTOF_RANGE_INPUT = 3;

    public interface InvalidInputListener {
        void onInvalidInput(String source, int reasonId);
    }

    public NumberValidator(T minVal, T maxVal) {
        mMaxVal = maxVal;
        mMinVal = minVal;
    }

    public T getMinVal() {
        return mMinVal;
    }

    public T getMaxVal() {
        return mMaxVal;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String number = dest.toString() + source;
        final T val = charToVal(number);

        final int minValSize = mMinVal.toString().length();
        // Always allow the first digit
        if (dstart <= (minValSize -1)
            && val.compareTo(charToVal("0")) != LOWER) {
            return source;
        } else if (val.compareTo(charToVal("-1")) == ZERO) {
            if (mListener != null) {
                mListener.onInvalidInput(number, INVALID_INPUT);
            }
            return "";
        } else if (val.compareTo(charToVal("0")) != LOWER
            && val.compareTo(mMaxVal) != BIGGER
            && val.compareTo(mMinVal) != LOWER) {
            return source;
        } else {
            if (mListener != null) {
                mListener.onInvalidInput(number, OUTOF_RANGE_INPUT);
            }
            return "";
        }
    }

    public void setInvalidInputListener(InvalidInputListener listener) {
        mListener = listener;
    }

    public void clearListener() {
        mListener = null;
    }

    public abstract T charToVal(String source);

    @Override
    public boolean isValid() {
        if (getValue() == null) {
            return false;
        } else {
            return getValue().compareTo(charToVal("0")) == BIGGER
                && getValue().compareTo(mMinVal) == BIGGER
                && getValue().compareTo(mMaxVal) == LOWER;
        }
    }
}
