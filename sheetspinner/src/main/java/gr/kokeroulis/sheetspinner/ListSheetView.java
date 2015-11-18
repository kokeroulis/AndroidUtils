package gr.kokeroulis.sheetspinner;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import java.lang.ref.WeakReference;
import java.util.List;

public class ListSheetView extends FrameLayout {

    public interface OnSheetItemClickListener {
        void onSheetItemClick(String text, int pos);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(String title);
    }

    private List<String> mValues;
    private Adapter adapter;
    private AbsListView absListView;
    protected final int originalListPaddingTop;
    private WeakReference<BottomSheetLayout> mLayout;
    private String mTitle;

    public ListSheetView(final Context context, @Nullable final CharSequence title,
                         final OnSheetItemClickListener listener,
                         final WeakReference<BottomSheetLayout> layout,
                         final OnSelectionChangedListener selectionListener) {
        super(context);

        // Inflate the appropriate view and set up the absListView
        inflate(context, R.layout.list_sheet, this);
        absListView = (AbsListView) findViewById(R.id.list);
        mLayout = layout;
        if (listener != null) {
            absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mTitle = adapter.getItem(position);
                    listener.onSheetItemClick(mTitle, position);
                    selectionListener.onSelectionChanged(mTitle);
                    if (mLayout != null && mLayout.get() != null) {
                        mLayout.get().dismissSheet();
                    }
                }
            });
        }

        originalListPaddingTop = absListView.getPaddingTop();
    }

    public void setTitles(List<String> titles) {
        mValues = titles;
        mTitle = titles.get(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.adapter = new Adapter();
        absListView.setAdapter(this.adapter);
    }

    private class Adapter extends BaseAdapter {

        private final LayoutInflater inflater;

        public Adapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return mValues.size();
        }

        @Override
        public String getItem(int position) {
            return mValues.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final String item = getItem(position);

            NormalViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.holder_sheet, parent, false);
                holder = new NormalViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (NormalViewHolder) convertView.getTag();
            }
            holder.bindView(item);

            return convertView;
        }

        class NormalViewHolder {
            final ImageView icon;
            final TextView label;

            NormalViewHolder(View root) {
                icon = (ImageView) root.findViewById(R.id.icon);
                label = (TextView) root.findViewById(R.id.label);
            }

            public void bindView(String item) {
                icon.setVisibility(mTitle.equals(item) ? VISIBLE : GONE);
                label.setText(item);
            }
        }
    }
}
