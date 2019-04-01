package com.codegud.financeapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;


public class EnvelopeActions extends AppCompatActivity implements AddTransactionListener {

    private String category;

    private FirebaseFirestore db;
    private TextView currentAmountView;

    private FirestoreRecyclerAdapter<Transactions, MyViewHolder> adapter; //Firebase UI Firestore Adapter
    private ProgressBar goalProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envelope_actions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = getIntent();
        category = intent.getExtras().getString(DashBoardActivity.CATEGORY_TO_UPDATE);

        getSupportActionBar().setTitle(category);//This is located b/c it waits for the intent

        currentAmountView = findViewById(R.id.total_amount_tv);
        final TextView currentGoalView = findViewById(R.id.goal_amount_tv);
        goalProgressBar = findViewById(R.id.goal_progressBar);
        CardView withdrawView = findViewById(R.id.withdraw_cardView);
        CardView depositView = findViewById(R.id.deposit_cardView);
        final TextView percentProgressView = findViewById(R.id.progress_amount_tv);

        DocumentReference docReference = db.collection(userID).document("BudgetInfo").collection("budgets").document(category);
        docReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Envelope currentItem = documentSnapshot.toObject(Envelope.class);
                currentAmountView.setText(currentItem.getAmount());
                currentGoalView.setText(MoneyManager.FormatMoney(currentItem.getGoal()));
                goalProgressBar.setProgress(currentItem.getProgress());

                if (currentItem.getProgress() < 100) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            int color = ContextCompat.getColor(EnvelopeActions.this, R.color.progress_goal_not_yet_met);
                            goalProgressBar.setProgressTintList(ColorStateList.valueOf(color));
                        }
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        docReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Envelope model = documentSnapshot.toObject(Envelope.class);
                if (model != null) {
                    currentAmountView.setText(model.getAmount());
                    goalProgressBar.setProgress(model.getProgress());
                    percentProgressView.setText("" + model.getProgress() + "%");
                    currentGoalView.setText(MoneyManager.FormatMoney(model.getGoal()));

                    if (model.getProgress() < 100) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                int color = ContextCompat.getColor(EnvelopeActions.this, R.color.progress_goal_not_yet_met);
                                goalProgressBar.setProgressTintList(ColorStateList.valueOf(color));
                            }
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                int color = ContextCompat.getColor(EnvelopeActions.this, R.color.progress_goal_met);
                                goalProgressBar.setProgressTintList(ColorStateList.valueOf(color));
                            }
                        }
                    }
                }
            }
        });

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

        Query query = db.collection(userID).document("TransactionInfo").collection("transactions").whereEqualTo("category", category).orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Transactions> options = new FirestoreRecyclerOptions.Builder<Transactions>()
                .setQuery(query, Transactions.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Transactions, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Transactions model) {
                holder.amountView.setText(model.getAmount());
                holder.dataView.setText(model.getMessage());
                holder.categoryView.setText(model.getCategory());


                String OLD_FORMAT = "MM-dd-yyyy";
                String NEW_FORMAT = "MMMM d, yyyy";

                SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
                Date d = null;

                try {
                    d = sdf.parse(model.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                sdf.applyPattern(NEW_FORMAT);
                String newDateString = sdf.format(d);
                if (position == 0) {
                    holder.dateTestView.setText(newDateString);
                    holder.dateTestView.setVisibility(View.VISIBLE);
                    //Label Views
                    holder.labelLayout.setVisibility(View.VISIBLE);

                } else if (position > 0) {
                    Transactions item = getItem(position - 1);
                    if (!model.getDate().equals(item.getDate())) {
                        holder.dateTestView.setText(newDateString);
                        holder.dateTestView.setVisibility(View.VISIBLE);

                        holder.labelLayout.setVisibility(View.VISIBLE);//label views
                    } else {
                        holder.dateTestView.setVisibility(View.GONE);
                        holder.labelLayout.setVisibility(View.GONE);//label views
                    }
                }

                if (model.getTransactionType().equals("Deposit")) {
                    holder.dataView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.deposit_color));
                    holder.categoryView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.deposit_color));
                    holder.amountView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.deposit_color));
                } else {
                    holder.dataView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.withdraw_color));
                    holder.categoryView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.withdraw_color));
                    holder.amountView.setTextColor(ContextCompat.getColor(EnvelopeActions.this, R.color.withdraw_color));
                }
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_rv, viewGroup, false);
                return new MyViewHolder(view);
            }
        };

        rv.setAdapter(adapter);
        final NestedScrollView nestedScrollView = findViewById(R.id.envlope_actions_parent_nestScrollView);
        nestedScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (nestedScrollView.getScrollY() > 200) {
                    getSupportActionBar().setElevation(10);
                    getSupportActionBar().setSubtitle(currentAmountView.getText().toString());
                } else {
                    getSupportActionBar().setSubtitle(null);
                }
            }
        });
    }

    private void deleteEntry(FirebaseFirestore db, String category) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(userID).document("BudgetInfo").collection("budgets").document(category).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

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


                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                deleteEntry(db, category);
                                deleteTransactions(db, category);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(EnvelopeActions.this);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();



                return true;
            case R.id.update_goal:
                UpdateGoalDialogFragment updateGoalDialogFragment = new UpdateGoalDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("category", category);
                updateGoalDialogFragment.setArguments(bundle);

                FragmentManager fragmentManager = getSupportFragmentManager();
                updateGoalDialogFragment.show(fragmentManager, "Tag");

                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteTransactions(FirebaseFirestore db, String category) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final CollectionReference reference = db.collection(userID).document("TransactionInfo").collection("transactions");
        Query query = reference.whereEqualTo("category", category);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        reference.document(document.getId()).delete();
                    }
                }
            }
        });
    }

    @Override
    public void addNewTransactionAndUpdateEnvelope(String amountOfTransaction, final String type, final String memo) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DocumentReference reference = db.collection(userID).document("BudgetInfo").collection("budgets").document(category);
        String formatStringOldAmount = MoneyManager.FormatMoney(currentAmountView.getText().toString());
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
                // currentAmountView.setText(finalEndResult);
                createNewTransaction(formatStringTransactionAmount, type, memo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void createNewTransaction(String amount, String type, String memo) {
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        String dateString = formatter.format(todayDate);

        if (TextUtils.isEmpty(memo)) {
            memo = "None";
        }
        Transactions transactions = new Transactions(dateString, amount, type, memo, category);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(userID).document("TransactionInfo").collection("transactions").add(transactions)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EnvelopeActions.this, "Failed Transaction", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // finish();
                        adapter.notifyDataSetChanged();

                    }
                });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryView, amountView, dataView;
        TextView categoryLabelView, amountLabelView, dateLabelView;
        LinearLayout labelLayout;
        TextView dateTestView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryView = itemView.findViewById(R.id.category_rv_tv);
            amountView = itemView.findViewById(R.id.amount_rv_tv);
            dataView = itemView.findViewById(R.id.date_rv_tv);

            categoryLabelView = itemView.findViewById(R.id.category_label_rv);
            amountLabelView = itemView.findViewById(R.id.amount_label_rv);
            dateLabelView = itemView.findViewById(R.id.date_label_rv);

            labelLayout = itemView.findViewById(R.id.label_layout_rv);
            dateTestView = itemView.findViewById(R.id.date_test_rv);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
