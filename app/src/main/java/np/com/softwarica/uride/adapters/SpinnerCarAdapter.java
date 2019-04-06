package np.com.softwarica.uride.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.databinding.ItemCarsBinding;
import np.com.softwarica.uride.models.CarItem;

public class SpinnerCarAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CarItem> listCarItems;

    public SpinnerCarAdapter(Context context, ArrayList<CarItem> listCarItems) {
        this.context = context;
        this.listCarItems = listCarItems;
    }

    @Override
    public int getCount() {
        return listCarItems.size();
    }

    @Override
    public Object getItem(int i) {
        return listCarItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ItemCarsBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_cars, viewGroup, false);
        binding.setItem(listCarItems.get(i));
        return binding.getRoot();
    }
}
