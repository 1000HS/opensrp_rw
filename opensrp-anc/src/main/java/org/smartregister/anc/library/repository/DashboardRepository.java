package org.smartregister.anc.library.repository;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.custom.CustomClient;
import org.smartregister.anc.library.util.DBConstantsUtils;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import org.smartregister.view.activity.DrishtiApplication;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


public class DashboardRepository extends BaseRepository {

    public static final String TABLE_PREVIOUS_CONTACT = "previous_contact";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    private static final String[] projection = getRegisterQueryProvider().mainColumns();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getDueContactDash(LocalDate datetoday) {

            SQLiteDatabase db = getMasterRepository().getReadableDatabase();

//            String query = "SELECT COUNT(*) FROM " + getRegisterQueryProvider().getDetailsTable() + " WHERE " + DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE+ "= '"+datetoday+"'";
//        SQLiteStatement statement = db.compileStatement(query);
//        long count = statement.simpleQueryForLong();
//        return count;
        String query = "SELECT * FROM " + getRegisterQueryProvider().getDetailsTable();
        Cursor   cursor = db.rawQuery(query, null);
        int count=0;
        while (cursor.moveToNext()){
            String next_contact_date=cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE));


            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-d");
            LocalDate  d1=null;
            if(next_contact_date.charAt(0)=='-'){
                d1 = LocalDate.parse(next_contact_date.substring(1), df);
            }
            else{
                d1 = LocalDate.parse(next_contact_date, df);
            }

            if(datetoday.isEqual(d1) || datetoday.isAfter(d1)) {
                count++;
            }else{

            }


        }
        return count;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getWomanReferred(String dateStart, String dateEnd) {

        SQLiteDatabase db = getMasterRepository().getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PREVIOUS_CONTACT ;
      Cursor   cursor = db.rawQuery(query, null);
      int count=0;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-d");
        LocalDate  start= LocalDate.parse(dateStart, df);
        LocalDate  end= LocalDate.parse(dateEnd, df);
      while (cursor.moveToNext()){
          if (cursor.getString(3).equals("contact_date")){
              LocalDate d=LocalDate.parse(cursor.getString(4));
              if(d.isAfter(start) && d.isBefore(end)) {
                  if(isReferred(cursor.getString(2))){
                      count++;
                  }
              }
          }

      }
        return count;
    }

    private static boolean isReferred(String woman_id) {
        SQLiteDatabase db = getMasterRepository().getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PREVIOUS_CONTACT + " WHERE base_entity_id= '"+woman_id+"'";
        Cursor   cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
            try {
                if (cursor.getString(3).equals("referred_hosp")) {


                JSONObject val = new JSONObject(cursor.getString(4));

                if (val.get(VALUE).toString().equals("yes")) {

                    return true;
                }
                else {
                    return  false;
                }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static long getProcessedVisits(LocalDate datet) {

        SQLiteDatabase db = getMasterRepository().getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + TABLE_PREVIOUS_CONTACT + " WHERE " + KEY + "= 'contact_date' AND " +VALUE+ "='"+datet+"'";
        Cursor   cursor = db.rawQuery(query, null);
        SQLiteStatement statement = db.compileStatement(query);
        long count = statement.simpleQueryForLong();
        return count;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getWomanWithDangerSing(String dateStart, String dateEnd) {

        SQLiteDatabase db = getMasterRepository().getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PREVIOUS_CONTACT ;
        Cursor   cursor = db.rawQuery(query, null);
        int count=0;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-d");
        LocalDate  start= LocalDate.parse(dateStart, df);
        LocalDate  end= LocalDate.parse(dateEnd, df);
        while (cursor.moveToNext()){
            if (cursor.getString(3).equals("contact_date")){
                LocalDate d=LocalDate.parse(cursor.getString(4));
                if(d.isAfter(start) && d.isBefore(end)) {
                    if(hasDangerSign(cursor.getString(2))){
                        count++;
                    }
                }
            }

        }
        return count;
    }
    public static boolean hasDangerSign(String woman_id){

        SQLiteDatabase db = getMasterRepository().getReadableDatabase();

        String query = "SELECT * FROM " + getRegisterQueryProvider().getDetailsTable()+" WHERE id= '"+woman_id+"'";
        Cursor   cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()){
              String r=cursor.getString(18);
             if (!"0".equals(r) && r!= null){
                 return true;
             }else{
                 return false;
             }

        }

        return false;
    }
    protected static Repository getMasterRepository() {
        return DrishtiApplication.getInstance().getRepository();
    }
    private static RegisterQueryProvider getRegisterQueryProvider() {
        return AncLibrary.getInstance().getRegisterQueryProvider();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<CustomClient> getWomanProfileDetails(String baseEntityId) {
        Cursor cursor = null;
        List<CustomClient> clientList=new ArrayList<>();

        try {
            SQLiteDatabase db = getMasterRepository().getReadableDatabase();

            String query =
                    "SELECT " + StringUtils.join(projection, ",") + " FROM " + getRegisterQueryProvider().getDemographicTable() + " join " + getRegisterQueryProvider().getDetailsTable() +
                            " on " + getRegisterQueryProvider().getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = " + getRegisterQueryProvider().getDetailsTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " WHERE " +
                            getRegisterQueryProvider().getDetailsTable() + "." + DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE + " = ?";
            cursor = db.rawQuery(query, new String[]{baseEntityId});

            if (cursor != null ) {

                while (cursor.moveToNext()){
                    CustomClient client=new CustomClient();
                    client.setFirstName(cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.FIRST_NAME)));
                    client.setLastName(cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.LAST_NAME)));
                    client.setAge(String.valueOf(calculateAge(cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DOB)))));
                    client.setGa(cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.EDD)));
                    client.setRegistrationId(cursor.getString(cursor.getColumnIndex("register_id")));
                    client.setNextContactDate(cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE)));
                    String r=cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT));
                    String red= r == null?  "0":  r;
                    String y=cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT));
                    String yellow= y == null?  "0":  y;
                    client.setAttentionFlag(Integer.parseInt(red)+ Integer.parseInt(yellow));
                    clientList.add(client)  ;

            }
    }

            return clientList;
        } catch (Exception e) {
            Timber.e(e, "%s ==> getWomanProfileDetails()", PatientRepository.class.getCanonicalName());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static int calculateAge(String birthDate) {
        LocalDate currentDate =  LocalDate.now();
        if ((birthDate != null) && (currentDate != null)) {
            return  currentDate.getYear()-Integer.valueOf(birthDate.split("-", 0)[0]);
        } else {
            return 0;
        }
    }
}
