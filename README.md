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
* [`read()`](#read)
* [`readUntil(...)`](#readuntil)
* [`disconnect()`](#disconnect)
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


### read()

```typescript
read() => Promise<ReadResult>
```

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


#### ReadUntilOptions

| Prop            | Type                  |
| --------------- | --------------------- |
| **`delimiter`** | <code>number[]</code> |
| **`timeout`**   | <code>number</code>   |


### Type Aliases


#### DeviceType

<code>'classic' | 'le' | 'dual' | 'unknown'</code>


#### DeviceState

<code>'none' | 'bonded' | 'bonding' | 'unknown'</code>


#### DeviceAddressType

<code>'public' | 'random' | 'anonymous' | 'unknown'</code>

</docgen-api>
