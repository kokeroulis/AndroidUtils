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
import android.util.AttributeSet;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import kokeroulis.gr.uiforms.R;
import kokeroulis.gr.uiforms.validators.NumberValidator;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class NumberForm<Validator extends NumberValidator> extends BaseForm<Validator> {

    private EditText mEditValue;
    private Subscription sub;

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

        sub = valueChanged().subscribe(new Action1<String>() {
            @Override
            public void call(String text) {
                mValidator.setValue(mValidator.charToVal(text));
            }
        });
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
    }

    protected Observable<String> valueChanged() {
        return RxTextView
            .textChanges(mEditValue)
            .skip(1) // First event is a blank string "".
            .debounce(400, TimeUnit.MILLISECONDS) //
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Func1<CharSequence, String>() {
                @Override
                public String call(CharSequence text) {
                    return text.toString();
                }
            });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValidator != null) {
            mValidator.clearListener();
        }
        sub.unsubscribe();
    }

    @Override
    public void setPlaceHolder(String text) {
        if (mEditValue != null) {
            mEditValue.setHint(text);
        }
    }
}
