import Foundation

@objc public class BluetoothClassic: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
