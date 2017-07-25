package com.kikyo.note.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kikyo.note.R;

import java.util.List;

/**
 * Created by 婷 on 2017/7/25.
 */

//好了。你大概知道我剛才寫了什麽碼。等一下哦我用一下這個類給你看一下。現在大概知道我寫了什麽嗎知道了
public class OperationMenuDialogBuilder extends MaterialDialog.Builder {


    public interface OnOperationSelectListener {

        void onSelect(String text, int position);

    }

    private RecyclerView mOperations;
    private OnOperationSelectListener mOperationSelectListener;
    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mOperationSelectListener == null){
                return;
            }
            OperationViewHolder h = (OperationViewHolder) mOperations.getChildViewHolder(v);
            mOperationSelectListener.onSelect(h.text.getText().toString(), h.getAdapterPosition());
        }
    };


    public OperationMenuDialogBuilder(@NonNull Context context) {
        super(context);
        View view = View.inflate(context, R.layout.operation_menu, null);
        mOperations = (RecyclerView) view.findViewById(R.id.operations);
        customView(view, false);
    }

    public OperationMenuDialogBuilder operations(List<String> operations){
        mOperations.setAdapter(new OperationAdapter(operations));
        return this;
    }

    public OperationMenuDialogBuilder selectListener(OnOperationSelectListener listener){
        mOperationSelectListener = listener;
        return this;
    }

    private class OperationAdapter extends RecyclerView.Adapter<OperationViewHolder> {

        List<String> mOperations;

         OperationAdapter(List<String> operations) {
            mOperations = operations;
        }

        @Override
        public OperationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OperationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.operation_menu_item, parent, false));
        }

        @Override
        public void onBindViewHolder(OperationViewHolder holder, int position) {
            holder.text.setText(mOperations.get(position));
        }

        @Override
        public int getItemCount() {
            return mOperations.size();
        }
    }

    private class OperationViewHolder extends RecyclerView.ViewHolder {

        TextView text;

         OperationViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(mOnItemClickListener);
        }

    }
}
