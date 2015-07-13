package com.tobykurien.androidgroovysupport.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import asia.sonix.android.orm.AbatisService
import groovy.transform.CompileStatic

@CompileStatic
class DbService extends AbatisService {

    protected DbService(Context context, String dbName, int version) {
        super(context, dbName, version)
    }

    public static DbService getInstance(Context context, String dbName, int dbVersion) {
        return new DbService(context, dbName, dbVersion)
    }

    @Override
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion)
    }

    /**
     * Find an object by it's id
     */
    public <T> T findById(String table, long id, Class<T> bean) {
        super.<T>executeForBean(
                "select * from ${table} where id = #id#",
                [ 'id': id ],
                bean
        )
    }

    /**
     * Find all objects in a table
     */
    public <T> List<T> findAll(String table, String orderBy, Class<T> bean) {
        this.<T>findByFields(table, null, orderBy, bean)
    }

    /**
     * Find an object by the field-value mappings specified in the Map.
     */
    public <T> List<T> findByFields(String table, Map<String, ? extends Object> values, String orderBy, Class<T> bean) {
        findByFields(table, values, orderBy, 0, 0, bean)
    }

    /**
     * Find an object by the field-value mappings specified in the Map. The Map
     * key can contain a space followed by the operator for that field.
     *
     * Sample usage:
     *
     *  // get all users sorted by surname descending
     *  db.findByFields("users", #{
     * 	}, "surname desc", 0, 0, User)
     *
     *  // get all users
     *  db.findByFields("users", null, null, 0, 0, User)
     *
     * // get first 10 users with age less than or equal to 18
     * db.findByFields("users", #{ "age <=" -> 18 }, null, 10, 0, User)
     *
     */
    public <T> List<T> findByFields(String table, Map<String, ? extends Object> values, String orderBy, long limit, long skip, Class<T> bean) {
        def sql = "select * from " + getFindByFieldsSql(table, values, orderBy)
        if (limit > 0) {
            if (skip > 0) {
                sql = sql + " limit ${skip},${limit} "
            } else {
                sql = sql + " limit ${limit} "
            }
        }

        // strip operators from the keys inside values Map (if any)
        HashMap vals = values == null ? null : new HashMap()
        values?.each { k, v ->
            if (k.indexOf(" ") > 0) {
                vals.put(k.split(" ")[0], v)
            } else {
                vals.put(k, v)
            }
        }

        super.<T>executeForBeanList(sql, vals, bean)
    }

    /**
     * Returns a partial SQL string (doesn't include "select * from " prefix) for
     * the specified parameters, that cen then be used to retrieve the data, or
     * get a count, and have a LIMIT added to the end
     */
    private String getFindByFieldsSql(String table, Map<String, ? extends Object> values, String orderBy) {
        def String where = ""
        if (values != null) {
            where = " where " + values.keySet().collect { key ->
                if (key.indexOf(" ") > 0) {
                    def keyop = key.split(" ")
                    return " ${key} #${keyop[0]}#"
                } else {
                    return "${key} = #${key}#"
                }
            }.join(" and ")
        }

        def order = ""
        if (orderBy != null && orderBy.trim().length() > 0) {
            order = "order by " + orderBy
        }

        return "${table} ${where} ${order}"
    }

    /**
     * Convert the Map object into ContentValues object
     */
    public ContentValues getContentValues(Map<String, ? extends Object> values) {
        def vals = new ContentValues()

        for (String key : values.keySet) {
            def value = values.get(key)
            if (value instanceof Date) {
                vals.put(key, (value as Date).time)
            } else {
                vals.put(key, String.valueOf(value))
            }
        }

        return vals
    }

    /**
     * Generic method to insert records into the database from a Map
     * of key-value pairs.
     * @return the id of the inserted row
     */
    public insert(String table, Map<String, ? extends Object> values) {
        def db = writableDatabase
        try {
            return db.insert(table, "", getContentValues(values))
        } finally {
            db.close()
        }
    }

    /**
     * Generic method to update a record with the given id in the database from a Map
     * of key-value pairs.
     * @return the number of rows affected
     */
    public update(String table, Map<String, ?> values, long id) {
        update(table, values, String.valueOf(id))
    }

    public update(String table, Map<String, ? extends Object> values, String id) {
        def db = writableDatabase
        try {
            return db.update(table, getContentValues(values), "id = ?", [ id ] as String[])
        } finally {
            db.close()
        }
    }

    /**
     * Delete the specified row from the specified table
     * @return the number of rows affected
     */
    public delete(String table, String id) {
        def db = writableDatabase
        try {
            return db.delete(table, "id = ?", [id] as String[])
        } finally {
            db.close()
        }
    }

    /**
     * Delete all rows from the specified table
     * @return the number of rows affected
     */
    public delete(String table) {
        def db = writableDatabase
        try {
            return db.delete(table, "1", null)
        } finally {
            db.close()
        }
    }

    /**
     * Find all objects from a db table. Return a lazy-loading list for large results.
     * Good for using in an Adapter (sequential access), but terrible for random access.
     */
//    public <T> LazyList<T> lazyFindAll(String table, String orderBy, Class<T> bean) {
//        def sql = getFindByFieldsSql(table, null, orderBy)
//        new LazyList<T>(sql, null, this, bean)
//    }

    /**
     * Like findByFields() but returns a lazy-loading list for large results.
     * Good for using in an Adapter (sequential access), but terrible for random access.
     */
//    def <T> LazyList<T> lazyFindByFields(String table, Map<String, ? extends Object> values,
//                                         String orderBy, Class<T> bean) {
//        def sql = getFindByFieldsSql(table, values, orderBy)
//
//        // strip operators from the keys inside values Map (if any)
//        def vals = if (values == null) null else newHashMap()
//        values?.each  [k,v->
//        if (k.indexOf(" ") > 0) {
//            vals.put(k.split(" ").get(0), v)
//        } else {
//            vals.put(k,v)
//        }
//        ]
//
//        new LazyList<T>(sql, vals, this, bean)
//    }
}