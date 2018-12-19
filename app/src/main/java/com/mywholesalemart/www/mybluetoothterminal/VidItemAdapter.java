package com.mywholesalemart.www.mybluetoothterminal;

import android.content.Context;

import com.fr3ts0n.pvs.PvList;

import java.util.Collection;

/**
 * Adapter to display OBD VID items from a process variable list
 *
 */


class VidItemAdapter extends ObdItemAdapter
{
    public VidItemAdapter(Context context, int resource, PvList pvs)
    {
        super(context, resource, pvs);
    }

    @Override
    public Collection<Object> getPreferredItems(PvList pvs)
    {
        return pvs.values();
    }

	/* (non-Javadoc)
	 * @see com.fr3ts0n.ecu.gui.androbd.ObdItemAdapter#getView(int, android.view.View, android.view.ViewGroup)
	@Override
	public View getView(int position, View v, ViewGroup parent)
	{
		// let superclass format the item
		v = super.getView(position, v, parent);
		// hide the checkbox
		CheckBox cbx = (CheckBox) v.findViewById(R.id.check);
		cbx.setVisibility(View.GONE);

		return v;
	}
   */
}
