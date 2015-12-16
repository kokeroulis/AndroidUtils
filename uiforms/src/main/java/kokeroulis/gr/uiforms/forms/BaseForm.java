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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class BaseForm<Validator> extends LinearLayout {

    protected Validator mValidator;

    public BaseForm(Context context) {
        this(context, null);
    }

    public BaseForm(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI(context);
    }

    private void initUI(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(getLayout(), this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupUi();
    }

    public Validator getValidator() {
        if (mValidator == null) {
            throw new RuntimeException("Forms must set a validator"
                + " before they are being accessed!");
        }
        return mValidator;
    }

    public void setValidator(Validator validator) {
        mValidator = validator;
    }

    protected abstract void setupUi();

    protected abstract int getLayout();
}
