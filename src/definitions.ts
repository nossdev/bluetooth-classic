export interface BluetoothClassicPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
