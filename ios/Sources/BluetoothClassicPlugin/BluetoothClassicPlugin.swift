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
    private var centralManager: CBCentralManager?

    /// Creates CBCentralManager lazily on first use to avoid triggering
    /// the iOS Bluetooth permission/power dialogs at app startup.
    private func ensureCentralManager(showPowerAlert: Bool = false) {
        guard centralManager == nil else { return }
        centralManager = CBCentralManager(
            delegate: self,
            queue: DispatchQueue.global(qos: .background),
            options: [CBCentralManagerOptionShowPowerAlertKey: showPowerAlert]
        )
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

    @objc override public func addListener(_ call: CAPPluginCall) {
        ensureCentralManager()
        super.addListener(call)
    }

    @objc func isEnabled(_ call: CAPPluginCall) {
        ensureCentralManager()
        let enabled = centralManager?.state == .poweredOn
        call.resolve(["enabled": enabled])
    }

    @objc func enable(_ call: CAPPluginCall) {
        // If Bluetooth is already on, no alert needed.
        if let manager = centralManager, manager.state == .poweredOn {
            call.resolve(["enabled": true])
            return
        }

        // iOS does not allow apps to programmatically enable Bluetooth.
        // Recreate CBCentralManager with showPowerAlert: true so the system
        // "Turn On Bluetooth" dialog appears when Bluetooth is off.
        centralManager?.delegate = nil
        centralManager = nil
        centralManager = CBCentralManager(
            delegate: self,
            queue: DispatchQueue.global(qos: .background),
            options: [CBCentralManagerOptionShowPowerAlertKey: true]
        )

        // Freshly created manager starts in .unknown state;
        // the actual state arrives via centralManagerDidUpdateState.
        call.resolve(["enabled": false])
    }

    @objc func disconnect(_ call: CAPPluginCall) {
        call.reject("Classic Bluetooth is not supported on iOS")
    }

    @objc override public func checkPermissions(_ call: CAPPluginCall) {
        // Classic Bluetooth doesn't need permissions on iOS
        // Always return granted since it's not supported anyway
        call.resolve(["status": "granted"])
    }

    @objc override public func requestPermissions(_ call: CAPPluginCall) {
        // Classic Bluetooth doesn't need permissions on iOS
        // Always return granted since it's not supported anyway
        call.resolve(["status": "granted"])
    }

    // MARK: - CBCentralManagerDelegate

    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        // Ignore callbacks from a stale manager instance (e.g. after enable() recreates it).
        guard central === centralManager else { return }

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

}
