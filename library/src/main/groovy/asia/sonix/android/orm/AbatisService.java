package asia.sonix.android.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Android向けのO/RMを提供します。
 * 
 * @author sonix - http://www.sonix.asia
 * @since JDK1.5 Android Level 3
 * 
 * Forked from http://code.google.com/p/abatis/
 * Modified by Toby Kurien
 * Modifications: 
 *  - changed "initialize" to "dbInitialize" and allowed multiple statements
 *  - changed methods to take R.string.xxxx instead of "xxxx"
 *  - added version numbers to constructors for db versioning
 *  - protected the getInstance() methods and constructor. 
 *    Projects should derive a subclass with a getInstance() singleton method
 *  - added toSqlString to allow sanitizing SQL strings
 *  - improved error messages
 *  - several bug fixes
 *
  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
       http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
public class AbatisService extends SQLiteOpenHelper {
   /**
    * Debug TAG名
    */
   private static final String TAG = "aBatis";

   /**
    * DBを初期化するSQLID
    */
   private static final String INIT_CREATE_SQL = "dbInitialize";
   /**
    * Default DB file name
    */
   private static final String DB_FILE_NAME = "database.db";

   /**
    * 自分のinstance object
    */
   private static AbatisService instance = null;

   /**
    * SQLiteDatabase object
    */
   private SQLiteDatabase dbObj;

   /**
    * Context object
    */
   private Context context;

   // show SQL as debug output
   protected boolean showSQL = false;
   
   /**
    * Default DB file nameを利用するConstructor
    * 
    * @param context
    *           呼び出し元Contextオブジェクト
    * 
    */
   protected AbatisService(Context context, int version) {
      super(context, DB_FILE_NAME, null, version);
      this.context = context;
   }

   /**
    * 指定DB file nameを利用するConstructor
    * 
    * @param context
    *           呼び出し元Contextオブジェクト
    * @param dbName
    *           生成するDB file name
    * 
    */
   protected AbatisService(Context context, String dbName, int version) {
      super(context, dbName.concat(".db"), null, version);
      this.context = context;
   }

   /**
    * Default DB file nameを利用する外部Constructor
    * 
    * @param context
    *           呼び出し元Contextオブジェクト
    * @param dbName
    *           生成するDB file name
    * 
    */
   protected static AbatisService getInstance(Context context, int version) {
      if (instance == null) {
         instance = new AbatisService(context, version);
      }
      return instance;
   }

   /**
    * 指定DB file nameを利用する外部Constructor
    * 
    * @param context
    *           呼び出し元Contextオブジェクト
    * @param dbName
    *           生成するDB file name
    * 
    */
   protected static AbatisService getInstance(Context context, String dbName, int version) {
      if (instance == null) {
         instance = new AbatisService(context, dbName, version);
      }
      return instance;
   }

   /**
    * DB connector
    * 
    * @param db
    *           SQLiteDatabase object
    * 
    */
   @Override
   public void onCreate(SQLiteDatabase db) {
      int pointer = context.getResources().getIdentifier(INIT_CREATE_SQL, "string", context.getPackageName());
      if (pointer == 0) {
         Log.e(TAG, "undefined sql id - " + INIT_CREATE_SQL);
      } else {
         String createTabelSql = context.getResources().getString(pointer);
         for (String sql : createTabelSql.split(";")) {
            db.execSQL(sql);
         }
      }
   }

   /**
    * for upgrade (バージョン0.1では実装されていない)
    * 
    * @param db
    *           SQLiteDatabase object
    * @param oldVersion
    *           old version value
    * @param newVersion
    *           new version value
    * 
    */
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // no poc
   }

   /**
    * 指定したSQLIDにparameterをmappingして、クエリする。結果mapを返却。
    * 
    * <p>
    * mappingの時、parameterが足りない場合はnullを返す。 また、結果がない場合nullを返す。
    * </p>
    * 
    * @param sqlId
    *           SQLID
    * @param bindParams
    *           sql parameter
    * 
    * @return Map<String, Object> result
    */
   public Map<String, Object> executeForMap(int sqlId, Map<String, ? extends Object> bindParams) {
      String sql = context.getResources().getString(sqlId);
      return executeForMap(sql, bindParams);
   }
   
   public Map<String, Object> executeForMap(String sql, Map<String, ? extends Object> bindParams) {
      Map<String, Object> map = null;
      getDbObject();
      try {
         if (bindParams != null) {
            Iterator<String> mapIterator = bindParams.keySet().iterator();
            while (mapIterator.hasNext()) {
               String key = mapIterator.next();
               Object value = bindParams.get(key);
               sql = sql.replaceAll("#" + key + "#", toSqlString(value));
            }
         }
         if (sql.indexOf('#') != -1) {
            Log.e(TAG, "undefined parameter in sql: " + sql);
            return map;
         }
         if (showSQL) Log.d(TAG, sql);
         Cursor cursor = dbObj.rawQuery(sql, null);
         if (cursor == null) { return map; }
         String[] columnNames = cursor.getColumnNames();
         if (cursor.moveToNext()) {
            map = new HashMap<String, Object>();
            int i = 0;
            for (String columnName : columnNames) {
               map.put(columnName, cursor.getString(i));
               i++;
            }
         }
         cursor.close();
         return map;
      } finally {
         dbObj.close();
      }
   }

   /**
    * 指定したSQLIDにparameterをmappingして、クエリする。結果mapをリストで返却。
    * 
    * <p>
    * mappingの時、parameterが足りない場合はnullを返す。
    * </p>
    * 
    * @param sqlId
    *           SQLID
    * @param bindParams
    *           sql parameter
    * 
    * @return List<Map<String, Object>> result
    */
   public List<Map<String, Object>> executeForMapList(int sqlId, Map<String, ? extends Object> bindParams) {
      String sql = context.getResources().getString(sqlId);
      return executeForMapList(sql, bindParams);
   }
   
   public List<Map<String, Object>> executeForMapList(String sql, Map<String, ? extends Object> bindParams) {
      List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
      getDbObject();
      try {
         if (bindParams != null) {
            Iterator<String> mapIterator = bindParams.keySet().iterator();
            while (mapIterator.hasNext()) {
               String key = mapIterator.next();
               Object value = bindParams.get(key);
               sql = sql.replaceAll("#" + key + "#", toSqlString(value));
            }
         }
         if (sql.indexOf('#') != -1) {
            Log.e(TAG, "undefined parameter in sql: " + sql);
            return mapList;
         }
         if (showSQL) Log.d(TAG, sql);
         Cursor cursor = dbObj.rawQuery(sql, null);
         if (cursor == null) { return mapList; }
         String[] columnNames = cursor.getColumnNames();
         while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            int i = 0;
            for (String columnName : columnNames) {
               map.put(columnName, cursor.getString(i));
               i++;
            }
            mapList.add(map);
         }
         cursor.close();
         return mapList;
      } finally {
         dbObj.close();
      }
   }

   /**
    * 指定したSQLIDにparameterをmappingして、クエリする。結果beanで返却。
    * 
    * <p>
    * mappingの時、parameterが足りない場合はnullを返す。 また、結果がない場合nullを返す。
    * </p>
    * 
    * @param sqlId
    *           SQLID
    * @param bindParams
    *           sql parameter
    * @param bean
    *           bean class of result
    * 
    * @return List<Map<String, Object>> result
    */
   @SuppressWarnings({ "rawtypes" })
   public <T> T executeForBean(int sqlId, Map<String, ? extends Object> bindParams, Class bean) {
      String sql = context.getResources().getString(sqlId);
      return executeForBean(sql, bindParams, bean);
   }
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public <T> T executeForBean(String sql, Map<String, ? extends Object> bindParams, Class bean) {
      T beanObj = null;
      getDbObject();
      try {
         if (bindParams != null) {
            Iterator<String> mapIterator = bindParams.keySet().iterator();
            while (mapIterator.hasNext()) {
               String key = mapIterator.next();
               Object value = bindParams.get(key);
               sql = sql.replaceAll("#" + key + "#", toSqlString(value));
            }
         }
         if (sql.indexOf('#') != -1) {
            Log.e(TAG, "undefined parameter in sql: " + sql);
            return beanObj;
         }
         if (showSQL) Log.d(TAG, sql);
         Cursor cursor = dbObj.rawQuery(sql, null);
         if (cursor == null) { return beanObj; }
         String[] columnNames = cursor.getColumnNames();
         List<String> dataNames = new ArrayList<String>();
         for (String columnName : columnNames) {
            dataNames.add(chgDataName(columnName));
         }
         // get bean class package
         Package beanPackage = bean.getPackage();
         if (cursor.moveToNext()) {
            try {
               beanObj = (T) parse(cursor, bean, beanPackage.getName(), bean.newInstance());
            } catch (Exception e) {
               Log.d(TAG, e.toString());
            }
         }
         cursor.close();
         return beanObj;
      } finally {
         dbObj.close();
      }
   }

   /**
    * 指定したSQLIDにparameterをmappingして、クエリする。結果beanをリストで返却。
    * 
    * <p>
    * mappingの時、parameterが足りない場合はnullを返す。
    * </p>
    * 
    * @param sqlId
    *           SQLID
    * @param bindParams
    *           sql parameter
    * @param bean
    *           bean class of result
    * 
    * @return List<Map<String, Object>> result
    */
   @SuppressWarnings({ "rawtypes" })
   public <T> List<T> executeForBeanList(int sqlId, Map<String, ? extends Object> bindParams, Class bean) {
      String sql = context.getResources().getString(sqlId);
      return executeForBeanList(sql, bindParams, bean);
   }
   
   @SuppressWarnings({ "rawtypes" })
   public <T> List<T> executeForBeanList(String sql, Map<String, ? extends Object> bindParams, Class bean) {
	   return executeForBeanList(sql, bindParams, bean, null);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   /**
    * Execute a query and return a List of beans populated with the data from the database
    * with column names matching the fields of the speified bean. A buffer of pre-instantiated
    * beans can be passed in to avoid object instantiation, for better performance.
    * @param sql - the SQL query to execute with placeholders like #id# and #name#
    * @param bindParams - the values for the placeholders in the SQL
    * @param bean - the class of the bean into which data will be stored
    * @param objectList - an optional buffer of pre-instantiated beans to populate
    * @return
    */
   public <T> List<T> executeForBeanList(String sql, Map<String, ? extends Object> bindParams, 
		   Class bean, List<T> objectList) {
	  
	  if (objectList == null) {
		  objectList = new ArrayList<T>();
	  }
	   
      getDbObject();
      try {
         if (bindParams != null) {
            Iterator<String> mapIterator = bindParams.keySet().iterator();
            while (mapIterator.hasNext()) {
               String key = mapIterator.next();
               Object value = bindParams.get(key);
               sql = sql.replaceAll("#" + key + "#", toSqlString(value));
            }
         }
         if (sql.indexOf('#') != -1) {
            Log.e(TAG, "undefined parameter in sql: " + sql);
            return objectList;
         }
         if (showSQL) Log.d(TAG, sql);
         Cursor cursor = dbObj.rawQuery(sql, null);
         if (cursor == null) { return objectList; }
         String[] columnNames = cursor.getColumnNames();
         List<String> dataNames = new ArrayList<String>();
         for (String columnName : columnNames) {
            dataNames.add(chgDataName(columnName));
         }
         
         // get bean class package
         T beanObj = null;
         Package beanPackage = bean.getPackage();
         int i = 0;
         while (cursor.moveToNext()) {
            try {
               if (objectList.size() <= i) {
            	   // create new object and add to list
            	   objectList.add((T) bean.newInstance());
               }
               beanObj = objectList.get(i++);
               parse(cursor, bean, beanPackage.getName(), beanObj);
            } catch (Exception e) {
               Log.e(TAG, e.toString(), e);
            }
         }
         cursor.close();
         return objectList;
      } finally {
         dbObj.close();
      }
   }
   
   /**
    * 指定したSQLIDにparameterをmappingして、実行する。
    * 
    * <p>
    * mappingの時、parameterが足りない場合は0を返す。
    * </p>
    * 
    * @param sqlId
    *           SQLiteDatabase object
    * @param bindParams
    *           old version value
    * 
    * @return int 実行によって影響をもらった行数
    */
   public int execute(int sqlId, Map<String, ? extends Object> bindParams) {
      String sql = context.getResources().getString(sqlId);
      return execute(sql, bindParams);
   }
   
   public int execute(String sql, Map<String, ? extends Object> bindParams) {
      getDbObject();
      try {
         int row = 0;
         if (bindParams != null) {
            Iterator<String> mapIterator = bindParams.keySet().iterator();
            while (mapIterator.hasNext()) {
               String key = mapIterator.next();
               Object value = bindParams.get(key);
               sql = sql.replaceAll("#" + key + "#", toSqlString(value));
            }
         }
         if (sql.indexOf('#') != -1) {
            Log.e(TAG, "undefined parameter in sql: " + sql);
            return row;
         }
         try {
            if (showSQL) Log.d(TAG, sql);
            dbObj.execSQL(sql);
            row += 1;
         } catch (SQLException e) {
            return row;
         }
         return row;
      } finally {
         dbObj.close();
      }
   }

   /**
    * SQLiteDatabase Objectを取得する。
    * 
    * @return SQLiteDatabase SQLiteDatabase Object
    */
   private SQLiteDatabase getDbObject() {
      if (dbObj == null || !dbObj.isOpen()) {
         dbObj = getWritableDatabase();
      }
      return dbObj;
   }

   /**
    * JsonStringからBeanに変換する。
    * 
    * @param jsonStr
    *           JSON String
    * @param beanClass
    *           Bean class
    * @param basePackage
    *           Base package name which includes all Bean classes
    * @return Object Bean
    * @throws Exception
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public Object parse(Cursor cursor, Class beanClass, String basePackage, Object obj) throws Exception {
      //Object obj = null;
      // Check bean object
      if (beanClass == null) {
         Log.d(TAG, "Bean class is null");
         return null;
      }
      // Read Class member fields
      Field[] props = beanClass.getDeclaredFields();
      if (props == null || props.length == 0) {
         Log.d(TAG, "Class" + beanClass.getName() + " has no fields");
         return null;
      }
      // Create instance of this Bean class
      if (obj == null) obj = beanClass.newInstance();
      
      // Set value of each member variable of this object
      for (int i = 0; i < props.length; i++) {
         String fieldName = props[i].getName();
         fieldName = fieldName.replaceAll("_", "");
         if (cursor.getColumnIndex(fieldName) < 0) continue;

         // Skip public and static fields
         if (props[i].getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)) {
            continue;
         }
         // Date Type of Field
         Class type = props[i].getType();
         String typeName = type.getName();
         // Check for Custom type
         if (typeName.equals("int") || typeName.equals("java.lang.Integer")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               // Set value
               m.invoke(obj, cursor.getInt(cursor.getColumnIndex(fieldName)));
            } catch (Exception ex) {
               Log.d(TAG, ex.getMessage());
            }
         } else if (typeName.equals("long") || typeName.equals("java.lang.Long")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               // Set value
               m.invoke(obj, cursor.getLong(cursor.getColumnIndex(fieldName)));
            } catch (Exception ex) {
               Log.d(TAG, ex.getMessage());
            }
         } else if (typeName.equals("boolean") || typeName.equals("java.lang.Boolean")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               // Set value
               String val = cursor.getString(cursor.getColumnIndex(fieldName));
               m.invoke(obj, val.equalsIgnoreCase("true"));
            } catch (Exception ex) {
               Log.d(TAG, ex.getMessage());
            }
         } else if (typeName.equals("java.lang.String")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               // Set value
               m.invoke(obj, cursor.getString(cursor.getColumnIndex(fieldName)));
            } catch (Exception ex) {
               Log.d(TAG, ex.getMessage());
            }
         } else if (typeName.equals("double") || typeName.equals("java.lang.Double")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               // Set value
               m.invoke(obj, cursor.getDouble(cursor.getColumnIndex(fieldName)));
            } catch (Exception ex) {
               Log.d(TAG, ex.getMessage());
            }
         } else if (typeName.equals("java.util.Date")) {
            Class[] parms = { type };
            try {
               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
               m.setAccessible(true);
               Long dateVal = cursor.getLong((cursor.getColumnIndex(fieldName)));
               if (cursor.isNull(cursor.getColumnIndex(fieldName)) || dateVal == 0) {
                  m.invoke(obj, new Object[]{null});
               } else {
                  m.invoke(obj, new Date(dateVal));
               }
            } catch (Exception ex) {
               Log.d(TAG, "error: " + ex.getMessage());
            }
//         } else if (type.getName().equals(List.class.getName()) || type.getName().equals(ArrayList.class.getName())) {
//            // Find out the Generic
//            String generic = props[i].getGenericType().toString();
//            if (generic.indexOf("<") != -1) {
//               String genericType = generic.substring(generic.lastIndexOf("<") + 1, generic.lastIndexOf(">"));
//               if (genericType != null) {
//                  JSONArray array = null;
//                  try {
//                     array = jsonObj.getJSONArray(fieldName);
//                  } catch (Exception ex) {
//                     Log.d(TAG, ex.getMessage());
//                     array = null;
//                  }
//                  if (array == null) {
//                     continue;
//                  }
//                  ArrayList arrayList = new ArrayList();
//                  for (int j = 0; j < array.length(); j++) {
//                     arrayList.add(parse(array.getJSONObject(j).toString(), Class.forName(genericType), basePackage));
//                  }
//                  // Set value
//                  Class[] parms = { type };
//                  try {
//                     Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
//                     m.setAccessible(true);
//                     m.invoke(obj, arrayList);
//                  } catch (Exception ex) {
//                     Log.d(TAG, ex.getMessage());
//                  }
//               }
//            } else {
//               // No generic defined
//               generic = null;
//            }
//         } else if (typeName.startsWith(basePackage)) {
//            Class[] parms = { type };
//            try {
//               Method m = beanClass.getDeclaredMethod(getBeanMethodName(fieldName, 1), parms);
//               m.setAccessible(true);
//               // Set value
//               JSONObject customObj = jsonObj.getJSONObject(fieldName);
//               if (customObj != null) {
//                  m.invoke(obj, parse(customObj.toString(), type, basePackage));
//               }
//            } catch (JSONException ex) {
//               Log.d(TAG, ex.getMessage());
//            }
         } else {
            // Skip
            Log.d(TAG, "Field " + fieldName + "#" + typeName + " is skip");
         }
      }
      return obj;
   }

   /**
    * BeanClass fields名からmethod名を取得する。
    * 
    * @param fieldName
    * @param type
    * @return String MethodName
    */
   private String getBeanMethodName(String fieldName, int type) {
      if (fieldName == null || fieldName == "") { return ""; }
      String methodName = "";
      if (type == 0) {
         methodName = "get";
      } else {
         methodName = "set";
      }
      methodName += fieldName.substring(0, 1).toUpperCase();
      if (fieldName.length() == 1) { return methodName; }
      methodName += fieldName.substring(1);
      //Log.d(TAG, "fieldName: " + fieldName + " beanMethod: " + methodName);
      return methodName;
   }

   /**
    * Databaseカラム名をjava bean名に変換する。
    * 
    * @param targetStr
    *           databaseカラム名
    * @return String bean data名
    */
   private String chgDataName(String targetStr) {
      Pattern p = Pattern.compile("_([a-z])");
      Matcher m = p.matcher(targetStr);

      StringBuffer sb = new StringBuffer(targetStr.length());
      while (m.find()) {
         m.appendReplacement(sb, m.group(1).toUpperCase());
      }
      m.appendTail(sb);
      return sb.toString();
   }
   
   /**
    * Convert value object to sanitized SQL string
    * @param value - the value object
    * @return a string for use in an SQL statement
    * @author Toby Kurien
    */
   public String toSqlString(Object value) {
     if (value == null) {
        return "null";
     }
      
     String val = String.valueOf(value);

     if (value instanceof Integer ||
         value instanceof Float ||
         value instanceof Double ||
         value instanceof Long) {
       return val;
     } else if (value instanceof Boolean) {
        return "'" + ((Boolean) value ? "true" : "false") + "'";
     } else if (value instanceof Date) {
        return String.valueOf(((Date) value).getTime());
     } else {
       return "'" + val + "'"; // TODO escape special characters in val here
     }
   }    
}