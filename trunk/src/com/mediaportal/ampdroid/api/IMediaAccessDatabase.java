package com.mediaportal.ampdroid.api;

import java.util.ArrayList;
import java.util.List;

import com.mediaportal.ampdroid.data.CacheItemsSetting;
import com.mediaportal.ampdroid.data.EpisodeDetails;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MovieFull;
import com.mediaportal.ampdroid.data.Series;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.data.SeriesFull;
import com.mediaportal.ampdroid.data.SeriesSeason;
import com.mediaportal.ampdroid.data.SupportedFunctions;

public interface IMediaAccessDatabase {
   //general
   SupportedFunctions getSupportedFunctions();
   void setSupportedFunctions(SupportedFunctions supported);
   public void open();
   public void close();
   
   //Movies
   ArrayList<Movie> getAllMovies();
   List<Movie> getMovies(int _start, int _end);
   void saveMovie(Movie _movie);
   CacheItemsSetting getMovieCount();
   CacheItemsSetting setMovieCount(int movieCount);
   MovieFull getMovieDetails(int _movieId);
   void saveMovieDetails(MovieFull _movie);
   
   //Series
   List<Series> getSeries(int _start, int _end);
   void saveSeries(Series _series);
   List<Series> getAllSeries();
   CacheItemsSetting getSeriesCount();
   CacheItemsSetting setSeriesCount(int _seriesCount);
   
   SeriesFull getFullSeries(int _seriesId);
   void saveSeriesDetails(SeriesFull series);
   
   List<SeriesSeason> getAllSeasons(int _seriesId);
   void saveSeason(SeriesSeason s);
   
   List<SeriesEpisode> getAllEpisodes(int _seriesId);
   void saveEpisode(int _seriesId, SeriesEpisode _episode);
   List<SeriesEpisode> getAllEpisodesForSeason(int _seriesId, int _seasonNumber);
   //CacheItemsSetting getEpisodesCountForSeason(int _seriesId, int _seasonNumber);
   //CacheItemsSetting setEpisodesCountForSeason(int _seriesId, int _seasonNumber, int _episodesCount);
   
   EpisodeDetails getEpisodeDetails(int _seriesId, int _episodeId);
   void saveEpisodeDetails(int _seriesId, EpisodeDetails episode);
   

}
