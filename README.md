EasyRest
========

EasyRest is an extension to Spring-Android restTemplate that allows making rest calls using generics. it features arbitrary caching (IE: Dear server, I don't care about your opinion, I already have this data, so I'm gonna use it, because I prefer to show something to the user, than nothing!), deferred calls (IE: I already have this data, so I will send this to the user, and meanwhile, I will refresh it), and handles the asynchrony/synchrony. Caching is both physical and logical, so it can survive between app sessions. It has a mirror project for desktop (EasyRest-Desktop: https://github.com/fcopardo/EasyRest-desktop), with the same capabilities and signatures.

why another rest framework? There are a lot of options out there...

Back in the day, I was working in a project composed of a desktop (no, no web. DESKTOP) program, a webpage, and an android app. Gingerbread was the version at the time, and I was working on both a swing app (Yes, I know, I know!!!!) and the servers. And because the android apps are coded in java, I found myself answering, then coding, and finally working into the android app too. With such a workload, I started to create wrapper classes around libraries and common processes (databases, networking, threading, caching...), so I could code the three things simultaneosly. And there came a day, when I realized I was copypasting identical packages over and over... so I created libraries, and I needed to distribute them somehow. So, I published this!

Ok, how does it works??

First of all, you are gonna need jitpack:

Gradle:

add the jitpack repository:

maven { 
    url "https://jitpack.io" 
}

and then the dependency:

compile ('com.github.fcopardo:easyrest:v1.3.7@aar'){
    transitive = true
    ///If you are using jackson, spring-android and ok-http, they are included here.
}

Because we are using jackson, we also should add this to the android configuration:

android{
//Loots of things

packagingOptions {
        exclude 'META-INF/DEPENDENCIES.txt'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/dependencies.txt'
        exclude 'META-INF/LGPL2.1'
        exclude 'META-INF/ASL2.0'
        exclude 'META-INF/services/com.fasterxml.jackson.core.ObjectCodec'
        exclude 'META-INF/services/com.fasterxml.jackson.core.JsonFactory'
    }
}


For every call, you are gonna need to inject both the Response body type and the Request body type, as java classes (you can either use jackson or just create a common POJO). Obviously you can use the Void type if a body is not required.
