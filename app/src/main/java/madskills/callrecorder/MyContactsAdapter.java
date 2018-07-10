package madskills.callrecorder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.View_Holder> {
    private List<JSONObject> mDataset;
    public class View_Holder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView title;
        TextView description;
        ImageView imageView;

        View_Holder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cards);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            imageView = itemView.findViewById(R.id.contactImageView);
        }
    }
    public MyContactsAdapter(List<JSONObject> myDataset) {
        mDataset = myDataset;
    }
    @NonNull
    @Override
    public View_Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item_layout, viewGroup, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull View_Holder viewHolder, int i) {
        try {
            viewHolder.title.setText(mDataset.get(i).getString("name"));
            viewHolder.description.setText(mDataset.get(i).getString("number0"));
            //TODO: Images repeating problem
            if(mDataset.get(i).getString("image").equals("noImage")) {
                viewHolder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
                System.err.println("SETTING GRAY");
            }
            else {
                viewHolder.imageView.setImageURI(Uri.parse(mDataset.get(i).getString("image")));
                System.err.println(mDataset.get(i).getString("image"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}


