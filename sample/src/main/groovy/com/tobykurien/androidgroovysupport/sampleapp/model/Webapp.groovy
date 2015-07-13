package com.tobykurien.androidgroovysupport.sampleapp.model

import groovy.transform.Canonical

@Canonical
class Webapp {
    long id
    String name
    String url
    String iconUrl
}