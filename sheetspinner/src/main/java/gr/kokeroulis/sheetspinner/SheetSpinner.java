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
package gr.kokeroulis.sheetspinner;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

public class SheetSpinner extends FrameLayout {
    private TextView mTitle;
    private List<String> mEntries;
    private SheetListListener mListListener;
    private final View.OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            show(getManager());
        }
    };

    public SheetSpinner(Context context) {
        this(context, null);
    }

    public SheetSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SheetSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(getContext(), R.layout.sheetspinner, this);
        mTitle = (TextView) findViewById(R.id.title);
        setSaveEnabled(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString("stateToSave", mTitle.getText().toString());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("instanceState");
            mTitle.setText(bundle.getString("stateToSave"));

        }
        super.onRestoreInstanceState(state);
    }

    public void setEntries(List<String> entries) {
        mEntries = entries;
        mTitle.setText(entries.get(0));
    }

    public void setDefault(String title) {
        mTitle.setText(title);
    }

    public String getDefault() {
        return mTitle.getText().toString();
    }

    public void setOnListClickListener(SheetListListener listener) {
        mListListener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setOnClickListener(null);
        } else {
            setOnClickListener(mListener);
        }
    }

    private void show(FragmentManager manager) {
        SheetFragment.Callback callback = new SheetFragment.Callback() {
            @Override
            void setTitle(String title) {
                super.setTitle(title);
                mTitle.setText(title);
            }
        };
        callback.setTitle(mTitle.getText().toString());
        SheetFragment fragment = SheetFragment.newInstance(mEntries, callback);
        fragment.setOnListClickListener(mListListener);
        fragment.show(manager);
    }

    private FragmentManager getManager() {
        Context context = getContext();

        while (true) {
            while (context instanceof ContextWrapper) {
                if (context instanceof AppCompatActivity) {
                    return ((AppCompatActivity) context).getSupportFragmentManager();
                } else {
                    context = ((ContextWrapper)context).getBaseContext();
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isEnabled()) {
            return;
        }

        setOnClickListener(mListener);
    }
}
