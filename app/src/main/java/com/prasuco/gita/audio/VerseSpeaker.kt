package com.prasuco.gita.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Verse audio, V1: on-device TextToSpeech (offline where voice data exists).
 *
 * Slot for recorded audio: when chanting/explainer tracks land, add
 * `fun playTrack(url: String)` here and keep the same [speaking] contract —
 * UI code ([AppPill] Listen) won't change.
 */
object VerseSpeaker {
    var speaking by mutableStateOf(false)
        private set
    var label by mutableStateOf("")
        private set

    private var tts: TextToSpeech? = null
    private var pending: Pair<Context, String>? = null

    fun toggle(ctx: Context, english: String, spokenLabel: String = "") {
        if (speaking) { stop(); return }
        label = spokenLabel
        val appCtx = ctx.applicationContext
        val t = tts
        if (t != null) {
            speak(t, english)
            return
        }
        pending = appCtx to english
        tts = TextToSpeech(appCtx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    engine.language = Locale("hi", "IN").let {
                        // fall back to device default if Hindi voice missing
                        if (engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE) it
                        else Locale.getDefault()
                    }
                    engine.setSpeechRate(0.85f)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}
                        override fun onError(id: String?) { speaking = false }
                        override fun onDone(id: String?) { speaking = false }
                    })
                    pending?.let { (c, e) -> speak(engine, e) }
                    pending = null
                }
            } else {
                pending = null
            }
        }
    }

    private fun speak(engine: TextToSpeech, english: String) {
        val params = Bundle()
        engine.speak(english.replace(Regex("\\s+"), " ").trim(), TextToSpeech.QUEUE_FLUSH, params, "verse")
        speaking = true
    }

    fun stop() {
        tts?.stop()
        speaking = false
    }
}
