package com.codegud.financeapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DashBoardActivity extends AppCompatActivity implements AddCategoryListener {

    private static final String TAG = "TAD";

    public static final String CATEGORY_TO_UPDATE = "categoryToUpdate";
    public static final String AMOUNT_TO_UPDATE = "amountToUpdate";
    public static final String MINI_BUDGETS_NAME_LOCATION = FirebaseAuth.getInstance().getCurrentUser().getUid() + "miniBudget";
    public static final String GOAL_TO_PASS_FROM_DASH_TO_ACTIONS = "goalToPass";
    public static final String PROGRESS_TO_PASS_FROM_DASH_TO_ACTIONS = "progressToPass";

    private FirestoreRecyclerAdapter<Envelope, MyViewHolder> adapter; //Firebase UI Firestore Adapter
    TextView totalAmountView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Overview");
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rv_envelopes);
        totalAmountView = findViewById(R.id.total_amount_tv);
        totalAmountView.setText("0.00");

        //Toast.makeText(DashBoardActivity.this, MINI_BUDGETS_NAME_LOCATION, Toast.LENGTH_SHORT).show();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(DashBoardActivity.this, 2);
        rv.setLayoutManager(gridLayoutManager);
        //attempting to add realtime data retriveal
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        Query query = rootRef.collection(MINI_BUDGETS_NAME_LOCATION);

        FirestoreRecyclerOptions<Envelope> options = new FirestoreRecyclerOptions.Builder<Envelope>()
                .setQuery(query, Envelope.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<Envelope, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final Envelope model) {
                holder.setMiniBudgetItem(model.getCategory(), model.getAmount(), model.getProgress());
                holder.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(DashBoardActivity.this, EnvelopeActions.class);
                        intent.putExtra(CATEGORY_TO_UPDATE, model.getCategory());
                        intent.putExtra(AMOUNT_TO_UPDATE, model.getAmount());
                        intent.putExtra(GOAL_TO_PASS_FROM_DASH_TO_ACTIONS, model.getGoal());
                        intent.putExtra(PROGRESS_TO_PASS_FROM_DASH_TO_ACTIONS, model.getProgress());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.envelope_rv, parent, false);
                return new MyViewHolder(view);
            }
        };

        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEnvelopDialogFragment addEnvelopDialogFragment = new AddEnvelopDialogFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                addEnvelopDialogFragment.show(fragmentManager, "TAG");
            }
        });

        CardView overallAmountView = findViewById(R.id.total_amount_view_dash_cardView);
        overallAmountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashBoardActivity.this, TransactionActivity.class));
            }
        });
    }

    /**
     * @param categoryName is the result of the addEnvelopeDialogFragment
     *                     which is then saved to Firestore database
     */
    @Override
    public void addNewEnvelope(String categoryName, String goal) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(MINI_BUDGETS_NAME_LOCATION).document(categoryName).set(new Envelope(categoryName, "0.00", goal));
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private CardView parent;
        ProgressBar progressBar;
        TextView categoryTitleView, amountView;

        MyViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            parent = view.findViewById(R.id.parent_rv_envelope_cardView);

        }

        void setMiniBudgetItem(String category, String amount, int progress) {
            categoryTitleView = view.findViewById(R.id.category_tv);
            categoryTitleView.setText(category);

            amountView = view.findViewById(R.id.amount_tv);
            amountView.setText(amount);

            progressBar = view.findViewById(R.id.progressBar);
            progressBar.setMax(100);
            progressBar.setProgress(progress);


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening(); //start listening Firestore db to fill recycleView
        totalAmountView.setText("0.00");
        updateTotalAmountView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();//stop listening to Firestore db
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void updateTotalAmountView() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(MINI_BUDGETS_NAME_LOCATION)
                .whereGreaterThan("amount", "0.00")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Envelope temp = document.toObject(Envelope.class);
                                String oldTotal = MoneyManager.FormatMoney(totalAmountView.getText().toString());
                                String amountToAdd = MoneyManager.FormatMoney(temp.getAmount());


                                String newTotal = MoneyManager.add(oldTotal,amountToAdd);
                                totalAmountView.setText("" + newTotal);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashBoardActivity.this, LoginActivity.class));
                finish();
                return true;
            case R.id.settings_menu:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
