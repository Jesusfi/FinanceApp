package com.codegud.financeapp;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity {
    RecyclerView transactionRecyclerView;
    private FirestoreRecyclerAdapter<Transactions, TransactionActivity.MyViewHolder> adapter; //Firebase UI Firestore Adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        transactionRecyclerView = findViewById(R.id.transactions_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TransactionActivity.this);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = rootRef.collection(userID).document("TransactionInfo").collection("transactions").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Transactions> options = new FirestoreRecyclerOptions.Builder<Transactions>()
                .setQuery(query, Transactions.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Transactions, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Transactions model) {
                holder.dateView.setText(model.getMessage());
                holder.categoryView.setText(model.getCategory());
                holder.amountView.setText(model.getAmount());

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

                } else if (position > 0) {
                    Transactions item = getItem(position - 1);
                    if (!model.getDate().equals(item.getDate())) {
                        holder.dateTestView.setText(newDateString);
                        holder.dateTestView.setVisibility(View.VISIBLE);
                    } else {
                        holder.dateTestView.setVisibility(View.GONE);
                    }
                }

                if (model.getTransactionType().equals("Deposit")) {
                    holder.transactionTypeIndicatorView.setImageResource(R.drawable.ic_arrow_deposit_green_24dp);
                    holder.amountView.setTextColor(ContextCompat.getColor(TransactionActivity.this, R.color.deposit_color));
                } else {
                    holder.transactionTypeIndicatorView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    holder.amountView.setTextColor(ContextCompat.getColor(TransactionActivity.this, R.color.withdraw_color));
                }
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_rv, viewGroup, false);
                return new MyViewHolder(view);
            }
        };

        transactionRecyclerView.setAdapter(adapter);
        transactionRecyclerView.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dateView, amountView, categoryView;
        TextView amountLabelView, categoryLabelView;
        TextView dateTestView;
        LinearLayout labelLayout, parentLayout;
        ImageView transactionTypeIndicatorView;
        CardView cardView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parent);
            cardView = itemView.findViewById(R.id.cardView);

            dateView = itemView.findViewById(R.id.date_rv_tv);
            amountView = itemView.findViewById(R.id.amount_rv_tv);
            categoryView = itemView.findViewById(R.id.category_rv_tv);

            amountLabelView = itemView.findViewById(R.id.amount_label_rv);
            categoryLabelView = itemView.findViewById(R.id.category_label_rv);

            dateTestView = itemView.findViewById(R.id.date_test_rv);
            labelLayout = itemView.findViewById(R.id.label_layout_rv);
            transactionTypeIndicatorView = itemView.findViewById(R.id.transaction_type_indicator_imageView);

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //Toast.makeText(TransactionActivity.this, "Test",Toast.LENGTH_SHORT).show();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(5);
                    }
                    return false;
                }
            });

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
