package com.paris.calculator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paris.calculator.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.ViewHolder> {

    private LinkedHashMap<String, Double> wholeRatesMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> ratesMap = new LinkedHashMap<>();
    private LayoutInflater mInflater;
    private Context context;
    private ItemClickListener mListener;
    private ArrayList<String> currencyNames;


    public CurrencyRecyclerAdapter(Context context, LinkedHashMap<String, Double> ratesMap, ItemClickListener mListener) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        wholeRatesMap.putAll(ratesMap);
        this.ratesMap.putAll(ratesMap);
        this.mListener = mListener;
        currencyNames = getKeysOfRatesMap();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.currency.setText(currencyNames.get(position));
    }


    @Override
    public int getItemCount() {
        return ratesMap.size();
    }


    public void update(LinkedHashMap<String, Double> ratesMap) {
        wholeRatesMap.clear();
        wholeRatesMap.putAll(ratesMap);
        this.ratesMap.clear();
        this.ratesMap.putAll(ratesMap);
        currencyNames = getKeysOfRatesMap();
        notifyDataSetChanged();
    }


    private ArrayList<String> getKeysOfRatesMap() {
        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, Double> entry : ratesMap.entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView currency;

        ViewHolder(View itemView) {
            super(itemView);
            currency = itemView.findViewById(R.id.currency);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, currencyNames.get(getAdapterPosition()));
        }
    }


    public void filter(String text) {
        ratesMap.clear();
        if (text.isEmpty()) {
            ratesMap.putAll(wholeRatesMap);
        } else {
            text = text.toLowerCase();
            for (Map.Entry<String, Double> entry : wholeRatesMap.entrySet()) {
                if (entry.getKey().toLowerCase().contains(text)) {
                    ratesMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        currencyNames = getKeysOfRatesMap();
        notifyDataSetChanged();
    }


    public interface ItemClickListener {
        void onItemClick(View view, String currencyName);
    }

}
