package com.tobykurien.androidgroovysupport.extensions

class TimeExtensions {
    static Long second(Number self) {
        seconds(self)
    }

    static Long seconds(Number self) {
        self * 1_000
    }

    static Long minute(Number self) {
        minutes(self)
    }

    static Long minutes(Number self) {
        seconds(self) * 60
    }

    static Long hour(Number self) {
        hours(self)
    }

    static Long hours(Number self) {
        minutes(self) * 60
    }

    static Long day(Number self) {
        days(self)
    }

    static Long days(Number self) {
        hours(self) * 24
    }

    static Long week(Number self) {
        weeks(self)
    }

    static Long weeks(Number self) {
        days(self) * 7
    }

    static Date ago(Number self) {
        return new Date(System.currentTimeMillis() - self)
    }

    static Date fromNow(Number self) {
        return new Date(System.currentTimeMillis() + self)
    }

    static Date now(Object self) {
        return new Date(System.currentTimeMillis())
    }

    static long plus(Date self, Date other) {
        self.time + other.time
    }

    static long minus(Date self, Date other) {
        self.time - other.time
    }

    static Date plus(Date self, Number other) {
        new Date(self.time + other)
    }

    static Date plus(Number self, Date other) {
        new Date(self + other.time)
    }

    static Date minus(Date self, Number other) {
        new Date(self.time - other)
    }

    static Date minus(Number self, Date other) {
        new Date(self - other.time)
    }
}