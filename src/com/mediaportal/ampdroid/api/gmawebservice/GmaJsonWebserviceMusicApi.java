package com.mediaportal.ampdroid.api.gmawebservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;

import android.util.Log;

import com.mediaportal.ampdroid.api.CustomDateDeserializer;
import com.mediaportal.ampdroid.api.JsonClient;
import com.mediaportal.ampdroid.api.JsonUtils;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MovieFull;
import com.mediaportal.ampdroid.data.MusicAlbum;
import com.mediaportal.ampdroid.data.MusicArtist;
import com.mediaportal.ampdroid.data.MusicTrack;
import com.mediaportal.ampdroid.utils.LogUtils;

class GmaJsonWebserviceMusicApi {
   private JsonClient mJsonClient;
   private ObjectMapper mJsonObjectMapper;

   private static final String GET_MUSIC_TRACK = "MP_GetMusicTrack";
   private static final String GET_ALL_ALBUMS = "MP_GetAllAlbums";
   private static final String GET_ALBUMS = "MP_GetAlbums";
   private static final String GET_ALBUMS_COUNT = "MP_GetAlbumsCount";
   private static final String GET_ALL_ARTISTS = "MP_GetAllArtists";
   private static final String GET_ARTISTS = "MP_GetArtists";
   private static final String GET_ALBUM = "MP_GetAlbum";
   private static final String GET_ARTISTS_COUNT = "MP_GetArtistsCount";
   private static final String GET_ALBUMS_BY_ARTIST = "MP_GetAlbumsByArtist";
   private static final String GET_SONGS_OF_ALBUM = "MP_GetSongsOfAlbum";
   private static final String FIND_MUSIC_TRACKS = "MP_FindMusicTracks";

   public GmaJsonWebserviceMusicApi(JsonClient _jsonClient, ObjectMapper _mapper) {
      mJsonClient = _jsonClient;
      mJsonObjectMapper = _mapper;
   }

   private Object getObjectsFromJson(String _jsonString, Class _class) {
      return JsonUtils.getObjectsFromJson(_jsonString, _class, mJsonObjectMapper);
   }

   public MusicTrack getMusicTrack(int trackId) {
      String methodName = GET_MUSIC_TRACK;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("trackId", trackId));

      if (response != null) {
         MusicTrack returnObject = (MusicTrack) getObjectsFromJson(response, MusicTrack.class);

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

   public MusicAlbum getAlbum(String albumArtistName, String albumName) {
      String methodName = GET_ALBUM;
      String response = mJsonClient.Execute(methodName,
            JsonUtils.newPair("albumArtistName", albumArtistName),
            JsonUtils.newPair("albumName", albumName));

      if (response != null) {
         MusicAlbum returnObject = (MusicAlbum) getObjectsFromJson(response, MusicAlbum.class);

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

   public List<MusicAlbum> getAllAlbums() {
      String methodName = GET_ALL_ALBUMS;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         MusicAlbum[] returnObject = (MusicAlbum[]) getObjectsFromJson(response, MusicAlbum[].class);

         if (returnObject != null) {
            return new ArrayList<MusicAlbum>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public List<MusicAlbum> getAlbums(int _start, int _end) {
      String methodName = GET_ALBUMS;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("_start", _start),
            JsonUtils.newPair("_end", _end));

      if (response != null) {
         MusicAlbum[] returnObject = (MusicAlbum[]) getObjectsFromJson(response, MusicAlbum[].class);

         if (returnObject != null) {
            return new ArrayList<MusicAlbum>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public int getAlbumsCount() {
      String methodName = GET_ALBUMS_COUNT;
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

   public List<MusicArtist> getAllArtists() {
      String methodName = GET_ALL_ARTISTS;
      String response = mJsonClient.Execute(methodName);

      if (response != null) {
         MusicArtist[] returnObject = (MusicArtist[]) getObjectsFromJson(response,
               MusicArtist[].class);

         if (returnObject != null) {
            return new ArrayList<MusicArtist>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public List<MusicArtist> getArtists(int _start, int _end) {
      String methodName = GET_ARTISTS;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("_start", _start),
            JsonUtils.newPair("_end", _end));

      if (response != null) {
         MusicArtist[] returnObject = (MusicArtist[]) getObjectsFromJson(response,
               MusicArtist[].class);

         if (returnObject != null) {
            return new ArrayList<MusicArtist>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public int getArtistsCount() {
      String methodName = GET_ARTISTS_COUNT;
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

   public List<MusicAlbum> getAlbumsByArtist(String artistName) {
      String methodName = GET_ALBUMS_BY_ARTIST;
      String response = mJsonClient
            .Execute(methodName, JsonUtils.newPair("artistName", artistName));

      if (response != null) {
         MusicAlbum[] returnObject = (MusicAlbum[]) getObjectsFromJson(response, MusicAlbum[].class);

         if (returnObject != null) {
            return new ArrayList<MusicAlbum>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public List<MusicTrack> getSongsOfAlbum(String albumName, String albumArtistName) {
      String methodName = GET_SONGS_OF_ALBUM;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("albumName", albumName),
            JsonUtils.newPair("albumArtistName", albumArtistName));

      if (response != null) {
         MusicTrack[] returnObject = (MusicTrack[]) getObjectsFromJson(response, MusicTrack[].class);

         if (returnObject != null) {
            return new ArrayList<MusicTrack>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }

   public List<MusicTrack> findMusicTracks(String album, String artist, String title) {
      String methodName = FIND_MUSIC_TRACKS;
      String response = mJsonClient.Execute(methodName, JsonUtils.newPair("album", album),
            JsonUtils.newPair("artist", artist), JsonUtils.newPair("title", title));

      if (response != null) {
         MusicTrack[] returnObject = (MusicTrack[]) getObjectsFromJson(response, MusicTrack[].class);

         if (returnObject != null) {
            return new ArrayList<MusicTrack>(Arrays.asList(returnObject));
         } else {
            Log.d(LogUtils.LOG_CONST, "Error parsing result from JSON method " + methodName);
         }
      } else {
         Log.d(LogUtils.LOG_CONST, "Error retrieving data for method" + methodName);
      }
      return null;
   }
}
