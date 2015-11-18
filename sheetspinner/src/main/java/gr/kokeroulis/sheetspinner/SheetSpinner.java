package gr.kokeroulis.sheetspinner;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import java.lang.ref.WeakReference;
import java.util.List;

public class SheetSpinner extends FrameLayout {
    private List<String> mTitles;
    private ListSheetView mSheetList;
    private TextView mTitle;
    private WeakReference<BottomSheetLayout> mLayout;

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

    public void setTitles(List<String> titles,
                          ListSheetView.OnSheetItemClickListener listener,
                          WeakReference<BottomSheetLayout> layout) {
        mTitles = titles;

        if (mTitle.getText().toString().isEmpty()) {
            mTitle.setText(titles.get(0));
        }

        ListSheetView.OnSelectionChangedListener selectionChangedListener = new ListSheetView.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(String title) {
                mTitle.setText(title);
            }
        };

        mLayout = layout;
        if (mSheetList == null && mTitles != null && mTitles.size() > 0) {
            mSheetList = new ListSheetView(getContext(), mTitle.getText().toString(),
                                           listener, layout, selectionChangedListener);
            mSheetList.setTitles(titles);
        }
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

    public void setEnabled(boolean enabled) {
        if (mSheetList != null) {
            mSheetList.setEnabled(enabled);
        }
    }

    public boolean getEnabled() {
        return mSheetList != null && mSheetList.getEnabled();
    }

    public void setDefault(String title) {
        if (mSheetList != null) {
            mSheetList.setDefault(title);
        }

        mTitle.setText(title);
    }

    public String getDefault() {
        if (mSheetList != null) {
            return mSheetList.getDefault();
        } else if (mTitles != null) {
            return mTitles.get(0);
        } else {
            return "";
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLayout != null && mLayout.get() != null) {
                    mLayout.get().showWithSheetView(mSheetList);
                }
            }
        });
    }
}
