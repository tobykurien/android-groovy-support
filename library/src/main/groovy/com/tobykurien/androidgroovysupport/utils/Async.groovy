package com.tobykurien.androidgroovysupport.utils

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SimpleType

import java.util.concurrent.TimeUnit

/**
 * An implementation of {@link android.os.AsyncTask} which makes it easy to deal with
 * requests/callbacks using Groovy closures
 *
 * Modified from: https://gist.github.com/karfunkel/6eba3c237890f90c2779
 * Originally from: https://gist.github.com/melix/355185ffbc1332952cc8
 */
@CompileStatic
public class Async<Result, Progress> extends AsyncTask<Object, Progress, Result> {
    private Closure<Result> request
    private Closure<Void> first
    private Closure<Void> then
    private Closure<Void> error
    private Closure<Void> cancelledClosure
    private Closure progress
    boolean skipPostExecute = false

    @Override
    protected Result doInBackground(Object... params) {
        if (request) try {
            return request(params)
        } catch (Exception e) {
            skipPostExecute = true
            onError(e)
            return null
        }
    }

    @Override
    protected void onPreExecute() {
        if (continueTask() && first) {
            try {
                first()
            } catch (Exception e) {
                onError(e)
            }
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (continueTask() && !skipPostExecute && then) {
            try {
                then(result)
            } catch (Exception e) {
                onError(e)
            }
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        if (continueTask() && progress) {
            try {
                progress.call(values.asType(progress.getParameterTypes().first()))
            } catch (Exception e) {
                onError(e)
            }
        }
    }

    @Override
    protected void onCancelled(Result result) {
        if (continueTask() && cancelledClosure) {
            try {
                cancelledClosure(result)
            } catch (Exception e) {
                onError(e)
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (continueTask() && cancelledClosure) {
            try {
                cancelledClosure()
            } catch (Exception e) {
                onError(e)
            }
        }
    }

    protected final void progress(Progress... values) {
        publishProgress(values)
    }

    boolean continueTask() {
        !isCancelled()
    }

    private void onError(Exception e) {
        if (continueTask() && error) {
            try {
                error(e)
            } catch (Exception e2) {
                Log.e("Async", "Error executing error closure", e2)
            }
        } else {
            Log.e("Async", "Error executing AsyncTask", e)
        }
    }

    static class FluentAsyncTaskBuilder<Result, Progress> {
        Async<Result, Progress> task

        FluentAsyncTaskBuilder(Async<Result, Progress> task) {
            this.task = task
        }

        private FluentAsyncTaskBuilder<Result, Progress> from(@DelegatesTo(value = Async, strategy = Closure.OWNER_FIRST) Closure<Result> request) {
            request.delegate = task
            task.request = request
            return this
        }

        FluentAsyncTaskBuilder<Result, ?> onProgress(@DelegatesTo(value = Async, strategy = Closure.OWNER_FIRST) Closure progress) {
            progress.delegate = task
            task.progress = progress
            return this
        }

        FluentAsyncTaskBuilder<Result, ?> first(@DelegatesTo(value = Async, strategy = Closure.OWNER_FIRST) Closure first) {
            first.delegate = task
            task.first = first
            return this
        }

        FluentAsyncTaskBuilder<Result, ?> then(Closure<Void> then) {
            then.delegate = task
            task.then = then
            return this
        }

        FluentAsyncTaskBuilder<Result, ?> onError(
                @ClosureParams(value=SimpleType.class, options="java.lang.Exception") Closure<Void> onError) {
            onError.delegate = task
            task.error = onError
            return this
        }

        FluentAsyncTaskBuilder<Result, ?> onCancelled(Closure cancelled) {
            cancelled.delegate = task
            task.cancelledClosure = cancelled
            return this
        }

        AsyncTask<Object, Progress, Result> execute(Object... params) {
            task.skipPostExecute = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // newer versions of Android use a single thread, rather default to multiple threads
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params)
            } else {
                // older versions of Android already use a thread pool
                task.execute(params)
            }
        }

        Result get() {
            task.get()
        }

        Result get(long timeout, TimeUnit unit) {
            task.get(timeout, unit)
        }
    }

    static <Result, Progress> FluentAsyncTaskBuilder<Result, Progress> background(@DelegatesTo(value = Async, strategy = Closure.OWNER_FIRST) Closure<Result> request) {
        Async<Result, Progress> task = new Async<Result, Progress>()
        return new FluentAsyncTaskBuilder<Result, Progress>(task).from(request)
    }
}
