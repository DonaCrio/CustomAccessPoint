package com.donatien.customaccesspoint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.donatien.customaccesspoint.AccessPoint.Client;

import java.util.List;

public class ClientAdapter extends ArrayAdapter<Client> {

    public ClientAdapter(Context context, List<Client> clients) {
        super(context, 0, clients);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.client_layout,parent, false);
        }
        ClientViewHolder viewHolder = (ClientViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new ClientViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.ipAddr = (TextView) convertView.findViewById(R.id.ipAddr);
            viewHolder.hwAddr = (TextView) convertView.findViewById(R.id.hwAddr);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            convertView.setTag(viewHolder);
        }

        // We get the item [position] in List<Client>
        Client client = getItem(position);
        // We add client's field values to the view
        viewHolder.name.setText(client.getName());
        viewHolder.ipAddr.setText(client.getIpAddr());
        viewHolder.hwAddr.setText(client.getHwAddr());
        // We set the avatar according to client's status
        switch(client.getStatus().toInteger()) {
            case -1:
                viewHolder.avatar.setImageResource(R.drawable.client_dangerous);
                break;
            case 0:
                viewHolder.avatar.setImageResource(R.drawable.client_unknown);
                break;
            case 1:
                viewHolder.avatar.setImageResource(R.drawable.client_approved);
                break;
            default:
                viewHolder.avatar.setImageResource(R.drawable.client_unknown);
                break;
        }
        viewHolder.status = client.getStatus().toInteger();

        return convertView;
    }

    /**
     * Class for representing the View
     */
    public class ClientViewHolder{
        public TextView name;
        public TextView ipAddr;
        public TextView hwAddr;
        public ImageView avatar;
        public int status;
    }

}

