package madskills.callrecorder;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DisplayContactPicker extends AppCompatActivity implements SearchView.OnQueryTextListener,SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView contactsRecyclerView;
    private RecyclerView.Adapter contactsRecyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    Uri uri = ContactsContract.Contacts.CONTENT_URI;
    List<JSONObject> contactsArray = new ArrayList<>();
    ProgressDialog pd;

    private void handleIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if(query != null)
                if(!query.isEmpty()) {
                    if (contactsArray != null) {
                        List<JSONObject> tempList = new ArrayList<>();
                        for (int i = 0; i < contactsArray.size(); i++) {
                            try {
                                String name = contactsArray.get(i).getString("name");
                                String phone = contactsArray.get(i).getString("number0");
                                if (name.toLowerCase().contains(query.toLowerCase()) || phone.toLowerCase().contains(query.toLowerCase())) {
                                    tempList.add(contactsArray.get(i));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        contactsRecyclerViewAdapter = new MyContactsAdapter(tempList);
                        contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
                        contactsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    if(contactsArray == null)
                        new PopulateRecyclerViewTask(DisplayContactPicker.this).execute();
                    contactsRecyclerViewAdapter = new MyContactsAdapter(contactsArray);
                    contactsRecyclerView.setAdapter(contactsRecyclerViewAdapter);
                    contactsRecyclerViewAdapter.notifyDataSetChanged();
                }
        }
    }

    private void fetchContacts() throws JSONException {
        ContentResolver cR= this.getContentResolver();
        String[] projection = {ContactsContract.Contacts.DISPLAY_NAME,ContactsContract.Contacts._ID,ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI,ContactsContract.Contacts.HAS_PHONE_NUMBER};
        Cursor cur = cR.query(uri,projection,null,null,ContactsContract.Contacts.DISPLAY_NAME);
        if(cur != null) {
            while (cur.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                jsonObject.put("id", id);
                jsonObject.put("name", name);
                String pic = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI));
                if (pic == null)
                    jsonObject.put("image", "noImage");
                else
                    jsonObject.put("image", pic);
                jsonObject.put("selected", false);
                if (cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) > 0) {
                    Cursor numberCur = cR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id}, null);
                    int i = 0;
                    if (numberCur != null) {
                        while (numberCur.moveToNext()) {
                            String number = numberCur.getString(numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            jsonObject.put("number" + i, number);
                            i++;
                        }
                        numberCur.close();
                    }
                }
                contactsArray.add(jsonObject);
            }
            cur.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.activity_dislay_contact_picker);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        contactsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager contactsRecyclerViewLayoutManager = new LinearLayoutManager(this);
        contactsRecyclerView.setLayoutManager(contactsRecyclerViewLayoutManager);
        new PopulateRecyclerViewTask(DisplayContactPicker.this).execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_contact_picker, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(DisplayContactPicker.this);
        if(searchManager != null)
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:

                return true;
            case R.id.menu_refresh:
                executeRefresh();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent searchIntent = new Intent(DisplayContactPicker.this, DisplayContactPicker.class);
        searchIntent.setAction(Intent.ACTION_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, s);
        startActivity(searchIntent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Intent searchIntent = new Intent(DisplayContactPicker.this, DisplayContactPicker.class);
        searchIntent.setAction(Intent.ACTION_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, s);
        startActivity(searchIntent);
        return false;
    }

    @Override
    public void onRefresh() {
        executeRefresh();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void executeRefresh(){
        new PopulateRecyclerViewTask(DisplayContactPicker.this).execute();
    }

    private static class PopulateRecyclerViewTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<DisplayContactPicker> weakReference;
        PopulateRecyclerViewTask (DisplayContactPicker displayContactPicker ){
            weakReference = new WeakReference<>(displayContactPicker);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!weakReference.get().swipeRefreshLayout.isRefreshing())
                weakReference.get().pd = ProgressDialog.show(weakReference.get(),
                        "Loading..", "Please Wait", true, false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                weakReference.get().fetchContacts();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!weakReference.get().swipeRefreshLayout.isRefreshing())
                weakReference.get().pd.dismiss();
            weakReference.get().contactsRecyclerViewAdapter = new MyContactsAdapter(weakReference.get().contactsArray);
            weakReference.get().contactsRecyclerView.setAdapter(weakReference.get().contactsRecyclerViewAdapter);
        }
    }
}
