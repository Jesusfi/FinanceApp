package com.codegud.financeapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class EnvelopeRecycleViewAdapter extends RecyclerView.Adapter<EnvelopeRecycleViewAdapter.ViewHolder> {

    Context mContext;
    ArrayList<Envelope> envelopeArrayList;

    public EnvelopeRecycleViewAdapter(Context mContext, ArrayList<Envelope> mEnvelopeArrayList) {
        this.mContext = mContext;
        this.envelopeArrayList = mEnvelopeArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.envelope_rv, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Envelope envelope = envelopeArrayList.get(i);

        viewHolder.categoryTextView.setText(envelope.getCategory());
        viewHolder.amountTextView.setText(envelope.getAmount());
    }

    @Override
    public int getItemCount() {
        return envelopeArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView categoryTextView, amountTextView;
        CardView parentView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryTextView = itemView.findViewById(R.id.category_tv);
            amountTextView = itemView.findViewById(R.id.amount_tv);
            parentView = itemView.findViewById(R.id.parent_rv_envelope_cardView);

            parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext,EnvelopeActions.class));
                }
            });
        }
    }

    public ArrayList<Envelope> getEnvelopeArrayList(){
        return envelopeArrayList;
    }
}
