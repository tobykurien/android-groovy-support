package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tobykurien.androidgroovysupport.utils.AlertUtils
import com.tobykurien.androidgroovysupport.utils.BgTask

class MainActivityFragment extends Fragment implements AlertUtils {
    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @Override
    void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState)

        view.findViewById(R.id.btnExit).onClickListener = {v ->
            confirm("Are you sure you want to exit?") {
                toast("ok, bye!")
                finish()
            }
        }

        // run a background task
        new BgTask<String>().runInBg({
            Thread.sleep(5_000)
            return "Back from background thread"
        }, { result ->
            toast(result)
        }, { error ->
            toast(error.message)
        })
    }
}