# android-groovy-support

A support library to leverage Android development using Groovy. This is an attempt to port the features of [Xtendroid][] to Groovy. Fully supported by Android Studio with it's built-in support for Groovy.

# AlertUtils

Implementing the ```AlertUtils``` trait allows you to write this:

```groovy
    view.findViewById(R.id.btnExit).onClickListener = {v ->
        confirm("Are you sure you want to exit?") {
            toast("ok, bye!")
            finish()
        }
    }
```

# BgTask

The BgTask class allows easy use of Android's ```AsyncTask``` using Groovy's closures:

```groovy
    // run a background task
    new BgTask<String>().runInBg({
        Thread.sleep(5_000)
        return "Back from background thread"
    }, { result ->
        toast(result)
    }, { error ->
        toast(error.message)
    })
```

BgTask takes care of running each closure in the correct thread, handling errors, and aborting the UI thread closures if the task has been cancelled.

More to come...

   [Xtendroid]: https://github.com/tobykurien/Xtendroid
