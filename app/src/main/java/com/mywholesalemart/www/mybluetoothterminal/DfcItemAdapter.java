package com.mywholesalemart.www.mybluetoothterminal;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fr3ts0n.ecu.EcuCodeItem;
import com.fr3ts0n.pvs.IndexedProcessVar;
import com.fr3ts0n.pvs.PvList;

import java.util.Collection;
import java.util.Objects;

/**
 * Adapter to display OBD DFCs
 *
 */

class DfcItemAdapter extends ObdItemAdapter
{
    public DfcItemAdapter(Context context, int resource, PvList pvs)
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
     */
    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        // get data PV
        IndexedProcessVar currPv = (IndexedProcessVar) getItem(position);

        if (v == null)
        {
            v = mInflater.inflate(R.layout.obd_item, parent, false);
        }
        TextView tvDescr = v.findViewById(R.id.obd_label);
        TextView tvValue = v.findViewById(R.id.obd_units);

        tvValue.setText(String.valueOf(Objects.requireNonNull(currPv).get(EcuCodeItem.FID_CODE)));
        tvDescr.setText(String.valueOf(currPv.get(EcuCodeItem.FID_DESCRIPT)));

        return v;
    }

    /* (non-Javadoc)
     * @see com.fr3ts0n.ecu.gui.androbd.ObdItemAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(int position, View v, ViewGroup parent)
    {
        return getView(position, v, parent);
    }
}
