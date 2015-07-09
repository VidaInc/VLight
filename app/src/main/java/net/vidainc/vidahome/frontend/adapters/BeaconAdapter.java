package net.vidainc.vidahome.frontend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aprilbrother.aprilbrothersdk.Beacon;

import net.vidainc.vidahome.R;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconAdapter extends BaseAdapter {

	private ArrayList<Beacon> beacons;
	private LayoutInflater inflater;

	public BeaconAdapter(Context context) {
		this.inflater = LayoutInflater.from(context);
		this.beacons = new ArrayList<>();
	}

	public void replaceWith(Collection<Beacon> newBeacons) {
		this.beacons.clear();
		this.beacons.addAll(newBeacons);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return beacons.size();
	}

	@Override
	public Beacon getItem(int position) {
		return beacons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		view = inflateIfRequired(view, position, parent);
		bind(getItem(position), view);
		return view;
	}

	private void bind(Beacon beacon, View view) {
		NewViewHolder holder = (NewViewHolder) view.getTag();
		holder.name.setText(beacon.getName());
	}

	private View inflateIfRequired(View view, int position, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.device_item_new, null);
			view.setTag(new NewViewHolder(view));
		}
		return view;
	}

	static class ViewHolder {
		final TextView macTextView;
		final TextView uuidTextView;
		final TextView majorTextView;
		final TextView minorTextView;
		final TextView measuredPowerTextView;
		final TextView rssiTextView;
		final TextView proximityView;

		ViewHolder(View view) {
			macTextView = (TextView) view.findViewWithTag("mac");
			uuidTextView = (TextView) view.findViewWithTag("uuid");
			majorTextView = (TextView) view.findViewWithTag("major");
			minorTextView = (TextView) view.findViewWithTag("minor");
			measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
			rssiTextView = (TextView) view.findViewWithTag("rssi");
			proximityView = (TextView) view.findViewWithTag("proximity");
		}
	}
	
	static class NewViewHolder {
		final TextView name;

		NewViewHolder(View view) {
			name = (TextView) view.findViewWithTag("name");
		}
	}
}
