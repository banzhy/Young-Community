package com.example.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.libcommon.AppGlobals;

/*
* 普通类需要实现RoomDatabase的几个方法，
* 但是可以通过设置为抽象类abstract class，来避免复写这几个方法
* 因为Room数据库通过注解来实现相关的功能，在编译时通过annotationProcessor来实现其相关实现类
* 所以写成abstract class CacheDataBase，在运行时会生成CacheDataBase的实现类
* 这样就不用复写RoomDatabase的几个默认的方法
* */
// 主要提供缓存能力
@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDataBase extends RoomDatabase {

    private static final CacheDataBase database;

    // 初始化
    static {
        // 创建一个内存数据库
        // 但是这种数据库的数据只存在于内存中，也就是进程被杀之后，数据随之丢失
        // 所以这种方法需要谨慎选择
        // Room.inMemoryDatabaseBuilder();

        // 所以一般通过下列方法创建一个数据库实例
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDataBase.class, "ppjoke_cache")
                // 是否允许在主线程进行查询(默认false，在主线程操作会抛出异常)
                .allowMainThreadQueries()
                // 数据库创建和打开后的回调
                // .addCallback()
                // 设置查询的线程池
                // .setQueryExecutor()
                // 对SqlLiteOpenHelper创建时提供的工厂类
                // .openHelperFactory()
                // room的日志模式
                // .setJournalMode()
                // 版本更新发生异常时，进行回滚，它会重新创建数据库（删除数据重新创建）
                // .fallbackToDestructiveMigration()
                // 指定一个int数组进行回滚，从指定的之前版本中恢复
                // .fallbackToDestructiveMigrationFrom()
                // 数据库升级操作的入口（若不配置，升级到最新版本时会清空数据，然后重建）
                // .addMigrations(CacheDataBase.sMigration)
                .build();
    }

    public static CacheDataBase getDatabase() {
        return database;
    }

    public abstract CacheDao getCache();

    /*static Migration sMigration = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table teacher rename to student");
            database.execSQL("alter table teacher add column teacher_age INTEGER NOT NULL default 0");
        }
    };*/
}
