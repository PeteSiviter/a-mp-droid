package com.mediaportal.ampdroid.api;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

import com.mediaportal.ampdroid.data.EpisodeDetails;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MovieFull;
import com.mediaportal.ampdroid.data.MusicAlbum;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.data.Series;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.data.SeriesFull;
import com.mediaportal.ampdroid.data.SeriesSeason;
import com.mediaportal.ampdroid.data.SupportedFunctions;
import com.mediaportal.ampdroid.data.TvCardDetails;
import com.mediaportal.ampdroid.data.TvChannel;
import com.mediaportal.ampdroid.data.TvChannelGroup;
import com.mediaportal.ampdroid.data.TvRecording;
import com.mediaportal.ampdroid.data.TvSchedule;
import com.mediaportal.ampdroid.data.TvVirtualCard;
import com.mediaportal.ampdroid.data.VideoShare;
import com.mediaportal.ampdroid.data.commands.RemoteKey;

public class DataHandler {
   private RemoteClient client;
   private RemoteFunctions functions;
   private static List<RemoteClient> clientList = new ArrayList<RemoteClient>();
   private static int currentClient = 0;
   private static DataHandler dataHandler;
   private IMediaAccessDatabase mediaDatabase;

   private DataHandler(RemoteClient _client) {
      client = _client;
   }

   public static boolean setupRemoteHandler(RemoteClient _client, boolean _checkConnection) {
      dataHandler = new DataHandler(_client);
      setFunctions(_client, false);

      return true;
   }
   
   private static void setFunctions(RemoteClient _client, boolean _checkConnection){
      dataHandler.functions = new RemoteFunctions();

      ITvServiceApi tvApi = _client.getTvControlApi();
      if (tvApi != null) {
         dataHandler.functions.setTvEnabled(true);
         if (_checkConnection) {
            dataHandler.functions.setTvAvailable(tvApi.TestConnectionToTVService());
         }
      } else {
         dataHandler.functions.setTvEnabled(false);
      }

      IMediaAccessApi mediaApi = _client.getRemoteAccessApi();
      if (mediaApi != null) {
         dataHandler.functions.setMediaEnabled(true);
         if (_checkConnection) {
            // todo: test function for media access
            dataHandler.functions.setMediaAvailable(true);
         }
      } else {
         dataHandler.functions.setMediaEnabled(false);
      }

      IMediaAccessApi remoteApi = _client.getRemoteAccessApi();
      if (remoteApi != null) {
         dataHandler.functions.setRemoteEnabled(true);
         if (_checkConnection) {
            // todo: test function for remote access
            dataHandler.functions.setRemoteAvailable(true);
         }
      } else {
         dataHandler.functions.setRemoteEnabled(false);
      }
   }

   public static DataHandler getCurrentRemoteInstance() {
      return dataHandler;
   }

   public static void clearRemotClients() {
      clientList.clear();
   }

   public static void addRemoteClient(RemoteClient _client) {
      clientList.add(_client);
   }

   public static List<RemoteClient> getRemoteClients() {
      return clientList;
   }

   public static void updateRemoteClient(RemoteClient mClient) {
      for (int i = 0; i < clientList.size(); i++) {
         if (clientList.get(i).getClientId() == mClient.getClientId()) {
            clientList.set(i, mClient);
         }
      }
   }

   public static void setCurrentRemoteInstance(int _id) {
      for (RemoteClient c : clientList) {
         dataHandler.client = c;
         setFunctions(dataHandler.client, false);
      }
   }

   public List<VideoShare> getAllVideoShares() {
      return client.getRemoteAccessApi().getVideoShares();
   }

   public List<Movie> getAllMovies() {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllMovies();
   }

   public MovieFull getMovieDetails(int _movieId) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getMovieDetails(_movieId);
   }

   public Bitmap getBitmap(String _id) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getBitmap(_id);
   }

   public ArrayList<Series> getAllSeries() {
      // mediaDatabase.getAllSeries();
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllSeries();
   }

   public ArrayList<Series> getSeries(int _start, int _end) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getSeries(_start, _end);
   }

   public SeriesFull getFullSeries(int _seriesId) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getFullSeries(_seriesId);
   }

   public int getSeriesCount() {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getSeriesCount();
   }

   public ArrayList<SeriesSeason> getAllSeasons(int _seriesId) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllSeasons(_seriesId);
   }

   public ArrayList<SeriesEpisode> getAllEpisodes(int _seriesId) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllEpisodes(_seriesId);
   }

   public ArrayList<SeriesEpisode> getAllEpisodesForSeason(int _seriesId, int _seasonNumber) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllEpisodesForSeason(_seriesId, _seasonNumber);
   }

   public EpisodeDetails getEpisode(int _id) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getEpisode(_id);
   }

   public ArrayList<MusicAlbum> getAllAlbums() {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAllAlbums();
   }

   public SupportedFunctions getSupportedFunctions() {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getSupportedFunctions();
   }

   public List<MusicAlbum> getAlbums(int _start, int _end) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getAlbums(_start, _end);
   }

   public Bitmap getImage(String _url) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getBitmap(_url);
   }

   public Bitmap getImage(String _url, int _maxWidth, int _maxHeight) {
      IMediaAccessApi remoteAccess = client.getRemoteAccessApi();
      return remoteAccess.getBitmap(_url, _maxWidth, _maxHeight);
   }

   public ArrayList<TvChannelGroup> getTvChannelGroups() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetGroups();
   }

   public List<TvChannel> getTvChannelsForGroup(int _groupId) {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetChannels(_groupId);
   }

   public List<TvChannel> getTvChannelsForGroup(int _groupId, int _startIndex, int _endIndex) {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetChannels(_groupId, _startIndex, _endIndex);
   }

   public int getTvChannelsCount(int _groupId) {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetChannelsCount(_groupId);
   }

   public List<TvCardDetails> getTvCards() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetCards();
   }

   public List<TvVirtualCard> getTvCardsActive() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetActiveCards();
   }
   
   public String startTimeshift(int _channelId) {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.SwitchTVServerToChannelAndGetStreamingUrl("Android2", _channelId);
   }

   public boolean isTvServiceActive() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.TestConnectionToTVService();
   }
   

   public boolean stopTimeshift(String _user) {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.CancelCurrentTimeShifting(_user);
   }
   
   public List<TvRecording> getTvRecordings() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetRecordings();
   }
   

   public List<TvSchedule> getTvSchedules() {
      ITvServiceApi tvApi = client.getTvControlApi();
      return tvApi.GetSchedules();
   }

   public void setFunctions(RemoteFunctions functions) {
      this.functions = functions;
   }

   public RemoteFunctions getFunctions() {
      return functions;
   }

   public void addClientControlListener(IClientControlListener _listener) {
      client.getClientControlApi().addApiListener(_listener);
   }

   public boolean connectClientControl() {
      return client.getClientControlApi().connect();
   }

   public void disconnectClientControl() {
      client.getClientControlApi().disconnect();
   }

   public boolean isClientControlConnected() {
      return client.getClientControlApi().isConnected();
   }

   public void sendRemoteButton(RemoteKey _button) {
      client.getClientControlApi().sendKeyCommand(_button);
   }

   public void sendClientVolume(int _level) {
      client.getClientControlApi().setVolume(_level);
   }

   public int getClientVolume() {
      return client.getClientControlApi().getVolume();
   }

   public String getDownloadUri(String _filePath) {
      return client.getRemoteAccessApi().getDownloadUri(_filePath);
   }







}
