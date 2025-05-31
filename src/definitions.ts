export type DeviceType = 'classic' | 'le' | 'dual' | 'unknown';

export type DeviceState = 'none' | 'bonded' | 'bonding' | 'unknown';

export type DeviceAddressType = 'public' | 'random' | 'anonymous' | 'unknown';

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
  timeout?: number;
}

export interface ReadResult {
  data: number[];
}

export interface BluetoothClassicPlugin {
  scan(options?: ScanOptions): Promise<ScanResult>;
  pair(options: PairOptions): Promise<void>;
  connect(options: ConnectOptions): Promise<void>;
  write(options: WriteOptions): Promise<void>;
  read(): Promise<ReadResult>;
  readUntil(options: ReadUntilOptions): Promise<ReadResult>;
  disconnect(): Promise<void>;
}

export interface BluetoothClassicInterface {
  scan(): Promise<ScanResult>;
  scan(options?: ScanOptions): Promise<ScanResult>;
  pair(options: PairOptions): Promise<void>;
  connect(options: ConnectOptions): Promise<void>;
  write(options: WriteOptions): Promise<void>;
  read(): Promise<ReadResult>;
  read(options: ReadUntilOptions): Promise<ReadResult>;
  disconnect(): Promise<void>;
}
