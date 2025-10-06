# @nossdev/bluetooth-classic

Simple and straightforward implementation of classic bluetooth communication

## Install

```bash
npm install @nossdev/bluetooth-classic
npx cap sync
```

## API

<docgen-index>

* [`scan(...)`](#scan)
* [`pair(...)`](#pair)
* [`connect(...)`](#connect)
* [`write(...)`](#write)
* [`read(...)`](#read)
* [`readUntil(...)`](#readuntil)
* [`disconnect()`](#disconnect)
* [`isEnabled()`](#isenabled)
* [`enable()`](#enable)
* [`addListener(BluetoothState | 'bluetoothState', ...)`](#addlistenerbluetoothstate--bluetoothstate-)
* [`removeAllListeners()`](#removealllisteners)
* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### scan(...)

```typescript
scan(options?: ScanOptions | undefined) => Promise<ScanResult>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#scanoptions">ScanOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#scanresult">ScanResult</a>&gt;</code>

--------------------


### pair(...)

```typescript
pair(options: PairOptions) => Promise<void>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#pairoptions">PairOptions</a></code> |

--------------------


### connect(...)

```typescript
connect(options: ConnectOptions) => Promise<void>
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#connectoptions">ConnectOptions</a></code> |

--------------------


### write(...)

```typescript
write(options: WriteOptions) => Promise<void>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#writeoptions">WriteOptions</a></code> |

--------------------


### read(...)

```typescript
read(options?: ReadOptions | undefined) => Promise<ReadResult>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#readoptions">ReadOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#readresult">ReadResult</a>&gt;</code>

--------------------


### readUntil(...)

```typescript
readUntil(options: ReadUntilOptions) => Promise<ReadResult>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#readuntiloptions">ReadUntilOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#readresult">ReadResult</a>&gt;</code>

--------------------


### disconnect()

```typescript
disconnect() => Promise<void>
```

--------------------


### isEnabled()

```typescript
isEnabled() => Promise<{ enabled: boolean; }>
```

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

--------------------


### enable()

```typescript
enable() => Promise<{ enabled: boolean; }>
```

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

--------------------


### addListener(BluetoothState | 'bluetoothState', ...)

```typescript
addListener(eventName: BluetoothState | 'bluetoothState', listenerFunc: (data: BluetoothStateEvent) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                   |
| ------------------ | -------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#bluetoothstate">BluetoothState</a> \| 'bluetoothState'</code>          |
| **`listenerFunc`** | <code>(data: <a href="#bluetoothstateevent">BluetoothStateEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### Interfaces


#### ScanResult

| Prop          | Type                           |
| ------------- | ------------------------------ |
| **`devices`** | <code>BluetoothDevice[]</code> |


#### BluetoothDevice

| Prop              | Type                                                            |
| ----------------- | --------------------------------------------------------------- |
| **`name`**        | <code>string</code>                                             |
| **`type`**        | <code><a href="#devicetype">DeviceType</a></code>               |
| **`state`**       | <code><a href="#devicestate">DeviceState</a></code>             |
| **`address`**     | <code>string</code>                                             |
| **`addressType`** | <code><a href="#deviceaddresstype">DeviceAddressType</a></code> |


#### ScanOptions

| Prop           | Type                |
| -------------- | ------------------- |
| **`duration`** | <code>number</code> |


#### PairOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`address`** | <code>string</code> |


#### ConnectOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`address`** | <code>string</code> |


#### WriteOptions

| Prop       | Type                  |
| ---------- | --------------------- |
| **`data`** | <code>number[]</code> |


#### ReadResult

| Prop       | Type                  |
| ---------- | --------------------- |
| **`data`** | <code>number[]</code> |


#### ReadOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`timeout`** | <code>number</code> |


#### ReadUntilOptions

| Prop            | Type                  |
| --------------- | --------------------- |
| **`delimiter`** | <code>number[]</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### PermissionStatus

| Prop         | Type                                           |
| ------------ | ---------------------------------------------- |
| **`status`** | <code>'granted' \| 'denied' \| 'prompt'</code> |


### Type Aliases


#### DeviceType

<code>'classic' | 'le' | 'dual' | 'unknown'</code>


#### DeviceState

<code>'none' | 'bonded' | 'bonding' | 'unknown'</code>


#### DeviceAddressType

<code>'public' | 'random' | 'anonymous' | 'unknown'</code>


#### BluetoothState

<code>'on' | 'off' | 'turning_on' | 'turning_off'</code>


#### BluetoothStateEvent

<code>{ value: <a href="#bluetoothstate">BluetoothState</a> }</code>

</docgen-api>
