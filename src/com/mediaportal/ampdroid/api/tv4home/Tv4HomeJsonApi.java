package com.mediaportal.ampdroid.api.tv4home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;

import android.util.Log;

import com.mediaportal.ampdroid.api.CustomDateDeserializer;
import com.mediaportal.ampdroid.api.ITvServiceApi;
import com.mediaportal.ampdroid.api.JsonClient;
import com.mediaportal.ampdroid.data.TvCardDetails;
import com.mediaportal.ampdroid.data.TvChannel;
import com.mediaportal.ampdroid.data.TvChannelDetails;
import com.mediaportal.ampdroid.data.TvChannelGroup;
import com.mediaportal.ampdroid.data.TvProgram;
import com.mediaportal.ampdroid.data.TvProgramBase;
import com.mediaportal.ampdroid.data.TvRecording;
import com.mediaportal.ampdroid.data.TvRtspClient;
import com.mediaportal.ampdroid.data.TvSchedule;
import com.mediaportal.ampdroid.data.TvUser;
import com.mediaportal.ampdroid.data.TvVirtualCard;
import com.mediaportal.ampdroid.utils.IsoDate;
import com.mediaportal.ampdroid.utils.Constants;

public class Tv4HomeJsonApi implements ITvServiceApi {
   private final String TEST_SERVICE = "TestConnectionToTVService";
   private final String GET_GROUPS = "GetGroups";
   private final String GET_CHANNELS = "GetChannelsBasic";
   private final String GET_CHANNELS_INDEX = "GetChannelsBasicByRange";
   private final String GET_CHANNELS_DETAILS = "GetChannelsDetailed";
   private final String GET_CHANNELS_DETAILS_INDEX = "GetChannelsDetailedByRange";
   private final String GET_CHANNELS_COUNT = "GetChannelCount";
   private final String GET_CARDS = "GetCards";
   private final String GET_ACTIVE_CARDS = "GetActiveCards";
   private final String ADD_SCHEDULE = "AddSchedule";
   private final String CANCEL_SCHEDULE = "CancelSchedule";
   private final String DELETE_SCHEDULE = "DeleteSchedule";
   private final String CANCEL_CURRENT_TIMESHIFT = "CancelCurrentTimeShifting";
   private final String GET_CHANNEL_BY_ID = "GetChannelBasicById";
   private final String GET_CHANNEL_DETAILED_BY_ID = "GetChannelDetailedById";
   private final String GET_ACTIVE_USERS = "GetActiveUsers";
   private final String GET_CURRENT_PROGRAM_CHANNEL = "GetCurrentProgramOnChannel";
   private final String GET_PROGRAM = "GetProgramDetailedById";
   private final String GET_PROGRAM_IS_SCHEDULED_CHANNEL = "GetProgramIsScheduledOnChannel";
   private final String GET_PROGRAMS_DETAILED_FOR_CHANNEL = "GetProgramsDetailedForChannel";
   private final String GET_PROGRAMS_BASIC_FOR_CHANNEL = "GetProgramsBasicForChannel";
   private final String GET_RECORDINGS = "GetRecordings";
   private final String GET_SCHEDULES = "GetSchedules";
   private final String GET_STREAMING_CLIENTS = "GetStreamingClients";
   private final String SEARCH_PROGRAMS = "SearchPrograms";
   private final String DATABASE_READ = "ReadSettingFromDatabase";
   private final String DATABASE_WRITE = "WriteSettingToDatabase";
   private final String SWITCH_CHANNEL_GET_URL = "SwitchTVServerToChannelAndGetStreamingUrl";
   private final String SWITCH_CHANNEL_GET_FILE = "SwitchTVServerToChannelAndGetTimeshiftFilename";

   private String m_server;
   private int m_port;
   
   private String m_user;
   private String m_pass;
   private boolean m_useAuth;
   
   private JsonClient mJsonClient;
   private ObjectMapper mJsonObjectMapper;

   private final String JSON_PREFIX = "http://";
   private final String JSON_SUFFIX = "/TV4Home.Server.CoreService/TVEInteractionService/json";

   @SuppressWarnings("unchecked")
   public Tv4HomeJsonApi(String _server, int _port, String _user, String _pass, boolean _auth) {
      m_server = _server;
      m_port = _port;
      m_user = _user;
      m_pass = _pass;
      m_useAuth = _auth;
      
      mJsonClient = new JsonClient(JSON_PREFIX + m_server + ":" + m_port + JSON_SUFFIX, _user, _pass, _auth);
      mJsonObjectMapper = new ObjectMapper();
      mJsonObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   
      CustomDeserializerFactory sf = new CustomDeserializerFactory();
      mJsonObjectMapper.setDeserializerProvider(new StdDeserializerProvider(sf));
      sf.addSpecificMapping(Date.class, new CustomDateDeserializer());
   }
   
   public Tv4HomeJsonApi(String _server, int _port) {
      this(_server, _port, "", "", false);
   }

   @Override
   public String getServer() {
      return m_server;
   }

   @Override
   public int getPort() {
      return m_port;
   }
   
   @Override
   public String getUserName() {
      return m_user;
   }

   @Override
   public String getUserPass() {
      return m_pass;
   }

   @Override
   public boolean getUseAuth() {
      return m_useAuth;
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private Object getObjectsFromJson(String _jsonString, Class _class) {
      try {
         Object returnObjects = mJsonObjectMapper.readValue(_jsonString, _class);

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

   private BasicNameValuePair newPair(String _name, String _value) {
      return new BasicNameValuePair(_name, _value);
   }

   private BasicNameValuePair newPair(String _name, int _value) {
      return new BasicNameValuePair(_name, String.valueOf(_value));
   }
   
   private BasicNameValuePair newPair(String _name, Date _value){
      Calendar cal = Calendar.getInstance();
      cal.setTime(_value);
      int offset = (int)((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000 );

      cal.add(Calendar.MINUTE, offset); 
      String dateString = IsoDate.dateToString(cal.getTime(), IsoDate.DATE_TIME);
  
      return new BasicNameValuePair(_name, dateString);
   }

   @Override
   public String getAddress() {
      return m_server;
   }

   @Override
   public void AddSchedule(int _channelId, String _title, Date _startTime, Date _endTime,
         int _scheduleType) {
      mJsonClient.Execute(ADD_SCHEDULE, newPair("channelId", (_channelId)),
            newPair("title", (_title)), newPair("startTime", (_startTime)),
            newPair("endTime", (_endTime)), newPair("scheduleType", (_scheduleType)));
   }

   @Override
   public boolean CancelCurrentTimeShifting(String _user) {
      String response = mJsonClient.Execute(CANCEL_CURRENT_TIMESHIFT, newPair("userName", (_user)));

      if (response != null) {
         Boolean returnObject = (Boolean) getObjectsFromJson(response, Boolean.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + CANCEL_CURRENT_TIMESHIFT);
         }
      }
      else{
         Log.d(Constants.LOG_CONST, "Error retrieving data for method" + CANCEL_CURRENT_TIMESHIFT);
      }
      return false;
   }

   @Override
   public void cancelScheduleByProgramId(int _programId) {
      mJsonClient.Execute(CANCEL_SCHEDULE, newPair("programId", (_programId)));
   }

   public void cancelScheduleByScheduleId(int _scheduleId) {
      mJsonClient.Execute(DELETE_SCHEDULE, newPair("scheduleId", (_scheduleId)));
   }

   @Override
   public List<TvVirtualCard> GetActiveCards() {
      String response = mJsonClient.Execute(GET_ACTIVE_CARDS);

      if (response != null) {
         TvVirtualCard[] returnArray = (TvVirtualCard[]) getObjectsFromJson(response,
               TvVirtualCard[].class);

         if (returnArray != null) {
            return new ArrayList<TvVirtualCard>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_ACTIVE_CARDS);
         }
      }
      return null;
   }

   @Override
   public List<TvUser> GetActiveUsers() {
      String response = mJsonClient.Execute(GET_ACTIVE_USERS);

      if (response != null) {
         TvUser[] returnArray = (TvUser[]) getObjectsFromJson(response, TvUser[].class);

         if (returnArray != null) {
            return new ArrayList<TvUser>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_ACTIVE_USERS);
         }
      }
      return null;
   }

   @Override
   public List<TvCardDetails> GetCards() {
      String response = mJsonClient.Execute(GET_CARDS);

      if (response != null) {
         TvCardDetails[] returnArray = (TvCardDetails[]) getObjectsFromJson(response,
               TvCardDetails[].class);

         if (returnArray != null) {
            return new ArrayList<TvCardDetails>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CARDS);
         }
      }
      return null;
   }

   @Override
   public TvChannel GetChannelById(int _channelId) {
      String response = mJsonClient.Execute(GET_CHANNEL_BY_ID, newPair("channelId", (_channelId)));

      if (response != null) {
         TvChannel returnObject = (TvChannel) getObjectsFromJson(response, TvChannel.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNEL_BY_ID);
         }
      }
      return null;
   }

   @Override
   public TvChannelDetails GetChannelDetailedById(int _channelId) {
      String response = mJsonClient.Execute(GET_CHANNEL_DETAILED_BY_ID,
            newPair("channelId", (_channelId)));

      if (response != null) {
         TvChannelDetails returnObject = (TvChannelDetails) getObjectsFromJson(response,
               TvChannelDetails.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNEL_DETAILED_BY_ID);
         }
      }
      return null;
   }

   @Override
   public List<TvChannel> GetChannels(int _groupId) {
      String response = mJsonClient.Execute(GET_CHANNELS, newPair("groupId", (_groupId)));

      if (response != null) {
         TvChannel[] returnArray = (TvChannel[]) getObjectsFromJson(response, TvChannel[].class);

         if (returnArray != null) {
            return new ArrayList<TvChannel>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNELS);
         }
      }
      return null;
   }

   @Override
   public List<TvChannelDetails> GetChannelsDetails(int _groupId) {
      String response = mJsonClient.Execute(GET_CHANNELS_DETAILS, newPair("groupId", (_groupId)));

      if (response != null) {
         TvChannelDetails[] returnArray = (TvChannelDetails[]) getObjectsFromJson(response,
               TvChannelDetails[].class);

         if (returnArray != null) {
            return new ArrayList<TvChannelDetails>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNELS_DETAILS);
         }
      }
      return null;
   }

   @Override
   public List<TvChannel> GetChannels(int _groupId, int _startIndex, int _endIndex) {
      String response = mJsonClient.Execute(GET_CHANNELS_INDEX, newPair("groupId", (_groupId)),
            newPair("startIndex", (_startIndex)), newPair("endIndex", (_endIndex)));

      if (response != null) {
         TvChannel[] returnArray = (TvChannel[]) getObjectsFromJson(response, TvChannel[].class);

         if (returnArray != null) {
            return new ArrayList<TvChannel>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNELS_INDEX);
         }
      }
      return null;
   }

   @Override
   public List<TvChannelDetails> GetChannelsDetails(int _groupId, int _startIndex, int _endIndex) {
      String response = mJsonClient.Execute(GET_CHANNELS_DETAILS_INDEX,
            newPair("groupId", (_groupId)), newPair("startIndex", (_startIndex)),
            newPair("endIndex", (_endIndex)));

      if (response != null) {
         TvChannelDetails[] returnArray = (TvChannelDetails[]) getObjectsFromJson(response,
               TvChannelDetails[].class);

         if (returnArray != null) {
            return new ArrayList<TvChannelDetails>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNELS_DETAILS_INDEX);
         }
      }
      return null;
   }

   @Override
   public int GetChannelsCount(int _groupId) {
      String response = mJsonClient.Execute(GET_CHANNELS_COUNT, newPair("groupId", (_groupId)));

      if (response != null) {
         Integer returnObject = (Integer) getObjectsFromJson(response, Integer.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CHANNELS_COUNT);
         }
      }
      return 0;
   }

   @Override
   public TvProgram GetCurrentProgramOnChannel(int _channelId) {
      String response = mJsonClient.Execute(GET_CURRENT_PROGRAM_CHANNEL,
            newPair("channelId", (_channelId)));

      if (response != null) {
         TvProgram returnObject = (TvProgram) getObjectsFromJson(response, TvProgram.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_CURRENT_PROGRAM_CHANNEL);
         }
      }
      return null;
   }

   @Override
   public ArrayList<TvChannelGroup> GetGroups() {
      String response = mJsonClient.Execute(GET_GROUPS);

      if (response != null) {
         TvChannelGroup[] returnArray = (TvChannelGroup[]) getObjectsFromJson(response,
               TvChannelGroup[].class);

         if (returnArray != null) {
            return new ArrayList<TvChannelGroup>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_GROUPS);
         }
      }
      return null;
   }

   @Override
   public TvProgram GetProgramById(int _programId) {
      String response = mJsonClient.Execute(GET_PROGRAM, newPair("programId", (_programId)));

      if (response != null) {
         TvProgram returnObject = (TvProgram) getObjectsFromJson(response, TvProgram.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_PROGRAM);
         }
      }
      return null;
   }

   @Override
   public boolean GetProgramIsScheduledOnChannel(int _channelId, int _programId) {
      String response = mJsonClient.Execute(GET_PROGRAM_IS_SCHEDULED_CHANNEL,
            newPair("channelId", (_channelId)), newPair("programId", (_programId)));

      if (response != null) {
         Boolean returnObject = (Boolean) getObjectsFromJson(response, Boolean.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method "
                  + GET_PROGRAM_IS_SCHEDULED_CHANNEL);
         }
      }
      return false;
   }

   @Override
   public List<TvProgramBase> GetProgramsForChannel(int _channelId, Date _startTime, Date _endTime) {
      String response = mJsonClient.Execute(GET_PROGRAMS_BASIC_FOR_CHANNEL,
            newPair("channelId", (_channelId)), newPair("startTime", (_startTime)),
            newPair("endTime", (_endTime)));

      if (response != null) {
         TvProgramBase[] returnArray = (TvProgramBase[]) getObjectsFromJson(response, TvProgramBase[].class);

         if (returnArray != null) {
            return new ArrayList<TvProgramBase>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_PROGRAMS_BASIC_FOR_CHANNEL);
         }
      }
      return null;
   }
   
   @Override
   public List<TvProgram> GetProgramsDetailsForChannel(int _channelId, Date _startTime, Date _endTime) {
      String response = mJsonClient.Execute(GET_PROGRAMS_DETAILED_FOR_CHANNEL,
            newPair("channelId", (_channelId)), newPair("startTime", (_startTime)),
            newPair("endTime", (_endTime)));

      if (response != null) {
         TvProgram[] returnArray = (TvProgram[]) getObjectsFromJson(response, TvProgram[].class);

         if (returnArray != null) {
            return new ArrayList<TvProgram>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_PROGRAMS_DETAILED_FOR_CHANNEL);
         }
      }
      return null;
   }

   @Override
   public List<TvRecording> GetRecordings() {
      String response = mJsonClient.Execute(GET_RECORDINGS);

      if (response != null) {
         TvRecording[] returnArray = (TvRecording[]) getObjectsFromJson(response,
               TvRecording[].class);

         if (returnArray != null) {
            return new ArrayList<TvRecording>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_RECORDINGS);
         }
      }
      return null;
   }

   @Override
   public List<TvSchedule> GetSchedules() {
      String response = mJsonClient.Execute(GET_SCHEDULES);

      if (response != null) {
         TvSchedule[] returnArray = (TvSchedule[]) getObjectsFromJson(response, TvSchedule[].class);

         if (returnArray != null) {
            return new ArrayList<TvSchedule>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_SCHEDULES);
         }
      }
      return null;
   }

   @Override
   public List<TvRtspClient> GetStreamingClients() {
      String response = mJsonClient.Execute(GET_STREAMING_CLIENTS);

      if (response != null) {
         TvRtspClient[] returnArray = (TvRtspClient[]) getObjectsFromJson(response,
               TvRtspClient[].class);

         if (returnArray != null) {
            return new ArrayList<TvRtspClient>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + GET_STREAMING_CLIENTS);
         }
      }
      return null;
   }

   @Override
   public String ReadSettingFromDatabase(String _tagName) {
      String response = mJsonClient.Execute(DATABASE_READ, newPair("tagName", (_tagName)));

      if (response != null) {
         String returnObject = (String) getObjectsFromJson(response, String.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + DATABASE_READ);
         }
      }
      return null;
   }

   @Override
   public void WriteSettingToDatabase(String _tagName, String _value) {
      mJsonClient.Execute(DATABASE_WRITE, newPair("tagName", (_tagName)),
            newPair("value", (_value)));
   }

   @Override
   public List<TvProgram> SearchPrograms(String _searchTerm) {
      String response = mJsonClient.Execute(SEARCH_PROGRAMS, newPair("_searchTerm", (_searchTerm)));

      if (response != null) {
         TvProgram[] returnArray = (TvProgram[]) getObjectsFromJson(response, TvProgram[].class);

         if (returnArray != null) {
            return new ArrayList<TvProgram>(Arrays.asList(returnArray));
         } else {
            Log.d("Soap", "Error parsing result from soap method " + SEARCH_PROGRAMS);
         }
      }
      return null;
   }

   @Override
   public String SwitchTVServerToChannelAndGetStreamingUrl(String _user, int _channelId) {
      String response = mJsonClient.Execute(SWITCH_CHANNEL_GET_URL, newPair("userName", (_user)),
            newPair("channelId", (_channelId)));

      if (response != null) {
         String returnObject = (String) getObjectsFromJson(response, String.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + SWITCH_CHANNEL_GET_URL);
         }
      }
      return null;
   }

   @Override
   public String SwitchTVServerToChannelAndGetTimeshiftFilename(String _user, int _channelId) {
      String response = mJsonClient.Execute(SWITCH_CHANNEL_GET_FILE, newPair("userName", (_user)),
            newPair("channelId", (_channelId)));

      if (response != null) {
         String returnObject = (String) getObjectsFromJson(response, String.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + SWITCH_CHANNEL_GET_FILE);
         }
      }
      return null;
   }

   @Override
   public boolean TestConnectionToTVService() {
      String response = mJsonClient.Execute(TEST_SERVICE);

      if (response != null) {
         Boolean returnObject = (Boolean) getObjectsFromJson(response, Boolean.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d("Soap", "Error parsing result from soap method " + TEST_SERVICE);
         }
      }
      return false;
   }

}
