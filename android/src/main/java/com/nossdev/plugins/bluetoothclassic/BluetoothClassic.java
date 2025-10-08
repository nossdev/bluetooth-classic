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
import java.util.function.BiConsumer;

public class BluetoothClassic {

    private final Object connectionLock = new Object();

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
        Logger.info("Starting Bluetooth scan with duration: " + duration + "ms");
        CompletableFuture<Collection<BluetoothDevice>> result = new CompletableFuture<>();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Logger.error("Bluetooth not supported on this device");
            result.completeExceptionally((new IOException("Bluetooth not supported")));
            return result;
        }
        if (!adapter.isEnabled()) {
            Logger.warn("Bluetooth is not enabled");
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
                            Logger.debug("Device found: " + device.getAddress());
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
                    Logger.info("Scan completed. Found " + discoveredDevices.size() + " device(s)");
                    result.complete(discoveredDevices);
                },
                duration
            );
        return result;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void pair(String address) throws IOException {
        Logger.info("Attempting to pair with device: " + address);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Logger.error("Bluetooth adapter not available");
            throw new IOException("Bluetooth adapter not available");
        }
        BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null) {
            Logger.error("Invalid device address: " + address);
            throw new IOException("Invalid device address: " + address);
        }
        if (!device.createBond()) {
            Logger.error("Bonding sequence not started for: " + address);
            throw new IOException("Bonding sequence not started");
        }
        Logger.debug("Bonding initiated for device: " + address);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect(Context context, String address) throws IOException {
        Logger.info("Connecting to device: " + address);
        synchronized (connectionLock) {
            try {
                close(context);
            } catch (IOException e) {
                Logger.error("Failed to close existing connection", e);
                throw new IOException("Unable to close before establishing a new connection.", e);
            }

            BluetoothDevice target = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            socket = target.createRfcommSocketToServiceRecord(SPP_ID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            Logger.info("Successfully connected to device: " + address);
        }
    }

    public void write(byte[] data) throws IOException {
        synchronized (connectionLock) {
            if (outputStream == null) {
                Logger.error("Write failed: not connected");
                throw new IOException("Not connected - outputStream is null");
            }
            Logger.debug("Writing " + data.length + " bytes");
            outputStream.write(data);
        }
    }

    public CompletableFuture<byte[]> read() {
        return read(DEFAULT_READ_TIMEOUT);
    }

    public CompletableFuture<byte[]> read(int timeout) {
        Logger.debug("Starting read with timeout: " + timeout + "ms");
        CompletableFuture<byte[]> result = new CompletableFuture<>();
        Thread readThread = new Thread(() -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                if (inputStream == null) {
                    Logger.error("Read failed: not connected");
                    result.completeExceptionally(new IOException("Not connected - inputStream is null"));
                    return;
                }
                while (!result.isDone()) {
                    int b = inputStream.read(); // blocks until data or error
                    if (b == -1) break;

                    buffer.write((byte) b);
                }
                if (!result.isDone()) {
                    Logger.debug("Read completed: " + buffer.size() + " bytes");
                    result.complete(buffer.toByteArray());
                }
            } catch (IOException e) {
                if (!result.isDone()) {
                    Logger.error("Read error", e);
                    result.completeExceptionally(e);
                }
            }
        });
        waitUntil(timeout, readThread, result);
        return result;
    }

    public CompletableFuture<byte[]> readUntil(byte[] delimiter) {
        return readUntil(delimiter, DEFAULT_READ_TIMEOUT);
    }

    public CompletableFuture<byte[]> readUntil(byte[] delimiter, int timeout) {
        Logger.debug("Starting readUntil with delimiter length: " + delimiter.length + ", timeout: " + timeout + "ms");
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        Thread readThread = new Thread(() -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int matchIndex = 0;
            try {
                if (inputStream == null) {
                    Logger.error("ReadUntil failed: not connected");
                    future.completeExceptionally(new IOException("Not connected - inputStream is null"));
                    return;
                }
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
                                Logger.debug("Delimiter matched. Read completed: " + buffer.size() + " bytes");
                                future.complete(buffer.toByteArray());
                            }
                            return;
                        }
                    } else {
                        // Check if current byte could start a new match
                        matchIndex = (byteRead == delimiter[0]) ? 1 : 0;
                    }
                }
                if (!future.isDone()) {
                    Logger.debug("ReadUntil completed without delimiter match: " + buffer.size() + " bytes");
                    future.complete(buffer.toByteArray());
                }
            } catch (IOException e) {
                if (!future.isDone()) {
                    Logger.error("ReadUntil error", e);
                    future.completeExceptionally(e);
                }
            }
        });

        // ✅ Timeout handler (uses scheduled executor)
        waitUntil(timeout, readThread, future);
        return future;
    }

    private void waitUntil(int timeout, Thread thread, CompletableFuture<?> future) {
        thread.start();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(
            () -> {
                if (!future.isDone()) {
                    Logger.warn("Operation timed out after " + timeout + "ms");
                    future.completeExceptionally(new TimeoutException("Timed out after " + timeout + " ms"));
                    thread.interrupt();
                }
            },
            timeout,
            TimeUnit.MILLISECONDS
        );
    }

    public boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean enable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        if (!adapter.isEnabled()) {
            return adapter.enable();
        }
        return true;
    }

    public void disconnect(Context context) throws IOException {
        Logger.info("Disconnecting");
        close(context);
    }

    private void close(Context context) throws IOException {
        synchronized (connectionLock) {
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
            Logger.debug("Connection closed");
        }
    }
}
