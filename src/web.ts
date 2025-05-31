import { WebPlugin } from '@capacitor/core';

import type { BluetoothClassicPlugin } from './definitions';

export class BluetoothClassicWeb extends WebPlugin implements BluetoothClassicPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
