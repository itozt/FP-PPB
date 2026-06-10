package com.example.moviecatalogue.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MovieDatabase_Impl extends MovieDatabase {
  private volatile MovieDao _movieDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `watchlist_movies` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `overview` TEXT NOT NULL, `posterPath` TEXT, `backdropPath` TEXT, `releaseDate` TEXT NOT NULL, `voteAverage` REAL NOT NULL, `voteCount` INTEGER NOT NULL, `genreIds` TEXT NOT NULL, `genres` TEXT NOT NULL, `popularity` REAL NOT NULL, `originalLanguage` TEXT NOT NULL, `adult` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '028008300c541398caf43053c1480594')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `watchlist_movies`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWatchlistMovies = new HashMap<String, TableInfo.Column>(14);
        _columnsWatchlistMovies.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("overview", new TableInfo.Column("overview", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("posterPath", new TableInfo.Column("posterPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("backdropPath", new TableInfo.Column("backdropPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("releaseDate", new TableInfo.Column("releaseDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("voteAverage", new TableInfo.Column("voteAverage", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("voteCount", new TableInfo.Column("voteCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("genreIds", new TableInfo.Column("genreIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("genres", new TableInfo.Column("genres", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("popularity", new TableInfo.Column("popularity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("originalLanguage", new TableInfo.Column("originalLanguage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("adult", new TableInfo.Column("adult", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWatchlistMovies.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWatchlistMovies = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWatchlistMovies = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWatchlistMovies = new TableInfo("watchlist_movies", _columnsWatchlistMovies, _foreignKeysWatchlistMovies, _indicesWatchlistMovies);
        final TableInfo _existingWatchlistMovies = TableInfo.read(db, "watchlist_movies");
        if (!_infoWatchlistMovies.equals(_existingWatchlistMovies)) {
          return new RoomOpenHelper.ValidationResult(false, "watchlist_movies(com.example.moviecatalogue.data.local.MovieEntity).\n"
                  + " Expected:\n" + _infoWatchlistMovies + "\n"
                  + " Found:\n" + _existingWatchlistMovies);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "028008300c541398caf43053c1480594", "df99605ec202adf56069ff10a88da72e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "watchlist_movies");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `watchlist_movies`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MovieDao.class, MovieDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MovieDao movieDao() {
    if (_movieDao != null) {
      return _movieDao;
    } else {
      synchronized(this) {
        if(_movieDao == null) {
          _movieDao = new MovieDao_Impl(this);
        }
        return _movieDao;
      }
    }
  }
}
