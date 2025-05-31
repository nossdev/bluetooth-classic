import { BluetoothClassic } from '@nossdev/bluetooth-classic';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    BluetoothClassic.echo({ value: inputValue })
}
