package com.mfahmialif.pulseoximeter

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*

private const val GATT_MAX_MTU_SIZE = 517

class BluetoothHandle : AppCompatActivity(){

    companion object{
        var statusBT = "off"
        var deviceBT : BluetoothDevice?= null
        var bluetoothGatt: BluetoothGatt? = null
        var spo2 = ""
        var bpm = ""
    }
    var companion = Companion

    val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"

    public fun connect(device: BluetoothDevice, context: Context){
        deviceBT = device
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                    gatt.discoverServices()
                    gatt.requestMtu(GATT_MAX_MTU_SIZE)

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    statusBT = "off"
                    deviceBT = null
                    spo2 = "0"
                    bpm = "0"
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                statusBT = "off"
                deviceBT = null
                spo2 = "0"
                bpm = "0"
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.w("BluetoothGattCallback", "Discovered ${gatt?.services?.size} services for ${gatt?.device?.address}")
            gatt?.printGattTable()
//            tesNotify()
            Log.i("gasnotify", "${gatt.toString()} nottt")
            statusBT = "on"
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            with(characteristic) {
                Log.i("BluetoothGattCallback", "status : $status")
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Read characteristic $uuid:\n${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            with(characteristic) {
//                Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: ${value.toHexString()}")
                if (value[0].toString() == "-127"){
                    bpm = value[1].toString()
                    spo2 = value[2].toString()
                    Log.w("tes delay", "get spo2 : ${value[2].toString()}")
                    Log.w("tes delay", "get bpm : ${value[1].toString()}")
                    Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: BPM : ${value[1].toString()}")
                    Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: SPO2 : ${value[2].toString()}")
                    Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: PI :${value[3].toString()}")
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.w("BluetoothGattCallback", "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
        }

    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }

        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                    separator = "\n|--",
                    prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
            for (c in service.characteristics){
                if (c.isNotifiable() == true){
                    Log.i("notifgat", "${c.uuid.toString()} cihuyyy")
                    Log.i("notifgat", "${c.toString()} cihuyyy")
                    Log.i("readgatt", "${c.descriptors.toString()} ini descriptors")
                    val ds = c.descriptors
                    for (d in ds){
                        Log.i("readgatt", "${d.uuid.toString()} ini descriptor")
                    }
                }else if(c.isReadable() == true){
                    Log.i("notifgataaaa", "${service.uuid.toString()} cihuyyy")
                    Log.i("readablegat", "${c.uuid.toString()} cihuyyy")
                    Log.i("readablegat", "${c.toString()} cihuyyy")

                }
            }

        }
    }

    private fun tesRead(serviceuid : String, charuid : String) {
        val serviceUUID = serviceuid
        val characteristicUUID = charuid
        val characteristic = bluetoothGatt?.getService(UUID.fromString(serviceUUID))?.getCharacteristic(UUID.fromString(characteristicUUID))

        if(characteristic?.isReadable() == true){
            Log.i("readgatt", "${characteristic.toString()} Bisa Read")
            bluetoothGatt?.readCharacteristic(characteristic)
        }else{
            Log.i("readgatt", "${characteristic.toString()} Tidak Bisa Read")
        }

    }

    public fun enableNotifyBT() {
        Log.i("tes", "Bisa notify")
        val serviceUUID = "cdeacb80-5235-4c07-8846-93a37ee6b86d"
        val characteristicUUID = "cdeacb81-5235-4c07-8846-93a37ee6b86d"
        val characteristic = bluetoothGatt?.getService(UUID.fromString(serviceUUID))?.getCharacteristic(UUID.fromString(characteristicUUID))
        if(characteristic?.isNotifiable() == true){
            Log.i("gasnotify", "${characteristic.toString()} Bisa notify")
            enableNotifications(characteristic)
        }else{
            Log.i("gasnotify", "${characteristic.toString()} Tidak Bisa notify")
        }
    }

    public fun disableNotifyBT() {
        Log.i("tes", "Bisa notify")
        val serviceUUID = "cdeacb80-5235-4c07-8846-93a37ee6b86d"
        val characteristicUUID = "cdeacb81-5235-4c07-8846-93a37ee6b86d"
        val characteristic = bluetoothGatt?.getService(UUID.fromString(serviceUUID))?.getCharacteristic(UUID.fromString(characteristicUUID))
        if(characteristic?.isNotifiable() == true){
            Log.i("gasnotify", "${characteristic.toString()} Bisa disable notify")
            disableNotifications(characteristic)
        }else{
            Log.i("gasnotify", "${characteristic.toString()} Tidak Bisa notify")
        }
    }

    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        val uuid: UUID = UUID.fromString(CCC_DESCRIPTOR_UUID)
        val descriptor = characteristic.getDescriptor(uuid).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        bluetoothGatt?.writeDescriptor(descriptor)
    }

    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, false)
        val uuid: UUID = UUID.fromString(CCC_DESCRIPTOR_UUID)
        val descriptor = characteristic.getDescriptor(uuid).apply {
            value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        bluetoothGatt?.writeDescriptor(descriptor)
    }

    fun ByteArray.toHexString(): String =
            joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
            properties and property != 0

    fun BluetoothGattDescriptor.isReadable(): Boolean =
            containsPermission(BluetoothGattDescriptor.PERMISSION_READ) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM)

    fun BluetoothGattDescriptor.isWritable(): Boolean =
            containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||
                    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM)

    fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
            permissions and permission != 0

}