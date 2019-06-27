package ydkim2110.com.androidbarberstaffapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import retrofit2.Retrofit;
import ydkim2110.com.androidbarberstaffapp.Adapter.MyConfirmShoppingItemAdapter;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Model.BarberServices;
import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;
import ydkim2110.com.androidbarberstaffapp.R;
import ydkim2110.com.androidbarberstaffapp.Retrofit.IFCMService;
import ydkim2110.com.androidbarberstaffapp.Retrofit.RetrofitClient;

public class TotalPriceFragment extends BottomSheetDialogFragment {

    private static final String TAG = TotalPriceFragment.class.getSimpleName();

    private Unbinder mUnbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    private HashSet<BarberServices> mServicesAdded;
    private List<ShoppingItem> mShoppingItemList;
    private IFCMService mIFCMService;
    private AlertDialog mDialog;

    private static TotalPriceFragment instance;

    public static TotalPriceFragment getInstance() {
        return instance == null ? new TotalPriceFragment() : instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        mIFCMService = RetrofitClient.getInstance().create(IFCMService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_price, container, false);

        mUnbinder = ButterKnife.bind(this, view);
        
        init();
        initView();
        
        getBundle(getArguments());

        setInformation();
        
        return view;
    }

    private void setInformation() {
        Log.d(TAG, "setInformation: called!!");
        txt_salon_name.setText(Common.selected_salon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if (mServicesAdded.size() > 0) {
            // Add to Chip Group
            int i=0;
            for (BarberServices services : mServicesAdded) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mServicesAdded.remove(v.getTag());
                        chip_group_services.removeView(v);
                        
                        calculatePrice();
                    }
                });

                chip_group_services.addView(chip);

                i++;
            }
        }

        if (mShoppingItemList.size() > 0) {
            MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(), mShoppingItemList);
            recycler_view_shopping.setAdapter(adapter);
        }

        calculatePrice();

    }

    private void calculatePrice() {
        Log.d(TAG, "calculatePrice: called!!");
        double price = Common.DEFAULT_PRICE;
        for (BarberServices services : mServicesAdded) {
            price += services.getPrice();
        }
        for (ShoppingItem shoppingItem : mShoppingItemList) {
            price += shoppingItem.getPrice();
        }
        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN).append(price));
    }

    private void getBundle(Bundle arguments) {
        Log.d(TAG, "getBundle: called!!");
        this.mServicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

        this.mShoppingItemList = new Gson()
                .fromJson(arguments.getString(Common.SHOPPING_LIST),
                        new TypeToken<List<ShoppingItem>>(){}.getType());
    }

    private void initView() {
        Log.d(TAG, "initView: called!");
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
    }
}
