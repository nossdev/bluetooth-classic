/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';

import type {
  BluetoothClassicPlugin,
  ConnectOptions,
  PairOptions,
  ReadOptions,
  ReadResult,
  ReadUntilOptions,
  ScanOptions,
  ScanResult,
  WriteOptions,
  PermissionStatus,
} from './definitions';

export class BluetoothClassicWeb extends WebPlugin implements BluetoothClassicPlugin {
  scan(_options?: ScanOptions): Promise<ScanResult> {
    throw new Error('Method not implemented.');
  }
  pair(_options: PairOptions): Promise<void> {
    throw new Error('Method not implemented.');
  }
  connect(_options: ConnectOptions): Promise<void> {
    throw new Error('Method not implemented.');
  }
  write(_options: WriteOptions): Promise<void> {
    throw new Error('Method not implemented.');
  }
  read(_options?: ReadOptions): Promise<ReadResult> {
    throw new Error('Method not implemented.');
  }
  readUntil(_options: ReadUntilOptions): Promise<ReadResult> {
    throw new Error('Method not implemented.');
  }
  isEnabled(): Promise<{ enabled: boolean }> {
    throw new Error('Method not implemented.');
  }
  enable(): Promise<{ enabled: boolean }> {
    throw new Error('Method not implemented.');
  }
  disconnect(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  checkPermissions(): Promise<PermissionStatus> {
    throw new Error('Method not implemented.');
  }
  requestPermissions(): Promise<PermissionStatus> {
    throw new Error('Method not implemented.');
  }
}
