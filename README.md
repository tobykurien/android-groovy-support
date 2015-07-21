# android-groovy-support

A support library to leverage Android development using Groovy. This is an attempt to port the features of [Xtendroid][] to Groovy. In order to maintain performance, ```@CompileStatic``` is used to ensure that the Groovy code is statically compiled, rather than being run dynamically.

# Activity and Fragment extensions

The ```toast```, ```toastShort```, and ```confirm``` methods are appended to Activities and Fragments as an extension, allowing you to write code such as:

```groovy
    findViewById(R.id.btnExit).onClickListener = {v ->
        confirm("Are you sure you want to exit?") {
            toast("ok, bye!")
            finish()
        }
    }
```

# Time extensions

The time extensions make it easier to work with ```java.util.Date``` objects, as in the example below:

```groovy
var Date yesterday = 24.hours().ago()
var Date tomorrow = 24.hours().fromNow()
var Date futureDate = now + 48.days() + 20.hours() + 2.seconds()
if (futureDate - now() < 24.hours()) {
    // we are in the future!
}
```

# Async

The ```Async``` class allows easy use of Android's ```AsyncTask``` using Groovy's closures (it is based on [this][fluent]):

```groovy
    // run a background task
    Async.background {
        // This closure runs in a background thread, all other closures run in UI thread
        Thread.sleep(2_000)
        progress(100) // update progress
        return "I'm done!"
    } first {
        // optional closure to run onPreExecute()
        textview.setText("Loading...")
    } then { String result ->
        // optional closure for onPostExecute()
        textview.setText(result)
    } onProgress { Object[] progress ->
        // optional progress update
    } onCancelled {
        // optional closure to run when task is cancelled
    } onError { error ->
        // optional closure to handle any errors in the UI thread
        toast("ERROR! ${error.class.name} ${error.message}")
    } execute() // execute the task
```

Async takes care of running each closure in the correct thread, handling errors, and aborting the UI thread closures if the task has been cancelled.

# DbService

A light-weight ORM solution is implemented by utilising the Abatis project (a fork of which is included).

## Step 1: Initialize the database

In a string resource file (e.g. ```res/values/sqlmaps.xml```), initialize the database using the ```dbInitialize``` string name. Multiple statements can be included, separated by ```;``` (including ```INSERT``` statements:

```xml
<resources>
    <string name="dbInitialize">
        create table users (
           id integer primary key,
           firstName text not null,
           lastName text not null,
           age number
        );

        insert into users (firstName, lastName, age) values (\'John\', \'Smith\', 25);
    </string>

    <string name="dbGetOlderThan">
      select * from users
      where age > #age#
      order by age asc
    </string>
</resources>
```

## Step 2: Model

Create a data model (bean) for your data:

```groovy
@Canonical
class User {
    long id
    String firstName
    String lastName
    int age
}
```

## Step 3: DbService class

The ```DbService``` class can be sub-classed, or used directly to perform CRUD operations:

```groovy
def db = DbService.getInstance(activity, "dbname", 1)

// get all users order by lastName
def users = db.findAll("users", "lastName asc", User)
users.each { user ->
   Log.d("db", "Got user: " + user)
}

// get all users older than 18 (uses SQL defined above)
def adults = db.executeForBeanList(R.string.dbGetOlderThan,
   [ age: 18 ], User)

adults.each { adult ->
   Log.d("db", "Got user: " + adult)
}

// alternative to above without defining an SQL string
adults = db.findByFields("users", [ 'age >': 18 ],
    "age asc", User)

// can also do paging by specifying a limit and offset, e.g.
// get top 6 to top 10 users 18 or younger
adults = db.findByFields("users", [ 'age <=': 18 ], "age desc",
    5, 5, User)

// insert a record
def johnId = db.insert("users", [
   firstName: 'John',
   lastName: 'Doe',
   age: 43
])

// get back this user
def john = db.findById("users", johnId, User)
toast("Hi " + john)

// update this user
db.update("users", [lastName: 'Smith'], johnId)

// delete this user
db.delete("users", johnId)
```

# Work in progress

More to come...

   [Xtendroid]: https://github.com/tobykurien/Xtendroid
   [fluent]: https://gist.github.com/melix/355185ffbc1332952cc8
