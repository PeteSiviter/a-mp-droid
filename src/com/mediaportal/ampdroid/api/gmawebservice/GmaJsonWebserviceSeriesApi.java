package com.mediaportal.ampdroid.api.gmawebservice;

import java.util.ArrayList;
import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

import com.mediaportal.ampdroid.api.JsonClient;
import com.mediaportal.ampdroid.api.JsonUtils;
import com.mediaportal.ampdroid.data.EpisodeDetails;
import com.mediaportal.ampdroid.data.Series;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.data.SeriesFull;
import com.mediaportal.ampdroid.data.SeriesSeason;
import com.mediaportal.ampdroid.utils.LogUtils;

public class GmaJsonWebserviceSeriesApi {

   private JsonClient mJsonClient;
   private ObjectMapper mJsonObjectMapper;

   private static final String GET_ALL_SERIES = "MP_GetAllSeries";
   private static final String GET_SERIES = "MP_GetSeries";
   private static final String GET_SERIES_COUNT = "MP_GetSeriesCount";
   private static final String GET_FULL_SERIES = "MP_GetFullSeries";
   private static final String GET_ALL_SEASONS = "MP_GetAllSeasons";
   private static final String GET_SEASON = "MP_GetSeason";
   private static final String GET_ALL_EPISODES = "MP_GetAllEpisodes";
   private static final String GET_EPISODES = "MP_GetEpisodes";
   private static final String GET_EPISODES_COUNT = "MP_GetEpisodesCount";
   private static final String GET_EPISODES_COUNT_FOR_SEASON = "MP_GetEpisodesCountForSeason";
   private static final String GET_ALL_EPISODES_FOR_SEASON = "MP_GetAllEpisodesForSeason";
   private static final String GET_EPISODES_FOR_SEASON = "MP_GetEpisodesForSeason";
   private static final String GET_FULL_EPISODE = "MP_GetFullEpisode";

   public GmaJsonWebserviceSeriesApi(JsonClient _wcfService, ObjectMapper _mapper) {
      mJsonClient = _wcfService;
      mJsonObjectMapper = _mapper;
   }

   private Object getObjectsFromJson(String _jsonString, Class _class) {
      return JsonUtils.getObjectsFromJson(_jsonString, _class, mJsonObjectMapper);
   }

   public ArrayList<Series> getAllSeries() {
      String methodName = GET_ALL_SERIES;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         Series[] returnObject = (Series[]) getObjectsFromJson(response, Series[].class);

         if (returnObject != null) {
            return new ArrayList<Series>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public ArrayList<Series> getSeries(int _start, int _end) {
      String methodName = GET_SERIES;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("startIndex", _start),
            JsonUtils.newPair("endIndex", _end));

      if (response != null) {
         Series[] returnObject = (Series[]) getObjectsFromJson(response, Series[].class);

         if (returnObject != null) {
            return new ArrayList<Series>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public SeriesFull getFullSeries(int _seriesId) {
      String methodName = GET_FULL_SERIES;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId));

      if (response != null) {
         SeriesFull returnObject = (SeriesFull) getObjectsFromJson(response, SeriesFull.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public int getSeriesCount() {
      String methodName = GET_SERIES_COUNT;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         Integer returnObject = (Integer) getObjectsFromJson(response, Integer.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return 0;
   }

   public ArrayList<SeriesSeason> getAllSeasons(int _seriesId) {
      String methodName = GET_ALL_SEASONS;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId));

      if (response != null) {
         SeriesSeason[] returnObject = (SeriesSeason[]) getObjectsFromJson(response,
               SeriesSeason[].class);

         if (returnObject != null) {
            return new ArrayList<SeriesSeason>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public SeriesSeason getSeason(int _seriesId, int _seasonNumber) {
      String methodName = GET_SEASON;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("seasonNumber", _seasonNumber));

      if (response != null) {
         SeriesSeason returnObject = (SeriesSeason) getObjectsFromJson(response, SeriesSeason.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public ArrayList<SeriesEpisode> getAllEpisodes(int _seriesId) {
      String methodName = GET_ALL_EPISODES;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId));

      if (response != null) {
         SeriesEpisode[] returnObject = (SeriesEpisode[]) getObjectsFromJson(response,
               SeriesEpisode[].class);

         if (returnObject != null) {
            return new ArrayList<SeriesEpisode>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public ArrayList<SeriesEpisode> getEpisodes(int _seriesId, int _start, int _end) {
      String methodName = GET_EPISODES;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("startIndex", _start), JsonUtils.newPair("endIndex", _end));

      if (response != null) {
         SeriesEpisode[] returnObject = (SeriesEpisode[]) getObjectsFromJson(response,
               SeriesEpisode[].class);

         if (returnObject != null) {
            return new ArrayList<SeriesEpisode>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public int getEpisodesCount() {
      String methodName = GET_EPISODES_COUNT;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         Integer returnObject = (Integer) getObjectsFromJson(response, Integer.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return -99;
   }

   public ArrayList<SeriesEpisode> getAllEpisodesForSeason(int _seriesId, int _seasonNumber) {
      String methodName = GET_ALL_EPISODES_FOR_SEASON;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("seasonNumber", _seasonNumber));

      if (response != null) {
         SeriesEpisode[] returnObject = (SeriesEpisode[]) getObjectsFromJson(response,
               SeriesEpisode[].class);

         if (returnObject != null) {
            return new ArrayList<SeriesEpisode>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public int getEpisodesCountForSeason(int _seriesId, int _seasonNumber) {
      String methodName = GET_EPISODES_COUNT_FOR_SEASON;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("season", _seasonNumber));

      if (response != null) {
         Integer returnObject = (Integer) getObjectsFromJson(response, Integer.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return -99;
   }

   public ArrayList<SeriesEpisode> getEpisodesForSeason(int _seriesId, int _seasonId, int _start,
         int _end) {
      String methodName = GET_EPISODES_FOR_SEASON;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("seasonId", _seasonId), JsonUtils.newPair("startIndex", _start),
            JsonUtils.newPair("endIndex", _end));

      if (response != null) {
         SeriesEpisode[] returnObject = (SeriesEpisode[]) getObjectsFromJson(response,
               SeriesEpisode[].class);

         if (returnObject != null) {
            return new ArrayList<SeriesEpisode>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public EpisodeDetails getFullEpisode(int _seriesId, int _episodeId) {
      String methodName = GET_FULL_EPISODE;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("seriesId", _seriesId),
            JsonUtils.newPair("episodeId", _episodeId));

      if (response != null) {
         EpisodeDetails returnObject = (EpisodeDetails) getObjectsFromJson(response,
               EpisodeDetails.class);

         if (returnObject != null) {
            return returnObject;
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }
}
