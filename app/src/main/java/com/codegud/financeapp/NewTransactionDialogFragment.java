package com.codegud.financeapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewTransactionDialogFragment extends DialogFragment {
    private AddTransactionListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (AddTransactionListener) context;
        } catch (ClassCastException e) {
            Log.d("MyDialog", "Activity doesn't implement the ISelectedData interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_dialog_withdraw_or_deposit, null);

        final EditText amountView = v.findViewById(R.id.new_transaction_amount_editText);
        amountView.addTextChangedListener(new NumberTextWatcher(amountView));
        final EditText memoView = v.findViewById(R.id.new_transaction_memo_editText);

        Button updateButton = v.findViewById(R.id.new_transaction_update_button);

        final String transactionType = getArguments().getString("type");
        updateButton.setText(transactionType);

        if (transactionType.equals("Withdraw")) {
            updateButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.withdraw_color));
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = amountView.getText().toString();
                String memo = memoView.getText().toString();
                if(TextUtils.isEmpty(amount)){
                    amountView.requestFocus();
                }
                if (!TextUtils.isEmpty(amount)) {
                    mCallback.addNewTransactionAndUpdateEnvelope(amount, transactionType, memo);
                    dismiss();
                }
            }
        });


        builder.setView(v);
        return builder.create();
    }
}
