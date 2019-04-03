package com.codegud.financeapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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

import java.util.Calendar;

public class DashBoardActivity extends AppCompatActivity implements AddCategoryListener {

    private static final String TAG = "TAD";

    public static final String CATEGORY_TO_UPDATE = "categoryToUpdate";
    public static final String AMOUNT_TO_UPDATE = "amountToUpdate";
    public static final String GOAL_TO_PASS_FROM_DASH_TO_ACTIONS = "goalToPass";
    public static final String PROGRESS_TO_PASS_FROM_DASH_TO_ACTIONS = "progressToPass";
    private static final String CHANNEL_ID = "Default";

    private FirestoreRecyclerAdapter<Envelope, MyViewHolder> adapter; //Firebase UI Firestore Adapter
    TextView totalAmountView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        createNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Overview");
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rv_envelopes);
        totalAmountView = findViewById(R.id.total_amount_tv);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(DashBoardActivity.this, 2);
        rv.setLayoutManager(gridLayoutManager);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = rootRef.collection(userID).document("BudgetInfo").collection("budgets");

        FirestoreRecyclerOptions<Envelope> options = new FirestoreRecyclerOptions.Builder<Envelope>()
                .setQuery(query, Envelope.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Envelope, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull final Envelope model) {
                holder.setMiniBudgetItem(model.getCategory(), model.getAmount(), model.getProgress());
                holder.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(DashBoardActivity.this, EnvelopeActions.class);
                        intent.putExtra(CATEGORY_TO_UPDATE, model.getCategory());
                        intent.putExtra(AMOUNT_TO_UPDATE, model.getAmount());
                        intent.putExtra(GOAL_TO_PASS_FROM_DASH_TO_ACTIONS, model.getGoal());
                        intent.putExtra(PROGRESS_TO_PASS_FROM_DASH_TO_ACTIONS, model.getProgress());

                        Pair<View, String> p1 = Pair.create((View) holder.parent, "cardTotalAmount");

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(DashBoardActivity.this, p1);
                        startActivity(intent, options.toBundle());
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

                //scheduleNotification(createNotification(), 5000);
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

    @Override
    public void addNewEnvelope(String categoryName, String goal) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(userID).document("BudgetInfo").collection("budgets").document(categoryName).set(new Envelope(categoryName, "0.00", goal));
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
            amountView.setText(MoneyManager.formatMoneyForDisplay(amount));

            progressBar = view.findViewById(R.id.progressBar);
            if (progress >= 100) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int color = ContextCompat.getColor(DashBoardActivity.this, R.color.progress_goal_met);
                        progressBar.setProgressTintList(ColorStateList.valueOf(color));
                    }
                }
            } else if (progress < 100) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int color = ContextCompat.getColor(DashBoardActivity.this, R.color.progress_goal_not_yet_met);
                        progressBar.setProgressTintList(ColorStateList.valueOf(color));
                    }
                }
            }

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
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(userID).document("BudgetInfo").collection("budgets")
                .whereGreaterThan("amount", "0.00")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Envelope temp = document.toObject(Envelope.class);
                                String oldTotal = MoneyManager.formatMoneyForCalculations(totalAmountView.getText().toString());
                                String amountToAdd = MoneyManager.formatMoneyForCalculations(temp.getAmount());

                                String newTotal = MoneyManager.add(oldTotal, amountToAdd);
                                totalAmountView.setText(MoneyManager.formatMoneyForDisplay(newTotal));
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
            case R.id.notificaiton_menu:


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void showNotification(String title, String content) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, createNotification());


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "These are notifications for my build a budget app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "BuildABudgetNotifcations", importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification createNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle("Update your Budget")
                .setContentText("Click to open BuildABudget")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)// Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);//tap will clear notification

        return builder.build();
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        long futureInMillis = SystemClock.elapsedRealtime() + delay;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 22);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

    }
}
