package com.codegud.financeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class EnvelopeActions extends AppCompatActivity implements AddTransactionListener {

    private String category;
    private String amount, goal;
    private int progress;
    private FirebaseFirestore db;
    private TextView currentAmountView;

    private FirestoreRecyclerAdapter<Transactions, MyViewHolder> adapter; //Firebase UI Firestore Adapter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envelope_actions);
        getSupportActionBar().setElevation(0);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        category = intent.getExtras().getString(DashBoardActivity.CATEGORY_TO_UPDATE);
        amount = intent.getExtras().getString(DashBoardActivity.AMOUNT_TO_UPDATE);
        goal = intent.getExtras().getString(DashBoardActivity.GOAL_TO_PASS_FROM_DASH_TO_ACTIONS);
        progress = intent.getExtras().getInt(DashBoardActivity.PROGRESS_TO_PASS_FROM_DASH_TO_ACTIONS);

        getSupportActionBar().setTitle(category);//This is located b/c it waits for the intent

        currentAmountView = findViewById(R.id.total_amount_tv);
        TextView currentGoalView = findViewById(R.id.goal_amount_tv);
        ProgressBar goalProgressBar = findViewById(R.id.goal_progressBar);
        CardView withdrawView = findViewById(R.id.withdraw_cardView);
        CardView depositView = findViewById(R.id.deposit_cardView);

        currentAmountView.setText(amount);
        currentGoalView.setText("Goal: " + goal);
        goalProgressBar.setProgress(progress);

        withdrawView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTransactionDialogFragment newTransactionDialogFragment = new NewTransactionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "Withdraw");
                newTransactionDialogFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                newTransactionDialogFragment.show(fragmentManager, "TAG");
            }
        });

        depositView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTransactionDialogFragment newTransactionDialogFragment = new NewTransactionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "Deposit");

                newTransactionDialogFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                newTransactionDialogFragment.show(fragmentManager, "TAG");
            }
        });


        RecyclerView rv = findViewById(R.id.transactions_recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(EnvelopeActions.this);
        rv.setLayoutManager(linearLayoutManager);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        Query query = rootRef.collection(DashBoardActivity.MINI_BUDGETS_NAME_LOCATION+"transactions").whereEqualTo("category",category);

        FirestoreRecyclerOptions<Transactions> options = new FirestoreRecyclerOptions.Builder<Transactions>()
                .setQuery(query, Transactions.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Transactions, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Transactions model) {
                holder.amountView.setText(model.getAmount());
                holder.dataView.setText(model.getDate());
                holder.categoryView.setText(model.getCategory());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_rv, viewGroup, false);
                return new MyViewHolder(view);
            }
        };

        rv.setAdapter(adapter);


    }

    private void deleteEntry(FirebaseFirestore db, String category) {
        db.collection(DashBoardActivity.MINI_BUDGETS_NAME_LOCATION).document(category).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.envelope_actions_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_item:
                deleteEntry(db, category);
                Toast.makeText(EnvelopeActions.this, "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void addNewTransactionAndUpdateEnvelope(String amountOfTransaction, final String type) {
        final DocumentReference reference = db.collection(DashBoardActivity.MINI_BUDGETS_NAME_LOCATION).document(category);
        String formatStringOldAmount = MoneyManager.FormatMoney(amount);
        final String formatStringTransactionAmount = MoneyManager.FormatMoney(amountOfTransaction);

        String endResult = "";
        if (type.equals("Deposit")) {
            endResult = MoneyManager.add(formatStringOldAmount, formatStringTransactionAmount);
        } else {
            endResult = MoneyManager.subtract(formatStringOldAmount, formatStringTransactionAmount);
        }

        final String finalEndResult = endResult;
        reference.update("amount", finalEndResult).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                currentAmountView.setText(finalEndResult);
                createNewTransaction(formatStringTransactionAmount, type);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void createNewTransaction(String amount, String type) {
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        String dateString = formatter.format(todayDate);

        Transactions transactions = new Transactions(dateString, amount, type, "Empty", category);
        db.collection(DashBoardActivity.MINI_BUDGETS_NAME_LOCATION+"transactions").add(transactions)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EnvelopeActions.this, "Failed Transaction", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(EnvelopeActions.this, "Sucess Transaction ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryView, amountView, dataView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryView = itemView.findViewById(R.id.category_rv_tv);
            amountView = itemView.findViewById(R.id.amount_rv_tv);
            dataView = itemView.findViewById(R.id.date_rv_tv);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening(); //start listening Firestore db to fill recycleView

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();//stop listening to Firestore db
        }
    }
}
