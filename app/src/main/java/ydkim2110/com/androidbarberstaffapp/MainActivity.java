package ydkim2110.com.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyStateAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Common.SpacesItemDecoration;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnAllStateLoadListener;
import ydkim2110.com.androidbarberstaffapp.Model.Barber;
import ydkim2110.com.androidbarberstaffapp.Model.City;
import ydkim2110.com.androidbarberstaffapp.Model.Salon;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    private CollectionReference allSalonCollection;
    private IOnAllStateLoadListener mIOnAllStateLoadListener;
    private MyStateAdapter mAdapter;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            Common.updateToken(MainActivity.this, task.getResult().getToken());
                            Log.d(TAG, "onComplete: TOKEN: "+task.getResult().getToken());
                        }
                    }
                });

        Paper.init(this);
        String user = Paper.book().read(Common.LOGGED_KEY);

        // If user not login before
        if (TextUtils.isEmpty(user)) {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: started!!");
            ButterKnife.bind(this);

            initView();
            init();
            loadAllStateFromFirestore();
        }
        else { // If User already login
            Gson gson = new Gson();
            Common.state_name = Paper.book().read(Common.STATE_KEY);
            Common.selected_salon = gson.fromJson(Paper.book().read(Common.SALON_KEY, ""),
                    new TypeToken<Salon>(){}.getType());
            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY, ""),
                    new TypeToken<Barber>(){}.getType());

            Intent intent = new Intent(this, StaffHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }


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

    private void initView() {
        Log.d(TAG, "initView: called!!");
        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(8));
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
