package madskills.callrecorder;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class DisplayContactPicker extends AppCompatActivity {
    private RecyclerView contactsRecyclerView;
    private RecyclerView.Adapter contactsRecyclerViewAdapter;
    private RecyclerView.LayoutManager contactsRecyclerViewLayoutManager;
    Uri uri = ContactsContract.Contacts.CONTENT_URI;
    List<JSONObject> contactsArray = new ArrayList<>();

    private void fetchContacts() throws JSONException {
        ContentResolver cR= this.getContentResolver();
        Cursor cur = cR.query(uri,null,null,null,null);
        while(cur.moveToNext()){
            JSONObject jsonObject = new JSONObject();
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            jsonObject.put("id",id);
            jsonObject.put("name",name);
            String pic = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI));
            if(pic == null)
                jsonObject.put("image","noImage");
            else
                jsonObject.put("image", pic);
            if(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER )>0){
                Cursor numberCur = cR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" =?",new String[]{id},null);
                int i=0;
                while(numberCur.moveToNext()){
                    String number = numberCur.getString(numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    jsonObject.put("number"+i,number);
                    i++;
                }
                numberCur.close();
            }
            System.out.println("Object "+ jsonObject.getString("image"));
            contactsArray.add(jsonObject);
        }
        cur.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dislay_contact_picker);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerViewLayoutManager = new LinearLayoutManager(this);
        contactsRecyclerView.setLayoutManager(contactsRecyclerViewLayoutManager);
        try {
           fetchContacts();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        contactsRecyclerViewAdapter = new MyContactsAdapter(contactsArray);
        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);

    }
}
