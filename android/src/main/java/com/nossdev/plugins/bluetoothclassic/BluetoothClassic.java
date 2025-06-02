package com.nossdev.plugins.bluetoothclassic;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.RequiresPermission;
import com.getcapacitor.Bridge;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BluetoothClassic {

    /**
     * The standard reserved UUID for classic bluetooth serial port profile
     */
    private static final UUID SPP_ID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int DEFAULT_SCAN_DURATION = 5_000;
    private static final int DEFAULT_READ_TIMEOUT = 10_000;
    private final Set<BluetoothDevice> discoveredDevices = new HashSet<>();
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BroadcastReceiver receiver;

    @RequiresPermission(allOf = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT })
    public CompletableFuture<Collection<BluetoothDevice>> scan(Bridge bridge, Context context) {
        return scan(bridge, context, DEFAULT_SCAN_DURATION);
    }

    @RequiresPermission(allOf = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT })
    public CompletableFuture<Collection<BluetoothDevice>> scan(Bridge bridge, Context context, int duration) {
        CompletableFuture<Collection<BluetoothDevice>> result = new CompletableFuture<>();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            result.completeExceptionally((new IOException("Bluetooth not supported")));
            return result;
        }
        if (!adapter.isEnabled()) {
            result.completeExceptionally((new IOException("Bluetooth is not enabled")));
            return result;
        }
        adapter.cancelDiscovery();
        discoveredDevices.clear();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && device.getAddress() != null) {
                            discoveredDevices.add(device);
                        }
                    }
                }
            },
            intentFilter
        );

        adapter.startDiscovery();
        bridge
            .getActivity()
            .getWindow()
            .getDecorView()
            .postDelayed(
                () -> {
                    adapter.cancelDiscovery();
                    try {
                        context.unregisterReceiver(receiver);
                    } catch (IllegalArgumentException ignore) {}
                    result.complete(discoveredDevices);
                },
                duration
            );
        return result;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void pair(String address) throws IOException {
        if (!BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).createBond()) {
            throw new IOException("Bonding sequence not started");
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect(Context context, String address) throws IOException {
        try {
            close(context);
        } catch (IOException e) {
            throw new IOException("Unable to close before establishing a new connection.", e);
        }

        BluetoothDevice target = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        socket = target.createRfcommSocketToServiceRecord(SPP_ID);
        socket.connect();
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public void write(byte[] data) throws IOException {
        outputStream.write(data);
    }

    public CompletableFuture<byte[]> read() {
        CompletableFuture<byte[]> result = new CompletableFuture<>();
        new Thread(() -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                while (true) {
                    int b = inputStream.read(); // blocks until data or error
                    if (b == -1) break;

                    buffer.write((byte) b);
                }
                result.complete(buffer.toByteArray());
            } catch (IOException e) {
                result.completeExceptionally(e);
            }
        }).start();

        return result;
    }

    public CompletableFuture<byte[]> readUntil(byte[] delimiter) {
        return readUntil(delimiter, DEFAULT_READ_TIMEOUT);
    }

    public CompletableFuture<byte[]> readUntil(byte[] delimiter, int timeout) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        Thread readThread = new Thread(() -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int matchIndex = 0;

            try {
                while (!future.isDone()) {
                    int b = inputStream.read(); // blocks until data or error
                    if (b == -1) break;

                    byte byteRead = (byte) b;
                    buffer.write(byteRead);

                    if (byteRead == delimiter[matchIndex]) {
                        matchIndex++;
                        if (matchIndex == delimiter.length) {
                            // ✅ Delimiter matched
                            if (!future.isDone()) {
                                future.complete(buffer.toByteArray());
                            }
                            return;
                        }
                    } else {
                        matchIndex = 0;
                    }
                }
            } catch (IOException e) {
                if (!future.isDone()) {
                    future.completeExceptionally(e);
                }
            }
        });

        readThread.start();

        // ✅ Timeout handler (uses scheduled executor)
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(
            () -> {
                if (!future.isDone()) {
                    future.completeExceptionally(new TimeoutException("Read timed out after " + timeout + " ms"));
                    readThread.interrupt(); // Optional: interrupt the thread if it's blocking
                }
                scheduler.shutdown(); // Clean up the thread
            },
            timeout,
            TimeUnit.MILLISECONDS
        );

        return future;
    }

    public void disconnect(Context context) throws IOException {
        close(context);
    }

    private void close(Context context) throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
        if (socket != null) {
            if (socket.isConnected()) {
                socket.close();
            }
            socket = null;
        }
    }
}
