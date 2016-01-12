package gr.kokeroulis.sheetspinner;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flipboard.bottomsheet.commons.BottomSheetFragment;

import java.util.Arrays;
import java.util.List;

@SuppressLint("ValidFragment")
class SheetFragment extends BottomSheetFragment {
    private ListView mList;
    private SheetListListener mListener;
    private Callback mCallback;

    public static SheetFragment newInstance(String[] entries, int layout, Callback callback) {
        SheetFragment f = new SheetFragment();
        Bundle args = new Bundle();
        args.putStringArray("items", entries);
        args.putInt("layout", layout);
        f.setArguments(args);
        f.setCallBack(callback);
        return f;
    }

    public static SheetFragment newInstance(List<String> entries, int layout, Callback callback) {
        String[] array = new String[entries.size()];
        entries.toArray(array);
        return newInstance(array, layout, callback);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_sheet, container, false);
        mList = (ListView) view.findViewById(R.id.list);
        mList.setAdapter(createAdapter());

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = parent.getItemAtPosition(position).toString();
                if (mListener != null) {
                    mListener.onClickListener(title);
                }
                mCallback.setTitle(title);
                dismiss();
            }
        });

        return view;
    }

    public void setOnListClickListener(final SheetListListener listener) {
        mListener = listener;
    }

    private ArrayAdapter<String> createAdapter() {
        List<String> items = Arrays.asList(getArguments().getStringArray("items"));
        return new ListAdapter(getContext(), items);
    }

    public void show(FragmentManager manager) {
        int layout = getArguments().getInt("layout");
        if (layout <= 0) {
            show(manager, R.id.bottomSheetLayout);
        } else {
            show(manager, layout);
        }
    }

    static abstract class Callback {
        private String mTitle;

        void setTitle(String title) {
            mTitle = title;
        }

        String getTitle() {
            return mTitle;
        }
    }

    void setCallBack(Callback callBack) {
        mCallback = callBack;
    }

    class ListAdapter extends ArrayAdapter<String> {

        public ListAdapter(Context context, List<String> items) {
            super(context, R.layout.holder_sheet, R.id.label, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);

            final String item = getItem(position);
            NormalViewHolder holder = new NormalViewHolder(convertView);
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
                final boolean titleIsActive = SheetFragment.this.mCallback.getTitle().equals(item);
                icon.setVisibility(titleIsActive ? View.VISIBLE : View.GONE);
                label.setText(item);
            }
        }
    }

}
