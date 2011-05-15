package com.mediaportal.ampdroid.api.gmawebservice;

import java.util.ArrayList;
import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

import com.mediaportal.ampdroid.api.JsonClient;
import com.mediaportal.ampdroid.api.JsonUtils;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MovieFull;
import com.mediaportal.ampdroid.utils.LogUtils;

class GmaJsonWebserviceMovieApi {
   private JsonClient mJsonClient;
   private ObjectMapper mJsonObjectMapper;

   private static final String GET_ALL_MOVIES = "MP_GetAllMovies";
   private static final String GET_MOVIES_COUNT = "MP_GetMovieCount";
   private static final String GET_MOVIES = "MP_GetMovies";
   private static final String GET_MOVIE_DETAILS = "MP_GetFullMovie";
   private static final String SEARCH_FOR_MOVIE = "MP_SearchForMovie";

   public GmaJsonWebserviceMovieApi(JsonClient _jsonClient, ObjectMapper _mapper) {
      mJsonClient = _jsonClient;
      mJsonObjectMapper = _mapper;
   }

   @SuppressWarnings("rawtypes")
   private Object getObjectsFromJson(String _jsonString, Class _class) {
      return JsonUtils.getObjectsFromJson(_jsonString, _class, mJsonObjectMapper);
   }

   ArrayList<Movie> getAllMovies() {
      String methodName = GET_ALL_MOVIES;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         Movie[] returnObject = (Movie[]) getObjectsFromJson(response, Movie[].class);

         if (returnObject != null) {
            return new ArrayList<Movie>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   int getMovieCount() {
      String methodName = GET_MOVIES_COUNT;
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

   MovieFull getMovieDetails(int _movieId) {
      String methodName = GET_MOVIE_DETAILS;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("movieId", _movieId));

      if (response != null) {
         MovieFull returnObject = (MovieFull) getObjectsFromJson(response, MovieFull.class);

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

   ArrayList<Movie> getMovies(int _start, int _end) {
      String methodName = GET_MOVIES;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("startIndex", _start),
            JsonUtils.newPair("endIndex", _end));

      if (response != null) {
         Movie[] returnObject = (Movie[]) getObjectsFromJson(response, Movie[].class);

         if (returnObject != null) {
            return new ArrayList<Movie>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   ArrayList<Movie> searchForMovie(String _searchString) {
      String methodName = SEARCH_FOR_MOVIE;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("searchString", _searchString));

      if (response != null) {
         Movie[] returnObject = (Movie[]) getObjectsFromJson(response, Movie[].class);

         if (returnObject != null) {
            return new ArrayList<Movie>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }
}
