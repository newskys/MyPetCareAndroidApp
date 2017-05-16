package petcare.com.mypetcare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import petcare.com.mypetcare.Activity.CustomView.GridViewItem;
import petcare.com.mypetcare.Model.AdoptListData;
import petcare.com.mypetcare.R;
import petcare.com.mypetcare.Util.VolleySingleton;

/**
 * Created by KS on 2017-04-13.
 */

public class AdoptGridViewAdapter extends BaseAdapter {
    private List<AdoptListData> list;
    private LayoutInflater inf;
    private Context context;
    private int layout;
    private ImageLoader imageLoader;

    public AdoptGridViewAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;
        inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public String getItemSaleId(int position) {
        return list.get(position).getId();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String id, String url, Double longitude, Double latitude, String radius) {
        AdoptListData data = new AdoptListData();
        data.setId(id);
        data.setImgUrl(url);
        data.setLongitude(longitude);
        data.setLatitude(latitude);
        data.setRadius(radius);
        list.add(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inf.inflate(layout, null);
        }

        imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        NetworkImageView image = (NetworkImageView) convertView.findViewById(R.id.iv_gridview_adopt_list);
        image.setImageUrl(list.get(position).getImgUrl(), imageLoader);

        return convertView;
    }
}
