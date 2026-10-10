package com.prasuco.gita.ui

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CHAPTERS = "chapters"
    const val CHAPTER = "chapter/{ch}"
    const val VERSE = "verse/{ch}/{v}"
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"
    fun chapter(ch: Int) = "chapter/$ch"
    fun verse(ch: Int, v: Int) = "verse/$ch/$v"
}
