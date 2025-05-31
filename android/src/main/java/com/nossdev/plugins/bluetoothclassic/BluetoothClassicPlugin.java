package com.nossdev.plugins.bluetoothclassic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import androidx.annotation.RequiresPermission;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CapacitorPlugin(
    name = "BluetoothClassic",
    permissions = {
        @Permission(alias = "Bluetooth", strings = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT })
    }
)
public class BluetoothClassicPlugin extends Plugin {

    private final BluetoothClassic implementation = new BluetoothClassic();

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
            .onError(error -> call.reject(error.getMessage()))
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
        CompletedFuture.from(implementation.read().thenApply(bytes -> new JSObject().put("data", toJSArray(bytes))))
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
        CompletedFuture.from(result.thenApply(bytes -> new JSObject().put("data", bytes)))
            .onSuccess(call::resolve)
            .onError(e -> call.reject(e.getMessage()))
            .await();
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        try {
            implementation.disconnect(getContext());
            call.resolve();
        } catch (IOException ignored) {}
    }

    @Override
    protected void handleOnDestroy() {
        try {
            implementation.disconnect(getContext());
        } catch (Exception ignored) {}
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

    private JSArray toJSArray(byte[] bytes) {
        JSArray data = new JSArray();
        for (byte theByte : bytes) {
            data.put(((int) theByte) & 0xFF);
        }
        return data;
    }
}
