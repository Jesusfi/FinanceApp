package com.codegud.financeapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddEnvelopDialogFragment extends DialogFragment {
    /**
     * This attaches the AddCategoryListener Interface so we can send
     * the category the user inputs back to the Dashboard activity
     */
    private AddCategoryListener mCallback;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (AddCategoryListener) context;
        }
        catch (ClassCastException e) {
            Log.d("MyDialog", "Activity doesn't implement the ISelectedData interface");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_dialog_add_envelope, null);

        final EditText addCategoryView = v.findViewById(R.id.enter_category_editText);
        final EditText addGoalView = v.findViewById(R.id.add_goal_editText);
        addGoalView.addTextChangedListener(new NumberTextWatcher(addGoalView));
        Button addCategoryButton = v.findViewById(R.id.add_category_button);

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String category = addCategoryView.getText().toString();
                String goal = addGoalView.getText().toString();
                if(!TextUtils.isEmpty(category) && !TextUtils.isEmpty(goal)){
                    mCallback.addNewEnvelope(category,goal);
                    dismiss();
                }else{
                    Toast.makeText(getActivity(),"Please enter amount and goal",Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setView(v);
        return builder.create();
    }
}
