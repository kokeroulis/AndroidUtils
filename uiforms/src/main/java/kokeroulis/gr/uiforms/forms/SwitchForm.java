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
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import kokeroulis.gr.uiforms.R;
import kokeroulis.gr.uiforms.validators.SwitchValidator;

public class SwitchForm extends BaseForm<SwitchValidator> {

    private SwitchCompat mSwitch;

    public SwitchForm(Context context) {
        super(context);
    }

    public SwitchForm(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void restoreValue(Comparable value) {
        boolean isEnabled = value.toString().toLowerCase().equals("true");
        mSwitch.setChecked(isEnabled);
    }

    @Override
    protected void setupUi() {
        mSwitch = (SwitchCompat) findViewById(R.id.edit);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mValidator.setValue(isChecked);
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.switch_form;
    }
}
