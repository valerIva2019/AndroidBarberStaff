package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyStateAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.SpacesItemDecoration;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnAllStateLoadListener;
import ydkim2110.com.androidbarberstaffapp.Model.City;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    CollectionReference allSalonCollection;

    IOnAllStateLoadListener mIOnAllStateLoadListener;

    MyStateAdapter mAdapter;
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        initView();

        init();

        loadAllStateFromFirestore();
    }

    private void loadAllStateFromFirestore() {
        Log.d(TAG, "loadAllStateFromFirestore: called!!");

        mDialog.show();

        allSalonCollection.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIOnAllStateLoadListener.onAllStateFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<City> cities = new ArrayList<>();
                            for (DocumentSnapshot citySnapshot : task.getResult()) {
                                City city = citySnapshot.toObject(City.class);
                                cities.add(city);
                            }
                            mIOnAllStateLoadListener.onAllStateLoadSuccess(cities);
                        }
                    }
                });
    }

    private void init() {
        Log.d(TAG, "init: called!!");

        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        mIOnAllStateLoadListener = this;
        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build();
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");

        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllStateLoadSuccess(List<City> cityList) {
        Log.d(TAG, "onAllStateLoadSuccess: called!!");

        mAdapter = new MyStateAdapter(this, cityList);
        recycler_state.setAdapter(mAdapter);

        mDialog.dismiss();
    }

    @Override
    public void onAllStateFailed(String message) {
        Log.d(TAG, "onAllStateFailed: called!!");

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }
}
