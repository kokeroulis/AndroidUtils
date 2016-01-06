package kokeroulis.gr.uiforms.forms;

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

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import kokeroulis.gr.uiforms.R;
import kokeroulis.gr.uiforms.validators.NumberValidator;

public class NumberForm<Validator extends NumberValidator> extends BaseForm<Validator> {

    private EditText mEditValue;
    private TextChanged mTextChanged;

    public NumberForm(Context context) {
        super(context);
    }

    public NumberForm(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void restoreValue(Comparable value) {
        mEditValue.setText(value.toString());
    }

    @Override
    protected void setupUi() {
        mEditValue = (EditText) findViewById(R.id.edit);
        mTextChanged = new TextChanged() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    mValidator.setValue(mValidator.charToVal(s.toString()));
                }
            }
        };
        mEditValue.addTextChangedListener(mTextChanged);
    }

    @Override
    public void setValidator(Validator validator) {
        super.setValidator(validator);
        setFilters();
    }

    @Override
    protected int getLayout() {
        return R.layout.number_form;
    }

    public void setInvalidInputListener(NumberValidator.InvalidInputListener listener) {
        mValidator.setInvalidInputListener(listener);
    }

    protected void setFilters() {
        mEditValue.setFilters(new InputFilter[]{mValidator});
        mEditValue.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValidator != null) {
            mValidator.clearListener();
        }
        mTextChanged = null;
    }

    @Override
    public void setPlaceHolder(String text) {
        if (mEditValue != null) {
            mEditValue.setHint(text);
        }
    }
}
