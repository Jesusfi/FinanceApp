package com.codegud.financeapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateGoalDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_dialog_update_goal, null);

        final String category  = getArguments().getString("category");

        final EditText amountToUpdateView = v.findViewById(R.id.update_goal_amount_editText);
        amountToUpdateView.addTextChangedListener(new NumberTextWatcher(amountToUpdateView));

        Button updateButton = v.findViewById(R.id.update_goal_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountToUpdateString = amountToUpdateView.getText().toString();
                if(!TextUtils.isEmpty(amountToUpdateString)) {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference ref = db.collection(userID).document("BudgetInfo").collection("budgets").document(category);

                    ref.update("goal",amountToUpdateString).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error " +e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismiss();
                        }
                    });
                }

            }
        });
        builder.setView(v);
        return builder.create();
    }
}

