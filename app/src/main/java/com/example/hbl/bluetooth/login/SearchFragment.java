package com.example.hbl.bluetooth.login;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hbl.bluetooth.BaseFragment;
import com.example.hbl.bluetooth.R;
import com.example.hbl.bluetooth.home.HomeActivity;
import com.example.hbl.bluetooth.network.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by hbl on 2017/9/2.
 */

public class SearchFragment extends BaseFragment {

    @BindView(R.id.imBack)
    ImageView imBack;
    @BindView(R.id.imBlu)
    ImageView imBlu;
    @BindView(R.id.imBleCenter)
    ImageView imBleCenter;
    @BindView(R.id.tvBluState)
    TextView tvBluState;
    @BindView(R.id.btnSearch)
    Button btnSearch;
    @BindView(R.id.tvUp)
    TextView tvUp;
    @BindView(R.id.tvDown)
    TextView tvDown;
    @BindView(R.id.btnMach)
    Button btnMach;
    @BindView(R.id.machView)
    FrameLayout machView;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity activity;
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    public static final String TAG = SearchFragment.class.getSimpleName();
    private RotateAnimation animation;


    @Override
    protected int getLayoutId() {
        return R.layout.search_fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
        checkBluetooth();
        initAnimation();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            imBlu.setImageResource(R.drawable.search_ble_round);
            imBleCenter.setVisibility(View.VISIBLE);
        }

    }

    private void initAnimation() {
        animation=new RotateAnimation(360,0, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            activity.finish();
            return;
        }
        imBlu.setImageResource(R.drawable.search_ble_round);
        imBleCenter.setVisibility(View.VISIBLE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btnSearch)
    public void onClick() {
        scanLeDevice(true);
    }

    @OnClick(R.id.imBack)
    public void onBackClick() {
        activity.finish();
        imBlu.clearAnimation();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            btnSearch.setEnabled(true);
            imBlu.clearAnimation();
        }
    };
    private void scanLeDevice(boolean enable) {
        mHandler.removeCallbacks(runnable);
        if (enable) {
            mHandler.postDelayed(runnable, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            btnSearch.setEnabled(false);
            imBlu.startAnimation(animation);
            return;
        }
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        imBlu.clearAnimation();
        btnSearch.setEnabled(true);
    }

    private void checkBluetooth() {
        if (!activity.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.show(activity, getResources().getString(R.string.ble_not_supported));
            activity.finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtil.show(activity, getResources().getString(R.string.ble_not_supported));
            activity.finish();
            return;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        scanLeDevice(false);
    }

    private ArrayMap<String, String> map = new ArrayMap<>();
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: " + device.getName() + "-> " + device.getAddress());
                    String name = device.getName();
                    if ("hotup".equals(name)) {
                        if (map.get("hotup") == null) {
                            map.put("hotup", device.getAddress());
                        }
                    }
                    if ("hotdw".equals(name)) {
                        if ((map.get("hotdw") == null)) {
                            map.put("hotdw", device.getAddress());
                        }
                    }
                    if (map.get("hotup") != null) {
                        tvUp.setVisibility(View.VISIBLE);
                        machView.setVisibility(View.VISIBLE);
                    }
                    if (map.get("hotdw") != null) {
                        tvDown.setVisibility(View.VISIBLE);
                        machView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

    @OnClick({R.id.btnMach, R.id.machView})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMach:
                scanLeDevice(false);
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.putExtra("hotup", map.get("hotup"));
                intent.putExtra("hotdw", map.get("hotdw"));
                mHandler.removeCallbacks(runnable);
                imBlu.clearAnimation();
                startActivity(intent);
                activity.finish();
                break;
            case R.id.machView:
                machView.setVisibility(View.GONE);
                scanLeDevice(false);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(runnable);
    }
}
