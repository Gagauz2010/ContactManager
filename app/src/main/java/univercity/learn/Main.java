package univercity.learn;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Main extends Activity {

    private static final int EDIT = 0, CALL = 1, SMS = 2, EMAIL = 3, MAP = 4, DELETE = 5;

    List<Contact> Contacts = new ArrayList<Contact>();

    Uri imageURI = Uri.parse("android.resource://univercity.learn/" +  R.drawable.no_profile_image), defaultURI = Uri.parse("android.resource://univercity.learn/" +  R.drawable.no_profile_image);

    DatabaseHandler dbHandler;

    ListView contactListView;
    ImageView contactImageImgView;
    EditText nameTxt, phoneTxt, emailTxt, addressTxt;

    private static final String EMPTY_STRING = "";

    int longClickedItemIndex;
    ArrayAdapter<Contact> contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.listView);
        contactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);

        dbHandler = new DatabaseHandler(getApplicationContext());

        TabHost tabHost;
        tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Добавить");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("Контакты");
        tabHost.addTab(tabSpec);


        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = new Contact(dbHandler.getContactsCount(), String.valueOf(nameTxt.getText()), String.valueOf(phoneTxt.getText()), String.valueOf(emailTxt.getText()), String.valueOf(addressTxt.getText()), imageURI);

                if (!contactExists(contact)) {
                    dbHandler.createContact(contact);
                    Contacts.add(contact);
                    contactAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), nameTxt.getText().toString() + R.string.added_completely, Toast.LENGTH_SHORT).show();

                    nameTxt.setText(EMPTY_STRING);
                    phoneTxt.setText(EMPTY_STRING);
                    emailTxt.setText(EMPTY_STRING);
                    addressTxt.setText(EMPTY_STRING);
                    imageURI =  defaultURI;
                    contactImageImgView.setImageURI(defaultURI);
                    addBtn.setEnabled(false);

                    return;
                }

                Toast.makeText(getApplicationContext(), nameTxt.getText().toString() + R.string.already_exist, Toast.LENGTH_SHORT).show();
            }
        });

        contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 1);
            }
        });

        if (dbHandler.getContactsCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());

        populateList();

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }
        });

        registerForContextMenu(contactListView);
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIndex = position;
                return false;
            }
        });
    }

    private boolean contactExists(Contact contact){
        String name = contact.getName();

        for (univercity.learn.Contact Contact : Contacts) {
            if (name.compareToIgnoreCase(Contact.getName()) == 0)
                return true;
        }
        return false;
    }

    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1) {
                contactImageImgView.setImageURI(data.getData());
                imageURI = data.getData();
            }
        }
    }

    private void populateList(){
        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }

    private class ContactListAdapter extends ArrayAdapter<Contact>{
        public ContactListAdapter(){
            super(Main.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.getName());
            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());
            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            email.setText(currentContact.getEmail());
            TextView address = (TextView) view.findViewById(R.id.cAddress);
            address.setText(currentContact.getAddress());
            ImageView image = (ImageView) view.findViewById(R.id.ivContactImage);
            image.setImageURI(currentContact.getImageURI());

            return view;
        }
    }

    public  void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        //menu.setHeaderIcon(R.drawable.edit_icon);
        menu.setHeaderTitle("Опции");
        menu.add(Menu.NONE, EDIT, menu.NONE, R.string.menu_edit);
        menu.add(Menu.NONE, CALL, menu.NONE, R.string.menu_call);
        menu.add(Menu.NONE, SMS, menu.NONE, R.string.menu_sms);
        menu.add(Menu.NONE, EMAIL, menu.NONE, R.string.menu_email);
        menu.add(Menu.NONE, MAP, menu.NONE, R.string.menu_map);
        menu.add(Menu.NONE, DELETE, menu.NONE, R.string.menu_delete);
    }

    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case EDIT:
                //TODO: Дdd edit contact
                break;
            case CALL: //NOTE: Worked!
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + Contacts.get(longClickedItemIndex).getPhone()));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Вызов", "Звонок не удался", activityException);
                }
                break;
            case SMS: //NOTE: Worked!
                try {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.setData(Uri.parse("sms:" + Contacts.get(longClickedItemIndex).getPhone()));
                    startActivity(smsIntent);
                }catch (ActivityNotFoundException activityException) {
                    Log.e("Отправка SMS", "Отправка не удалась", activityException);
                }
                break;
            case EMAIL:
                try{
                    //TODO: Add email intent
                }catch (ActivityNotFoundException activityException) {
                    Log.e("Отправка почты", "Отправка не удалась", activityException);
                }
                break;
            case MAP:
                // TODO: Add geo intent
                break;
            case DELETE: //NOTE: Worked!
                dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
                Contacts.remove(longClickedItemIndex);
                contactAdapter.notifyDataSetChanged();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }
}
