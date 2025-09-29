import Foundation
import Capacitor
import CoreBluetooth

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 *
 * Note: Classic Bluetooth is not supported on iOS.
 * This plugin monitors BLE state as a proxy for Bluetooth hardware status.
 */
@objc(BluetoothClassicPlugin)
public class BluetoothClassicPlugin: CAPPlugin, CAPBridgedPlugin, CBCentralManagerDelegate {
    public let identifier = "BluetoothClassicPlugin"
    public let jsName = "BluetoothClassic"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "scan", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pair", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "connect", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "write", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "read", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "readUntil", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isEnabled", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "enable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disconnect", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestPermissions", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = BluetoothClassic()
    private var centralManager: CBCentralManager?

    public override func load() {
        // Initialize CBCentralManager to monitor Bluetooth state
        // Using background queue to avoid blocking
        centralManager = CBCentralManager(delegate: self, queue: DispatchQueue.global(qos: .background))
    }

    @objc func scan(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func pair(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func connect(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func write(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func read(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func readUntil(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc func isEnabled(_ call: CAPPluginCall) {
        guard let manager = centralManager else {
            call.reject("Bluetooth manager not initialized")
            return
        }

        let enabled = manager.state == .poweredOn
        call.resolve(["enabled": enabled])
    }

    @objc func enable(_ call: CAPPluginCall) {
        // iOS does not allow apps to programmatically enable Bluetooth
        // However, we can trigger the system alert by checking the state
        // This will prompt the user to enable Bluetooth if it's off
        guard let manager = centralManager else {
            call.reject("Bluetooth manager not initialized")
            return
        }

        let enabled = manager.state == .poweredOn

        if !enabled {
            // Accessing the state when Bluetooth is off triggers iOS system alert
            // The alert asks user to enable Bluetooth in Settings
            _ = manager.state
        }

        call.resolve(["enabled": enabled])
    }

    @objc func disconnect(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc public override func checkPermissions(_ call: CAPPluginCall) {
        // Classic Bluetooth doesn't need permissions on iOS
        // Always return granted since it's not supported anyway
        call.resolve(["status": "granted"])
    }

    @objc public override func requestPermissions(_ call: CAPPluginCall) {
        // Classic Bluetooth doesn't need permissions on iOS
        // Always return granted since it's not supported anyway
        call.resolve(["status": "granted"])
    }

    // MARK: - CBCentralManagerDelegate

    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        var state: String

        switch central.state {
        case .poweredOn:
            state = "on"
        case .poweredOff:
            state = "off"
        case .resetting:
            state = "turning_off"
        case .unauthorized, .unsupported, .unknown:
            state = "off"
        @unknown default:
            state = "off"
        }

        // Notify listeners of Bluetooth state change
        notifyListeners("bluetoothState", data: ["value": state])
        notifyListeners(state, data: ["value": state])
    }

    deinit {
        centralManager = nil
    }
}
