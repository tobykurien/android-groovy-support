package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tobykurien.androidgroovysupport.utils.AlertUtils
import com.tobykurien.androidgroovysupport.utils.BgTask
import groovy.transform.CompileStatic

@CompileStatic
class MainActivityFragment extends Fragment implements AlertUtils {
    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        def button = view.findViewById(R.id.btnExit)
        button.enabled = false
        button.onClickListener = {v ->
            confirm("Are you sure you want to exit?") {
                activity.finish()
            }
        }

        // run a background task that returns a string
        new BgTask<String>().runInBg({
            // This runs in a background thread
            Thread.sleep(5_000)
            return "Back from background thread"
        }, { result ->
            // This runs in the UI thread
            button.enabled = true
            toast(result)
        }, { error ->
            // This runs in the UI thread if an error occurs during background processing
            toast(error.message)
        })
    }
}