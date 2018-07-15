package madskills.callrecorder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.View_Holder> {
    private List<JSONObject> mDataset;
    RecyclerView contactsRecyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        contactsRecyclerView = recyclerView;
    }
    public class View_Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        RelativeLayout layout;
        CardView cv;
        TextView title;
        TextView description;
        ImageView imageView;

        View_Holder(View itemView) {
            super(itemView);
            cv = (CardView) itemView;
            layout = itemView.findViewById(R.id.relativeLayout);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            imageView = (ImageView) itemView.findViewById(R.id.contactImageView);
            layout.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            System.out.println("onClick");
            try {
                System.out.println(getLayoutPosition());
                JSONObject jsonObject = mDataset.get(getLayoutPosition());
                System.err.println("Before : "+ jsonObject.getBoolean("selected"));
                jsonObject.put("selected",!jsonObject.getBoolean("selected"));
                System.err.println("After : "+ jsonObject.getBoolean("selected"));
                view.setBackgroundColor(jsonObject.getBoolean("selected") ? Color.CYAN : Color.WHITE);
                ImageView iv = (ImageView) view.findViewById(R.id.contactImageView);
                //TODO: For contact photos
                iv.setImageResource(jsonObject.getBoolean("selected") ? R.drawable.ic_baseline_how_to_reg_24px :R.drawable.ic_baseline_person_24px);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
    public void onBindViewHolder(@NonNull final View_Holder viewHolder,final int i) {
        try {
            final JSONObject jsonObject = mDataset.get(i);
            viewHolder.title.setText(jsonObject.getString("name"));
            viewHolder.description.setText(jsonObject.getString("number0"));
            if(jsonObject.getString("image").equals("noImage")) {
                viewHolder.imageView.setImageResource(jsonObject.getBoolean("selected") ? R.drawable.ic_baseline_how_to_reg_24px :R.drawable.ic_baseline_person_24px);
            }
            else {
                viewHolder.imageView.setImageURI(Uri.parse(jsonObject.getString("image")));
                System.err.println(jsonObject.getString("image"));
            }
            viewHolder.layout.setBackgroundColor(jsonObject.getBoolean("selected") ? Color.CYAN : Color.WHITE);
            System.err.println("Item View Type : "+ viewHolder.getItemViewType());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateList(List<JSONObject> list){
        mDataset = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDataset == null? 0 :mDataset.size();
    }
}


