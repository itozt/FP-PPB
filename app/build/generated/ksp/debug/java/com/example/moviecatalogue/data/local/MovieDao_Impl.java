package com.example.moviecatalogue.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.moviecatalogue.domain.Genre;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MovieDao_Impl implements MovieDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MovieEntity> __insertionAdapterOfMovieEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<MovieEntity> __deletionAdapterOfMovieEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMovieById;

  public MovieDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMovieEntity = new EntityInsertionAdapter<MovieEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `watchlist_movies` (`id`,`title`,`overview`,`posterPath`,`backdropPath`,`releaseDate`,`voteAverage`,`voteCount`,`genreIds`,`genres`,`popularity`,`originalLanguage`,`adult`,`addedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MovieEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getOverview());
        if (entity.getPosterPath() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPosterPath());
        }
        if (entity.getBackdropPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBackdropPath());
        }
        statement.bindString(6, entity.getReleaseDate());
        statement.bindDouble(7, entity.getVoteAverage());
        statement.bindLong(8, entity.getVoteCount());
        final String _tmp = __converters.fromIntList(entity.getGenreIds());
        statement.bindString(9, _tmp);
        final String _tmp_1 = __converters.fromGenreList(entity.getGenres());
        statement.bindString(10, _tmp_1);
        statement.bindDouble(11, entity.getPopularity());
        statement.bindString(12, entity.getOriginalLanguage());
        final int _tmp_2 = entity.getAdult() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        statement.bindLong(14, entity.getAddedAt());
      }
    };
    this.__deletionAdapterOfMovieEntity = new EntityDeletionOrUpdateAdapter<MovieEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `watchlist_movies` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MovieEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMovieById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM watchlist_movies WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMovie(final MovieEntity movie, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMovieEntity.insert(movie);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMovie(final MovieEntity movie, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMovieEntity.handle(movie);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMovieById(final int movieId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMovieById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, movieId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMovieById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MovieEntity>> getAllWatchlistMovies() {
    final String _sql = "SELECT * FROM watchlist_movies ORDER BY addedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"watchlist_movies"}, new Callable<List<MovieEntity>>() {
      @Override
      @NonNull
      public List<MovieEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfPosterPath = CursorUtil.getColumnIndexOrThrow(_cursor, "posterPath");
          final int _cursorIndexOfBackdropPath = CursorUtil.getColumnIndexOrThrow(_cursor, "backdropPath");
          final int _cursorIndexOfReleaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseDate");
          final int _cursorIndexOfVoteAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "voteAverage");
          final int _cursorIndexOfVoteCount = CursorUtil.getColumnIndexOrThrow(_cursor, "voteCount");
          final int _cursorIndexOfGenreIds = CursorUtil.getColumnIndexOrThrow(_cursor, "genreIds");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfPopularity = CursorUtil.getColumnIndexOrThrow(_cursor, "popularity");
          final int _cursorIndexOfOriginalLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLanguage");
          final int _cursorIndexOfAdult = CursorUtil.getColumnIndexOrThrow(_cursor, "adult");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<MovieEntity> _result = new ArrayList<MovieEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MovieEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpOverview;
            _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            final String _tmpPosterPath;
            if (_cursor.isNull(_cursorIndexOfPosterPath)) {
              _tmpPosterPath = null;
            } else {
              _tmpPosterPath = _cursor.getString(_cursorIndexOfPosterPath);
            }
            final String _tmpBackdropPath;
            if (_cursor.isNull(_cursorIndexOfBackdropPath)) {
              _tmpBackdropPath = null;
            } else {
              _tmpBackdropPath = _cursor.getString(_cursorIndexOfBackdropPath);
            }
            final String _tmpReleaseDate;
            _tmpReleaseDate = _cursor.getString(_cursorIndexOfReleaseDate);
            final double _tmpVoteAverage;
            _tmpVoteAverage = _cursor.getDouble(_cursorIndexOfVoteAverage);
            final int _tmpVoteCount;
            _tmpVoteCount = _cursor.getInt(_cursorIndexOfVoteCount);
            final List<Integer> _tmpGenreIds;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfGenreIds);
            _tmpGenreIds = __converters.toIntList(_tmp);
            final List<Genre> _tmpGenres;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.toGenreList(_tmp_1);
            final double _tmpPopularity;
            _tmpPopularity = _cursor.getDouble(_cursorIndexOfPopularity);
            final String _tmpOriginalLanguage;
            _tmpOriginalLanguage = _cursor.getString(_cursorIndexOfOriginalLanguage);
            final boolean _tmpAdult;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAdult);
            _tmpAdult = _tmp_2 != 0;
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new MovieEntity(_tmpId,_tmpTitle,_tmpOverview,_tmpPosterPath,_tmpBackdropPath,_tmpReleaseDate,_tmpVoteAverage,_tmpVoteCount,_tmpGenreIds,_tmpGenres,_tmpPopularity,_tmpOriginalLanguage,_tmpAdult,_tmpAddedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMovieById(final int movieId,
      final Continuation<? super MovieEntity> $completion) {
    final String _sql = "SELECT * FROM watchlist_movies WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, movieId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MovieEntity>() {
      @Override
      @Nullable
      public MovieEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfPosterPath = CursorUtil.getColumnIndexOrThrow(_cursor, "posterPath");
          final int _cursorIndexOfBackdropPath = CursorUtil.getColumnIndexOrThrow(_cursor, "backdropPath");
          final int _cursorIndexOfReleaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseDate");
          final int _cursorIndexOfVoteAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "voteAverage");
          final int _cursorIndexOfVoteCount = CursorUtil.getColumnIndexOrThrow(_cursor, "voteCount");
          final int _cursorIndexOfGenreIds = CursorUtil.getColumnIndexOrThrow(_cursor, "genreIds");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfPopularity = CursorUtil.getColumnIndexOrThrow(_cursor, "popularity");
          final int _cursorIndexOfOriginalLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLanguage");
          final int _cursorIndexOfAdult = CursorUtil.getColumnIndexOrThrow(_cursor, "adult");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final MovieEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpOverview;
            _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            final String _tmpPosterPath;
            if (_cursor.isNull(_cursorIndexOfPosterPath)) {
              _tmpPosterPath = null;
            } else {
              _tmpPosterPath = _cursor.getString(_cursorIndexOfPosterPath);
            }
            final String _tmpBackdropPath;
            if (_cursor.isNull(_cursorIndexOfBackdropPath)) {
              _tmpBackdropPath = null;
            } else {
              _tmpBackdropPath = _cursor.getString(_cursorIndexOfBackdropPath);
            }
            final String _tmpReleaseDate;
            _tmpReleaseDate = _cursor.getString(_cursorIndexOfReleaseDate);
            final double _tmpVoteAverage;
            _tmpVoteAverage = _cursor.getDouble(_cursorIndexOfVoteAverage);
            final int _tmpVoteCount;
            _tmpVoteCount = _cursor.getInt(_cursorIndexOfVoteCount);
            final List<Integer> _tmpGenreIds;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfGenreIds);
            _tmpGenreIds = __converters.toIntList(_tmp);
            final List<Genre> _tmpGenres;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.toGenreList(_tmp_1);
            final double _tmpPopularity;
            _tmpPopularity = _cursor.getDouble(_cursorIndexOfPopularity);
            final String _tmpOriginalLanguage;
            _tmpOriginalLanguage = _cursor.getString(_cursorIndexOfOriginalLanguage);
            final boolean _tmpAdult;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAdult);
            _tmpAdult = _tmp_2 != 0;
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _result = new MovieEntity(_tmpId,_tmpTitle,_tmpOverview,_tmpPosterPath,_tmpBackdropPath,_tmpReleaseDate,_tmpVoteAverage,_tmpVoteCount,_tmpGenreIds,_tmpGenres,_tmpPopularity,_tmpOriginalLanguage,_tmpAdult,_tmpAddedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object isInWatchlist(final int movieId, final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, movieId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Boolean> isInWatchlistFlow(final int movieId) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, movieId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"watchlist_movies"}, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getWatchlistCount() {
    final String _sql = "SELECT COUNT(*) FROM watchlist_movies";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"watchlist_movies"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
