package com.shwaeki.delivery.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.shwaeki.delivery.Models.Profit;
import com.shwaeki.delivery.R;

import java.util.ArrayList;


public class ProfitAdapter extends RecyclerView.Adapter<ProfitAdapter.ViewHolder> {
    private ArrayList<Profit> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView date, total, count;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            date = v.findViewById(R.id.pdate);
            total = v.findViewById(R.id.ptotal);
            count = v.findViewById(R.id.pcount);
        }
    }

    public ProfitAdapter(ArrayList<Profit> myDataset) {
        values = myDataset;
    }

    @Override
    public ProfitAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.profit_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Profit pack = values.get(position);
        holder.date.setText(pack.date);
        holder.total.setText("₪" + pack.total);
        holder.count.setText(String.valueOf(pack.count));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

}
