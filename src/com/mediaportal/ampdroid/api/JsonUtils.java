package com.mediaportal.ampdroid.api;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

import com.mediaportal.ampdroid.utils.IsoDate;
import com.mediaportal.ampdroid.utils.Constants;

public class JsonUtils {
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public static Object getObjectsFromJson(String _jsonString, Class _class, ObjectMapper _objectMapper) {
      try {
         Object returnObjects = _objectMapper.readValue(_jsonString, _class);

         return returnObjects;
      } catch (JsonParseException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      } catch (JsonMappingException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      } catch (IOException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      }
      return null;
   }

   public static BasicNameValuePair newPair(String _name, String _value) {
      return new BasicNameValuePair(_name, _value);
   }

   public static BasicNameValuePair newPair(String _name, int _value) {
      return new BasicNameValuePair(_name, String.valueOf(_value));
   }

   public static BasicNameValuePair newPair(String _name, Date _value) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(_value);
      int offset = (int) ((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000);

      cal.add(Calendar.MINUTE, offset);
      String dateString = IsoDate.dateToString(cal.getTime(), IsoDate.DATE_TIME);

      return new BasicNameValuePair(_name, dateString);
   }
}
