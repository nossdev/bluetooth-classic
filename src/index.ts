import type { PluginListenerHandle } from '@capacitor/core';
import { registerPlugin } from '@capacitor/core';

import type {
  BluetoothClassicPlugin,
  BluetoothClassicInterface,
  ConnectOptions,
  PairOptions,
  ReadResult,
  ReadOptions,
  ReadUntilOptions,
  ScanOptions,
  ScanResult,
  WriteOptions,
  BluetoothStateEvent,
  BluetoothState,
  PermissionStatus,
} from './definitions';

const Plugin = registerPlugin<BluetoothClassicPlugin>('BluetoothClassic', {
  web: () => import('./web').then((m) => new m.BluetoothClassicWeb()),
});

class Instance implements BluetoothClassicInterface {
  private plugin: BluetoothClassicPlugin;

  constructor(plugin: BluetoothClassicPlugin) {
    this.plugin = plugin;
  }

  scan(): Promise<ScanResult>;
  scan(options: ScanOptions): Promise<ScanResult>;
  scan(options?: ScanOptions): Promise<ScanResult> {
    return this.plugin.scan(options);
  }

  pair(options: PairOptions): Promise<void> {
    return this.plugin.pair(options);
  }

  connect(options: ConnectOptions): Promise<void> {
    return this.plugin.connect(options);
  }

  write(options: WriteOptions): Promise<void> {
    return this.plugin.write(options);
  }

  read(): Promise<ReadResult>;
  read(options: ReadOptions): Promise<ReadResult>;
  read(options?: ReadOptions): Promise<ReadResult> {
    if (!options) return this.plugin.read();
    if (Array.isArray((options as ReadUntilOptions).delimiter)) {
      return this.plugin.readUntil(options as ReadUntilOptions);
    }
    return this.plugin.read(options);
  }

  disconnect(): Promise<void> {
    return this.plugin.disconnect();
  }

  isEnabled(): Promise<{ enabled: boolean }> {
    return this.plugin.isEnabled();
  }

  enable(): Promise<{ enabled: boolean }> {
    return this.plugin.enable();
  }

  on(eventName: BluetoothState, listenerFunc: (data: BluetoothStateEvent) => void): Promise<PluginListenerHandle>;
  on(listenerFunc: (data: BluetoothStateEvent) => void): Promise<PluginListenerHandle>;
  on(eventName: any, listenerFunc?: any) {
    if (typeof eventName === 'function') {
      return this.plugin.addListener('bluetoothState', eventName);
    }
    return this.plugin.addListener(eventName, listenerFunc);
  }

  removeListeners() {
    return this.plugin.removeAllListeners();
  }

  checkPermissions(): Promise<PermissionStatus> {
    return this.plugin.checkPermissions();
  }

  requestPermissions(): Promise<PermissionStatus> {
    return this.plugin.requestPermissions();
  }
}

const theInstance = new Instance(Plugin);

export {
  DeviceType,
  DeviceState,
  BluetoothDevice,
  ScanOptions,
  ScanResult,
  PairOptions,
  ConnectOptions,
  ReadOptions,
  ReadUntilOptions,
  ReadResult,
  BluetoothClassicInterface as BluetoothClassicPlugin,
} from './definitions';

export { theInstance as BluetoothClassic };
export default theInstance;
