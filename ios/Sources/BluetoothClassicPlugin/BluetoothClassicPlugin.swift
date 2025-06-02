import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BluetoothClassicPlugin)
public class BluetoothClassicPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "BluetoothClassicPlugin"
    public let jsName = "BluetoothClassic"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = BluetoothClassic()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func scan(_ call: CAPPluginCall) {
        call.resolve(["devices": []])
    }

    @objc func pair(_ call: CAPPluginCall) {
        call.resolve()
    }

    @objc func connect(_ call: CAPPluginCall) {
        call.resolve()
    }

    @objc func disconnect(_ call: CAPPluginCall) {
        call.resolve()
    }

    @objc func read(_ call: CAPPluginCall) {
        call.resolve(["data": []])
    }

    @objc func readUntil(_ call: CAPPluginCall) {
        call.resolve(["data": []])
    }

    @objc func write(_ call: CAPPluginCall) {
        call.resolve()
    }
}
