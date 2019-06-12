package org.odk.share.views.ui.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.share.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * BluetoothDeviceAdapter: {@link RecyclerView.Adapter} for {@link BluetoothDevice}.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 12/06/2019
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDeviceList;
    private OnDeviceClickListener btDeviceClickListener;


    /**
     * Methods for updating data.
     */
    public void setDevices(List<BluetoothDevice> dateSet) {
        this.bluetoothDeviceList = dateSet;
    }

    public void addDevice(BluetoothDevice bluetoothDevice) {
        if (!bluetoothDeviceList.contains(bluetoothDevice)) {
            this.bluetoothDeviceList.add(bluetoothDevice);
        }
    }

    public BluetoothDeviceAdapter(OnDeviceClickListener btDeviceClickListener) {
        this.btDeviceClickListener = btDeviceClickListener;
        this.bluetoothDeviceList = new ArrayList<>();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        BluetoothDevice device = bluetoothDeviceList.get(position);
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        int deviceBondState = device.getBondState();
        viewHolder.deviceName.setText(deviceName == null ? "" : deviceName);
        viewHolder.deviceAddress.setText(String.format("%s (%s)", deviceAddress, deviceBondState == 10 ? "unpaired" : "paired"));
    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bt_device, null);
        return new ViewHolder(view);
    }


    /**
     * rescan the bluetooth devices and update the list.
     */
    public void rescan() {
        bluetoothDeviceList.clear();
        addBoundedDevices();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        notifyDataSetChanged();
    }


    /**
     * Add the bounded bluetooth devices.
     */
    private void addBoundedDevices() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null) {
            bluetoothDeviceList.addAll(bondedDevices);
        }
    }


    /**
     * View holder for the bluetooth devices.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_device_name)
        TextView deviceName;

        @BindView(R.id.tv_device_address)
        TextView deviceAddress;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position >= 0 && position < bluetoothDeviceList.size()) {
                btDeviceClickListener.onItemClick(bluetoothDeviceList.get(position));
            }
        }
    }


    /**
     * listener for bluetooth devices clicked.
     */
    public interface OnDeviceClickListener {
        void onItemClick(BluetoothDevice dev);
    }
}
