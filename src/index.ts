import { registerPlugin } from '@capacitor/core';

import type { BluetoothClassicPlugin } from './definitions';

const BluetoothClassic = registerPlugin<BluetoothClassicPlugin>('BluetoothClassic', {
  web: () => import('./web').then((m) => new m.BluetoothClassicWeb()),
});

export * from './definitions';
export { BluetoothClassic };
