import type { PluginListenerHandle } from '@capacitor/core';

export type DeviceType = 'classic' | 'le' | 'dual' | 'unknown';

export type DeviceState = 'none' | 'bonded' | 'bonding' | 'unknown';

export type DeviceAddressType = 'public' | 'random' | 'anonymous' | 'unknown';

export type BluetoothState = 'on' | 'off' | 'turning_on' | 'turning_off';

export interface BluetoothDevice {
  name: string;
  type: DeviceType;
  state: DeviceState;
  address: string;
  addressType: DeviceAddressType;
}

export interface ScanOptions {
  duration: number;
}

export interface ScanResult {
  devices: BluetoothDevice[];
}

export interface PairOptions {
  address: string;
}

export interface ConnectOptions {
  address: string;
}

export interface WriteOptions {
  data: number[];
}

export interface ReadUntilOptions {
  delimiter: number[];
}

export interface ReadOptions extends ReadUntilOptions {
  timeout?: number;
}

export interface ReadResult {
  data: number[];
}

export type BluetoothStateEvent = { value: BluetoothState };

export interface PermissionStatus {
  status: 'granted' | 'denied' | 'prompt';
}

export interface BluetoothClassicPlugin {
  scan(options?: ScanOptions): Promise<ScanResult>;
  pair(options: PairOptions): Promise<void>;
  connect(options: ConnectOptions): Promise<void>;
  write(options: WriteOptions): Promise<void>;
  read(options?: ReadOptions): Promise<ReadResult>;
  readUntil(options: ReadUntilOptions): Promise<ReadResult>;
  disconnect(): Promise<void>;
  isEnabled(): Promise<{ enabled: boolean }>;
  enable(): Promise<{ enabled: boolean }>;
  addListener(
    eventName: BluetoothState | 'bluetoothState',
    listenerFunc: (data: BluetoothStateEvent) => void,
  ): Promise<PluginListenerHandle>;
  removeAllListeners(): Promise<void>;
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
}

export interface BluetoothClassicInterface {
  scan(): Promise<ScanResult>;
  scan(options: ScanOptions): Promise<ScanResult>;
  pair(options: PairOptions): Promise<void>;
  connect(options: ConnectOptions): Promise<void>;
  write(options: WriteOptions): Promise<void>;
  read(): Promise<ReadResult>;
  read(options: ReadOptions): Promise<ReadResult>;
  disconnect(): Promise<void>;
  isEnabled(): Promise<{ enabled: boolean }>;
  enable(): Promise<{ enabled: boolean }>;
  on(eventName: BluetoothState, listenerFunc: (data: BluetoothStateEvent) => void): Promise<PluginListenerHandle>;
  on(listenerFunc: (data: BluetoothStateEvent) => void): Promise<PluginListenerHandle>;
  removeListeners(): Promise<void>;
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
}
