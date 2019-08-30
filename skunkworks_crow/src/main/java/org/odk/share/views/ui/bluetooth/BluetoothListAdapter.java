package org.odk.share.views.ui.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.share.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;


/**
 * BluetoothListAdapter: {@link RecyclerView.Adapter} for {@link BluetoothDevice}.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 * @since 12/06/2019
 */
public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDeviceList;
    private OnDeviceClickListener btDeviceClickListener;

    public BluetoothListAdapter(OnDeviceClickListener btDeviceClickListener) {
        this.btDeviceClickListener = btDeviceClickListener;
        this.bluetoothDeviceList = new ArrayList<>();
    }

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
        notifyDataSetChanged();
    }

    public void addDevices(Set<BluetoothDevice> devices) {
        if (!bluetoothDeviceList.containsAll(devices)) {
            this.bluetoothDeviceList.addAll(devices);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
        BluetoothDevice device = bluetoothDeviceList.get(position);
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        int deviceBondState = device.getBondState();
        viewHolder.deviceName.setText(deviceName == null ? viewHolder.itemView.getResources().getString(R.string.bluetooth_instance_name_default) : deviceName);
        viewHolder.deviceAddress.setText(String.format("%s (%s)", deviceAddress,
                deviceBondState == BOND_NONE ? context.getString(R.string.bluetooth_unpaired) : context.getString(R.string.bluetooth_paired)));
        if (deviceBondState == BOND_BONDED) {
            viewHolder.deviceLogo.setImageResource(R.drawable.ic_smart_phone_yellow);
        }
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
     * View holder for the bluetooth devices.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_device_name)
        TextView deviceName;

        @BindView(R.id.tv_device_address)
        TextView deviceAddress;

        @BindView(R.id.iv_device)
        ImageView deviceLogo;

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
     * clear all the data in the list.
     */
    public void clearBluetoothDeviceList() {
        bluetoothDeviceList.clear();
        notifyDataSetChanged();
    }

    /**
     * listener for bluetooth devices clicked.
     */
    public interface OnDeviceClickListener {
        void onItemClick(BluetoothDevice dev);
    }
}
