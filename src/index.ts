import { registerPlugin } from '@capacitor/core';

import type {
  BluetoothClassicPlugin,
  BluetoothClassicInterface,
  ConnectOptions,
  PairOptions,
  ReadResult,
  ReadUntilOptions,
  ScanOptions,
  ScanResult,
  WriteOptions,
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
  read(options: ReadUntilOptions): Promise<ReadResult>;
  read(options?: ReadUntilOptions): Promise<ReadResult> {
    if (options?.delimiter?.length) {
      return this.plugin.readUntil(options);
    }
    return this.plugin.read();
  }

  disconnect(): Promise<void> {
    return this.plugin.disconnect();
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
  ReadUntilOptions,
  ReadResult,
  BluetoothClassicInterface as BluetoothClassicPlugin,
} from './definitions';

export { theInstance as BluetoothClassic };
