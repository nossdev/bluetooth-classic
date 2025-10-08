package com.nossdev.plugins.bluetoothclassic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginResult;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import androidx.activity.result.ActivityResult;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CapacitorPlugin(
    name = "BluetoothClassic",
    permissions = {
        @Permission(
            alias = "bluetooth",
            strings = {
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION
            }
        )
    }
)
public class BluetoothClassicPlugin extends Plugin {

    private static final Map<Integer, String> BLUETOOTH_STATES = new HashMap<>();

    static {
        BLUETOOTH_STATES.put(BluetoothAdapter.STATE_ON, "on");
        BLUETOOTH_STATES.put(BluetoothAdapter.STATE_TURNING_ON, "turning_on");
        BLUETOOTH_STATES.put(BluetoothAdapter.STATE_OFF, "off");
        BLUETOOTH_STATES.put(BluetoothAdapter.STATE_TURNING_OFF, "turning_off");
    }

    private BroadcastReceiver bluetoothReceiver;
    private boolean isReceiverRegistered;

    private final BluetoothClassic implementation = new BluetoothClassic();

    @Override
    public void load() {
        super.load();
        Logger.info("Plugin loading");
        initialize();
        registerBluetoothStateReceiver();
    }

    private void initialize() {
        this.bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    Optional.ofNullable(
                        BLUETOOTH_STATES.get(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
                    ).ifPresent(state -> {
                        Logger.info("Bluetooth state changed: " + state);
                        JSObject result = new JSObject().put("value", state);
                        notifyListeners("bluetoothState", result);
                        notifyListeners(state, result);
                    });
                }
            }
        };
    }

    private void registerBluetoothStateReceiver() {
        if (!isReceiverRegistered) {
            Logger.debug("Registering Bluetooth state receiver");
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getContext().registerReceiver(bluetoothReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    private void unRegisterBluetoothStateReceiver() {
        if (isReceiverRegistered) {
            Logger.debug("Unregistering Bluetooth state receiver");
            getContext().unregisterReceiver(bluetoothReceiver);
            isReceiverRegistered = false;
        }
    }

    @RequiresPermission(allOf = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT })
    @PluginMethod
    public void scan(PluginCall call) {
        CompletedFuture.from(
            Optional.ofNullable(call.getInt("duration"))
                .map(duration -> implementation.scan(getBridge(), getContext(), duration))
                .orElseGet(() -> implementation.scan(getBridge(), getContext()))
                .thenApply(devices -> new JSObject().put("devices", toJSArrayDevices(devices)))
        )
            .onSuccess(call::resolve)
            .onError(e -> call.reject(e.getMessage()))
            .await();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @PluginMethod
    public void pair(PluginCall call) {
        String theAddress = call.getString("address", "");
        Optional<String> address = Optional.ofNullable(theAddress).map(String::trim);
        if (address.isEmpty()) {
            call.reject("Invalid device address");
            return;
        }

        try {
            implementation.pair(address.get());
            call.resolve();
        } catch (IOException e) {
            call.reject(e.getMessage());
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @PluginMethod
    public void connect(PluginCall call) {
        String theAddress = call.getString("address", "");
        Optional<String> address = Optional.ofNullable(theAddress).map(String::trim);
        if (address.isEmpty()) {
            call.reject("Invalid device address");
            return;
        }

        try {
            implementation.connect(getContext(), address.get());
            call.resolve();
        } catch (IOException e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void write(PluginCall call) {
        JSArray data = call.getArray("data", new JSArray());
        if (data == null || data.length() == 0) {
            call.reject("Attempted to write empty data");
            return;
        }
        try {
            implementation.write(getBytes(data));
            call.resolve();
        } catch (IOException e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void read(PluginCall call) {
        CompletableFuture<byte[]> result = Optional.ofNullable(call.getInt("timeout"))
            .map(implementation::read)
            .orElseGet(implementation::read);
        CompletedFuture.from(result.thenApply(bytes -> new JSObject().put("data", toJSByteArray(bytes))))
            .onSuccess(call::resolve)
            .onError(e -> call.reject(e.getMessage()))
            .await();
    }

    @PluginMethod
    public void readUntil(PluginCall call) {
        JSArray delimiterData = call.getArray("delimiter", new JSArray());
        if (delimiterData == null || delimiterData.length() == 0) {
            call.reject("Invalid delimiter");
            return;
        }

        byte[] delimiter = getBytes(delimiterData);
        CompletableFuture<byte[]> result = Optional.ofNullable(call.getInt("timeout"))
            .map(timeout -> implementation.readUntil(delimiter, timeout))
            .orElseGet(() -> implementation.readUntil(delimiter));
        CompletedFuture.from(result.thenApply(bytes -> new JSObject().put("data", toJSByteArray(bytes))))
            .onSuccess(call::resolve)
            .onError(e -> call.reject(e.getMessage()))
            .await();
    }

    @PluginMethod
    public void isEnabled(PluginCall call) {
        boolean enabled = implementation.isEnabled();
        call.resolve(new JSObject().put("enabled", enabled));
    }

    @PluginMethod
    public void enable(PluginCall call) {
        Logger.info("Enable Bluetooth requested");
        if (implementation.isEnabled()) {
            Logger.debug("Bluetooth already enabled");
            call.resolve(new JSObject().put("enabled", true));
            return;
        }

        // Use Intent to request Bluetooth enable (required for API 33+)
        Logger.debug("Launching Bluetooth enable intent");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(call, enableBtIntent, "handleEnableResult");
    }

    @ActivityCallback
    private void handleEnableResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        boolean enabled = implementation.isEnabled();
        Logger.info("Enable Bluetooth result: " + enabled);
        call.resolve(new JSObject().put("enabled", enabled));
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        try {
            implementation.disconnect(getContext());
        } catch (IOException e) {
            call.reject(e.getMessage());
            return;
        }
        call.resolve();
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        Logger.debug("Checking Bluetooth permissions");
        String status;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            boolean scanGranted =
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
            boolean connectGranted =
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED;

            status = (scanGranted && connectGranted) ? "granted" : "denied";
        } else {
            // Android 11 and below
            boolean locationGranted =
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;

            status = locationGranted ? "granted" : "denied";
        }

        Logger.info("Permission status: " + status);
        call.resolve(new JSObject().put("status", status));
    }

    @Override
    protected void handleOnDestroy() {
        Logger.info("Plugin destroying");
        try {
            unRegisterBluetoothStateReceiver();
            implementation.disconnect(getContext());
        } catch (Exception e) {
            Logger.error("Error during plugin destruction", e);
        }
        super.handleOnDestroy();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private JSArray toJSArrayDevices(Collection<BluetoothDevice> devices) {
        JSArray result = new JSArray();
        for (BluetoothDevice device : devices) {
            result.put(toJSONDevice(device));
        }
        return result;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private JSObject toJSONDevice(BluetoothDevice device) {
        return new JSObject()
            .put("name", device.getName())
            .put("type", getDeviceType(device))
            .put("address", device.getAddress())
            .put("addressType", getAddressType(device))
            .put("state", getBondState(device));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private String getDeviceType(BluetoothDevice device) {
        return switch (device.getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC -> "classic";
            case BluetoothDevice.DEVICE_TYPE_LE -> "le";
            case BluetoothDevice.DEVICE_TYPE_DUAL -> "dual";
            default -> "unknown";
        };
    }

    @SuppressLint("SwitchIntDef")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private String getAddressType(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return switch (device.getAddressType()) {
                case BluetoothDevice.ADDRESS_TYPE_PUBLIC -> "public";
                case BluetoothDevice.ADDRESS_TYPE_RANDOM -> "random";
                case BluetoothDevice.ADDRESS_TYPE_ANONYMOUS -> "anonymous";
                default -> "unknown";
            };
        }
        return "unknown";
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private String getBondState(BluetoothDevice device) {
        return switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED -> "bonded";
            case BluetoothDevice.BOND_BONDING -> "bonding";
            case BluetoothDevice.BOND_NONE -> "none";
            default -> "unknown";
        };
    }

    private byte[] getBytes(JSArray data) {
        byte[] bytes = new byte[data.length()];
        for (int i = 0; i < data.length(); i++) {
            bytes[i] = (byte) data.optInt(i);
        }
        return bytes;
    }

    private JSArray toJSByteArray(byte[] bytes) {
        JSArray data = new JSArray();
        for (byte theByte : bytes) {
            data.put(((int) theByte) & 0xFF);
        }
        return data;
    }
}
