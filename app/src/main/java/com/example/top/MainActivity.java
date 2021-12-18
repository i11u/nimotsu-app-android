package com.example.top;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int JIMUTOCHANGE_ACTIVITY = 1001;
    private static final int EVENT_REFRESH_ACTIVITY = 1002;
    String jimuto_room = "";
    String jimuto_name = "";
    String jimuto_id = null;
    String qr_uuid = "";

    final String[] from = {"id", "text"};
    final int[] to = {android.R.id.text2, android.R.id.text1};

    private TouchSound touchsound;
    private DatabaseHelper _helper;
    private SQLiteDatabase db;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _helper = new com.example.top.DatabaseHelper(this);
        db = _helper.getWritableDatabase();
        touchsound = new TouchSound(this);

        ImageButton image_button_touroku = findViewById(R.id.image_button_touroku);
        DoubleTourokuListener listener3 = new DoubleTourokuListener();
        image_button_touroku.setOnClickListener(listener3);

        ImageButton jimutou_change = findViewById(R.id.jimuto_change_button);
        DoubleJimutoChangeListener listener4 = new DoubleJimutoChangeListener();
        jimutou_change.setOnClickListener(listener4);

        ImageButton image_button_uketori = findViewById(R.id.image_button_uketori);
        DoubleUketoriListener listener5 = new DoubleUketoriListener();
        image_button_uketori.setOnClickListener(listener5);

        eventLogshow();
        ListView eventLogshower = findViewById(R.id.event_show);
        EventShowListener showListener = new EventShowListener();
        eventLogshower.setOnItemClickListener(showListener);

        Button parcel_update_button = findViewById(R.id.parcel_update_button);
        SendRequestListener parcels_update_listener = new SendRequestListener("parcel", "update");
        parcel_update_button.setOnClickListener(parcels_update_listener);

        Button parcel_insert_button = findViewById(R.id.parcel_insert_button);
        SendRequestListener parcel_insert_listener = new SendRequestListener("parcel", "create");
        parcel_insert_button.setOnClickListener(parcel_insert_listener);

        Button parcel_debug_button = findViewById(R.id.parcel_debug_button);
        ShowRecordsListener parcels_debug_listener = new ShowRecordsListener("parcel");
        parcel_debug_button.setOnClickListener(parcels_debug_listener);

        Button ryosei_update_button = findViewById(R.id.ryosei_update_button);
        SendRequestListener ryosei_update_listener = new SendRequestListener("ryosei", "update");
        ryosei_update_button.setOnClickListener(ryosei_update_listener);

        Button ryosei_insert_button = findViewById(R.id.ryosei_insert_button);
        SendRequestListener ryosei_insert_listener = new SendRequestListener("ryosei", "create");
        ryosei_insert_button.setOnClickListener(ryosei_insert_listener);

        Button ryosei_debug_button = findViewById(R.id.ryosei_debug_button);
        ShowRecordsListener ryosei_debug_listener = new ShowRecordsListener("ryosei");
        ryosei_debug_button.setOnClickListener(ryosei_debug_listener);

        Button parcel_event_update_button = findViewById(R.id.parcel_event_update_button);
        SendRequestListener parcel_event_update_listener = new SendRequestListener("parcel_event", "update");
        parcel_event_update_button.setOnClickListener(parcel_event_update_listener);

        Button parcel_event_insert_button = findViewById(R.id.parcel_event_insert_button);
        SendRequestListener parcel_event_insert_listener = new SendRequestListener("parcel_event", "create");
        parcel_event_insert_button.setOnClickListener(parcel_event_insert_listener);

        Button parcel_event_debug_button = findViewById(R.id.parcel_event_debug_button);
        ShowRecordsListener parcel_event_debug_listener = new ShowRecordsListener("parcel_event");
        parcel_event_debug_button.setOnClickListener(parcel_event_debug_listener);

        ImageButton duty_night = findViewById(R.id.duty_night_button);
        duty_night_listener listener6 = new duty_night_listener();
        duty_night.setOnClickListener(listener6);

        ImageButton qr_scanner = findViewById(R.id.qr_scanner);
        QRScanListener qr_Listener = new QRScanListener();
        qr_scanner.setOnClickListener(qr_Listener);

        ImageButton nimotsufuda = findViewById(R.id.nimotsufuda_Button);
        RefreshListener listenerNimotsufuda = new RefreshListener();
        nimotsufuda.setOnClickListener(listenerNimotsufuda);
    }

    class buttonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.jimuto_change_button || view.getId() == R.id.image_button_touroku || view.getId() == R.id.image_button_uketori || view.getId() == R.id.event_show || view.getId() == R.id.ryosei_insert_button || view.getId() == R.id.parcel_insert_button || view.getId() == R.id.parcel_event_insert_button || view.getId() == R.id.duty_night_button) {
                final Button button = (Button) findViewById(view.getId());
                button.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        button.setEnabled(true);
                    }
                }, 3000L);
            }

        }
    }

    public void eventLogshow() {
        List<Map<String, String>> show_eventlist = new ArrayList<>();
        String sql = "SELECT uid, created_at, event_type, parcel_uid, room_name, ryosei_name, target_event_uid,is_deleted FROM parcel_event where is_deleted = 0 order by uid desc limit 100";
        Cursor cursor = db.rawQuery(sql, null);
        show_eventlist.clear();
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("is_deleted")).equals("1")) {
                continue;
            }
            Map<String, String> event_raw = new HashMap<>();
            String text = "";
            int index = cursor.getColumnIndex("uid");
            String event_id = String.valueOf(cursor.getInt(index));
            index = cursor.getColumnIndex("event_type");
            int event_type_int = cursor.getInt(index);
            switch (event_type_int) {
                case 1://荷物登録
                    text = "受け取り   ";
                    index = cursor.getColumnIndex("room_name");
                    text += cursor.getString(index);
                    index = cursor.getColumnIndex("ryosei_name");
                    text += "    ";
                    text += cursor.getString(index);
                    index = cursor.getColumnIndex("created_at");
                    text += "  " + cursor.getString(index);
                    event_raw.put("id", event_id);
                    event_raw.put("text", text);
                    show_eventlist.add(event_raw);
                    break;
                case 2://荷物受取
                    text = "引き渡し   ";
                    index = cursor.getColumnIndex("room_name");
                    text += cursor.getString(index);
                    index = cursor.getColumnIndex("ryosei_name");
                    text += "    ";
                    text += cursor.getString(index);
                    index = cursor.getColumnIndex("created_at");
                    text += "  " + cursor.getString(index);
                    event_raw.put("id", event_id);
                    event_raw.put("text", text);
                    show_eventlist.add(event_raw);
                    break;
                case 3://イベント削除：表示しなくてもいいかもね
                    //text="イベントが削除されました";
                    break;
            }

        }
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                show_eventlist,
                android.R.layout.simple_list_item_1,
                from,
                to
        );
        ListView listView = (ListView) findViewById(R.id.event_show);
        listView.setAdapter(adapter);
        ListView listListener = findViewById(R.id.event_show);
        listListener.setOnItemClickListener(new EventShowListener());
    }

    public void onReturnJimutoValue(String value, String id) {
        jimuto_id = id;

        String[] newStr = value.split("\\s+");
        jimuto_room = newStr[0];
        jimuto_name = newStr[1];
        TextView jimuto_show = findViewById(R.id.main_jimutou_show);
        jimuto_show.setText(jimuto_room + " " + jimuto_name);
    }

    private class QRScanListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (jimuto_id == null) {
                this.showMyDialog(null,getString(R.string.main_not_selected_staff),"",getString(R.string.ok),"");
                touchsound.playsoundTwo();
            } else {
                //カメラの呼び出し
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        }
        public  void showMyDialog(View view,String title,String mainText,String positiveButton,String negativeButton) {
            DialogFragment dialogFragment = new myDialog();
            Bundle args = new Bundle();
            args.putString("positivebutton",positiveButton);
            args.putString("negativebutton",negativeButton);
            args.putString("title",title);
            args.putString("maintext",mainText);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "myDialog");
        }

    }

    private class DoubleTourokuListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (jimuto_id == null) {
                this.showMyDialog(null,getString(R.string.main_not_selected_staff),"",getString(R.string.ok),"");
                touchsound.playsoundTwo();
            } else {
                Intent intent = new Intent(MainActivity.this, Double_Buttoned_Touroku.class);
                intent.putExtra("Jimuto_id", jimuto_id);
                intent.putExtra("Jimuto_room", jimuto_room);
                intent.putExtra("Jimuto_name", jimuto_name);
                startActivityForResult(intent, EVENT_REFRESH_ACTIVITY);
                touchsound.playsoundTwo();
            }
        }
        public  void showMyDialog(View view,String title,String mainText,String positiveButton,String negativeButton) {
            DialogFragment dialogFragment = new myDialog();
            Bundle args = new Bundle();
            args.putString("positivebutton",positiveButton);
            args.putString("negativebutton",negativeButton);
            args.putString("title",title);
            args.putString("maintext",mainText);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "myDialog");
        }
    }



    private class DoubleUketoriListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (jimuto_id == null) {
                this.showMyDialog(null,getString(R.string.main_not_selected_staff),"",getString(R.string.ok),"");
                touchsound.playsoundTwo();
            } else {
                Intent intent = new Intent(MainActivity.this, Double_Buttoned_Uketori.class);
                intent.putExtra("Jimuto_id", jimuto_id);
                intent.putExtra("Jimuto_room", jimuto_room);
                intent.putExtra("Jimuto_name", jimuto_name);
                startActivityForResult(intent, EVENT_REFRESH_ACTIVITY);
                touchsound.playsoundTwo();
            }

        }
        public  void showMyDialog(View view,String title,String mainText,String positiveButton,String negativeButton) {
            DialogFragment dialogFragment = new myDialog();
            Bundle args = new Bundle();
            args.putString("positivebutton",positiveButton);
            args.putString("negativebutton",negativeButton);
            args.putString("title",title);
            args.putString("maintext",mainText);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "myDialog");
        }


    }

    private class JimutoChangeListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent jimuto_intent = new Intent(MainActivity.this, Jimuto_Change.class);
            jimuto_intent.putExtra("Jimuto_name", jimuto_room + " " + jimuto_name);
            jimuto_intent.putExtra("Jimuto_id", jimuto_id);
            startActivityForResult(jimuto_intent, JIMUTOCHANGE_ACTIVITY);
        }
    }

    private class DoubleJimutoChangeListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent jimuto_intent = new Intent(MainActivity.this, Double_Jimuto_Change.class);
            jimuto_intent.putExtra("Jimuto_name", jimuto_room + " " + jimuto_name);
            jimuto_intent.putExtra("Jimuto_id", jimuto_id);
            startActivityForResult(jimuto_intent, JIMUTOCHANGE_ACTIVITY);
            touchsound.playsoundTwo();
        }
    }

    private class NightDutyNimotsufudaListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent nimotsufuda_intent = new Intent(MainActivity.this, Night_Duty_NimotsuFuda.class);
            startActivity(nimotsufuda_intent);
            touchsound.playsoundTwo();
        }
    }
    private class RefreshListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            eventLogshow();
        }
    }


    public class duty_night_listener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (jimuto_id == null) {
                this.showMyDialog(null,getString(R.string.main_not_selected_staff),"",getString(R.string.ok),"");
                touchsound.playsoundTwo();
            } else {
                Intent intent = new Intent(MainActivity.this, Night_Duty_NimotsuFuda.class);
                intent.putExtra("Jimuto_id", jimuto_id);
                intent.putExtra("Jimuto_room", jimuto_room);
                intent.putExtra("Jimuto_name", jimuto_name);
                startActivityForResult(intent, EVENT_REFRESH_ACTIVITY);
                touchsound.playsoundTwo();
            }
        }

        public  void showMyDialog(View view,String title,String mainText,String positiveButton,String negativeButton) {
            DialogFragment dialogFragment = new myDialog();
            Bundle args = new Bundle();
            args.putString("positivebutton",positiveButton);
            args.putString("negativebutton",negativeButton);
            args.putString("title",title);
            args.putString("maintext",mainText);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "myDialog");
        }
    }

    private class EventShowListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String event_id = "";
            String ryosei_uid = "";
            String created_at = null;
            String event_type = null;
            String parcel_uid = "";
            String room_name = "";
            String ryosei_name = "";
            String target_event_uid = "";
            String is_finished = "";
            Map<String, String> item = (Map) parent.getItemAtPosition(position);
            //TextView configshow = findViewById(R.id.showText);
            //configshow.setText(item.get("id"));
            item.get("id");
            String sql = "SELECT uid, created_at, event_type,ryosei_uid, parcel_uid, room_name, ryosei_name, target_event_uid FROM parcel_event WHERE uid = " +
                    item.get("id");
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex("uid");
                event_id = String.valueOf(cursor.getInt(index));
                index = cursor.getColumnIndex("created_at");
                created_at = cursor.getString(index);
                created_at = Objects.toString(created_at);
                if (created_at == null) {
                    created_at = "未チェック";
                }
                index = cursor.getColumnIndex("event_type");
                event_type = String.valueOf(cursor.getInt(index));
                index = cursor.getColumnIndex("parcel_uid");
                parcel_uid = String.valueOf(cursor.getInt(index));
                index = cursor.getColumnIndex("room_name");
                room_name = cursor.getString(index);
                index = cursor.getColumnIndex("ryosei_name");
                ryosei_name = cursor.getString(index);
                index = cursor.getColumnIndex("target_event_uid");
                target_event_uid = String.valueOf(cursor.getInt(index));
                index = cursor.getColumnIndex("ryosei_uid");
                ryosei_uid = String.valueOf(cursor.getInt(index));
            }

            if (event_id == "") {
                return;
            }

            DialogFragment dialogFragment = new Delete_Event_Dialog();
            Bundle args = new Bundle();
            args.putString("event_id", event_id);
            args.putString("parcel_id", parcel_uid);
            args.putString("ryosei_id", ryosei_uid);
            args.putString("event_type", event_type);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "Delete_Event_Dialog");
            touchsound.playsoundTwo();

            _helper.close();
        }

    }

    private class A101KumanoTourokuListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得。
            SQLiteDatabase db = _helper.getWritableDatabase();

            // 日時情報を指定フォーマットの文字列で取得
            Date dateObj = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = format.format(dateObj);
            String ryousei = "A101KumanoAjiri";
            String mada = "MadaUketottenai";
            // インサート用SQL文字列の用意。
            //String sqlInsert = "INSERT INTO nimotsu (time, ryosei, done) VALUES (?, ?, ?)";
            String sqlInsert = "INSERT INTO nimotsu VALUES (?, ?, ?)";
            // SQL文字列を元にプリペアドステートメントを取得。
            SQLiteStatement stmt = db.compileStatement(sqlInsert);
            // 変数のバイド。
            stmt.bindString(1, date);
            stmt.bindString(2, ryousei);
            stmt.bindString(3, mada);
            // インサートSQLの実行。
            stmt.executeInsert();

            // 主キーによる検索SQL文字列の用意。
            String sql = "SELECT * FROM nimotsu ";
            // SQLの実行。
            Cursor cursor = db.rawQuery(sql, null);
            // データベースから取得した値を格納する変数の用意。データがなかった時のための初期値も用意。
            String note = "";
            // SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得。
            while (cursor.moveToNext()) {
                // カラムのインデックス値を取得。
                int dateNote = cursor.getColumnIndex("time");
                // カラムのインデックス値を元に実際のデータを取得。
                note += cursor.getString(dateNote);
                int ryouseiNote = cursor.getColumnIndex("ryosei");
                note += cursor.getString(ryouseiNote);
                int ryouseiStatus = cursor.getColumnIndex("done");
                note += cursor.getString(ryouseiStatus);
                note += "\n";

                touchsound.playsoundTwo();
            }
        }
    }

    private class SendRequestListener implements View.OnClickListener {
        String result = null;
        String table;
        String method;

        public SendRequestListener(String table, String method) {
            this.table = table;
            this.method = method;
        };

        @Override
        public void onClick(View view) {
            touchsound.playsoundTwo();
            String json = getJsonFromDatabase();
            OkHttpPost postTask = new OkHttpPost(MainActivity.this, handler, json, db, _helper);
            postTask.url = postTask.url + "/" + table + "/" + method;
            postTask.setListener(createListener());
            postTask.execute();

            while (true) {
                if (this.result == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (this.result.equals("Success")) {
                    result = null;
                    break;
                } else if (this.result.equals("")) {
                    result = null;
                    return;
                }
            }

            OkHttpPost postTask2 = new OkHttpPost(MainActivity.this, handler, "Success", db, _helper);
            postTask2.url = postTask2.url + "/" + table + "/check";
            postTask2.execute();
        }

        private OkHttpPost.Listener createListener() {
            return new OkHttpPost.Listener() {
                @Override
                public void onReceiveResponseFromPC(String res) {
                    result = res;
                }
            };
        }

        private String getJsonFromDatabase() {
            int sharing_status = getSharingStatus();
            switch (table) {
                case "ryosei":
                    return _helper.select_ryosei_show_json(db, sharing_status);
                case "parcel":
                    return _helper.select_parcels_show_json(db, sharing_status);
                case "parcel_event":
                    return _helper.select_event_show_json(db, sharing_status);
                default:
                    return "";
            }
        }

        private int getSharingStatus() {
            switch(method){
                case "create":
                    return 10;
                case "update":
                    return 11;
                case "debug":
                    return 30;
                default:
                    return 0;
            }
        }
    }

    private class ShowRecordsListener implements View.OnClickListener {
        String table;

        public ShowRecordsListener(String table) {
            this.table = table;
        }

        @Override
        public void onClick(View view) {
            // 主キーによる検索SQL文字列の用意。
            String json = getJsonFromDatabase();
        }

        private String getJsonFromDatabase() {
            int sharing_status = 1;
            switch (table) {
                case "ryosei":
                    return _helper.select_ryosei_show_json(db, sharing_status);
                case "parcel":
                    return _helper.select_parcels_show_json(db, sharing_status);
                case "parcel_event":
                    return _helper.select_event_show_json(db, sharing_status);
                default:
                    return "";
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case JIMUTOCHANGE_ACTIVITY:
                jimuto_id = intent.getStringExtra("Jimuto_id");
                String[] newStr = intent.getStringExtra("Jimuto_room_name").split("\\s+");
                jimuto_room = newStr[0];
                jimuto_name = newStr[1];
                TextView jimuto_show = findViewById(R.id.main_jimutou_show);
                jimuto_show.setText(jimuto_room + " " + jimuto_name);
            case EVENT_REFRESH_ACTIVITY:
                boolean event_update = intent.getBooleanExtra("EventRefresh", false);
                eventLogshow();
            default:
                //ここにケースを追加！
        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            qr_uuid = scanResult.getContents();
            TextView qr_show = findViewById(R.id.qr_result);
            qr_show.setText(qr_uuid);
            // DBヘルパーオブジェクトを生成。
            _helper = new DatabaseHelper(MainActivity.this);
            SQLiteDatabase db = _helper.getWritableDatabase();
            String sql = " SELECT uid,is_released,fragile,owner_ryosei_name,owner_room_name,owner_uid FROM parcels WHERE uid ='" + qr_uuid + "';";
            Cursor cursor = db.rawQuery(sql, null);
            int is_released = 0;
            String uid = "";
            String owner_ryosei_name = "";
            String owner_room_name = "";
            int fragile = 0;
            if(cursor.getCount() == 0){//QRコードがデータベースにない場合
                this.showMyDialog(null,getString(R.string.error),getString(R.string.qr_no_qr),getString(R.string.ok),"");
            }else if(cursor.getCount() == 1) {//QRコードがデータベースに一つある場合
                while (cursor.moveToNext()) {
                    int uid_index = cursor.getColumnIndex("owner_uid");
                    int released_index = cursor.getColumnIndex("is_released");
                    int fragile_index = cursor.getColumnIndex("feagile");
                    int owner_room_index = cursor.getColumnIndex("owner_room_name");
                    int owner_ryosei_index = cursor.getColumnIndex("owner_ryosei_name");
                    uid = cursor.getString(uid_index);
                    owner_ryosei_name = cursor.getString(owner_ryosei_index);
                    owner_room_name = cursor.getString(owner_room_index);
                    is_released = cursor.getInt(released_index);
                }

                if(is_released == 0){//その荷物が未受け取りの時
                    this.showQRDialog(null,owner_room_name,owner_ryosei_name,uid);
                }else{//その荷物が受け取り済みの時
                    this.showMyDialog(null,getString(R.string.error),getString(R.string.qr_already),getString(R.string.ok),"");
                }
            }else{//QRコードがデータベースに二つ以上ある場合
                this.showMyDialog(null,getString(R.string.error),getString(R.string.qr_more_than_two),getString(R.string.ok),"");
            }
        }

    }

    public void showQRDialog(View view, String owner_room, String owner_name, String owner_id) {
        DialogFragment dialogFragment = new Nimotsu_Uketori_QR_Dialog();
        Bundle args = new Bundle();
        args.putString("owner_room",owner_room);
        args.putString("owner_name",owner_name);
        args.putString("owner_id",owner_id);
        args.putString("release_staff_room",jimuto_room);
        args.putString("release_staff_name",jimuto_name);
        args.putString("release_staff_id",jimuto_id);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "Nimotsu_Uketori_Dialog");
    }

    public  void showMyDialog(View view,String title,String mainText,String positiveButton,String negativeButton) {
        DialogFragment dialogFragment = new myDialog();
        Bundle args = new Bundle();
        args.putString("positivebutton",positiveButton);
        args.putString("negativebutton",negativeButton);
        args.putString("title",title);
        args.putString("maintext",mainText);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "myDialog");
    }
}
