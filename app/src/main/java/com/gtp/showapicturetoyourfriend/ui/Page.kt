package com.gtp.showapicturetoyourfriend.ui

sealed class Page {
    object Receiver : Page()
    object Demo : Page()
}
