package com.example.w3wchallenge.viewmodel

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaActionSound
import android.media.MediaRecorder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.w3wchallenge.BuildConfig
import com.example.w3wchallenge.extensions.io
import com.example.w3wchallenge.extensions.ioThenMain
import com.example.w3wchallenge.extensions.main
import com.example.w3wchallenge.extensions.requestLocation
import com.example.w3wchallenge.viewmodel.model.BaseVoiceMessagePayload
import com.example.w3wchallenge.view.SearchActivity
import com.example.w3wchallenge.viewmodel.model.SuggestionsPayload
import com.example.w3wchallenge.viewmodel.model.SuggestionsUpdate
import com.google.gson.Gson
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.Coordinates
import kotlinx.coroutines.Job
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val what3WordsV3: What3WordsV3,
    private val context: Context
) : ViewModel() {

    companion object {
        const val RECORDING_RATE = 16000
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var searchJob: Job? = null
    private val bufferSize = AudioRecord.getMinBufferSize(
        RECORDING_RATE, CHANNEL, FORMAT
    )
    private var recorder: AudioRecord? = null
    private var continueRecording: Boolean = false

    val suggestions = MutableLiveData<SuggestionsUpdate>()

    fun autosuggest(newText: String) {
        searchJob?.cancel()
        searchJob = ioThenMain({
            if (Pattern.compile(SearchActivity.W3W_REGEX).matcher(newText).find()) {
                val request = what3WordsV3.autosuggest(newText).nResults(5)
                context.requestLocation()?.let {
                    request.focus(Coordinates(it.latitude, it.longitude))
                }
                request.execute()
            } else null
        }, {
            it?.let {
                suggestions.value = SuggestionsUpdate().apply {
                    this.success = it.isSuccessful
                    if (it.isSuccessful) {
                        this.suggestions = it.suggestions
                    } else {
                        this.error = it.error.message
                    }
                }
            }
        })
    }

    fun handleAudioRecord() {
        io {
            var url =
                "wss://voiceapi.what3words.com/v1/autosuggest?key=${BuildConfig.W3W_API_KEY}&voice-language=en"
            context.requestLocation()?.let {
                url += "&focus=${it.latitude},${it.longitude}"
            }
            val request = Request.Builder().url(url).build()
            OkHttpClient().newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    val message = JSONObject(
                        mapOf(
                            "message" to "StartRecognition",
                            "audio_format" to mapOf(
                                "type" to "raw",
                                "encoding" to "pcm_s16le",
                                "sample_rate" to RECORDING_RATE
                            )
                        )
                    )
                    webSocket.send(message.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    val message = Gson().fromJson(text, BaseVoiceMessagePayload::class.java)
                    if (message.message == BaseVoiceMessagePayload.RecognitionStarted) {
                        continueRecording = true
                        processAudio(webSocket)
                    }

                    if (message.message == BaseVoiceMessagePayload.Suggestions) {
                        val result = Gson().fromJson(text, SuggestionsPayload::class.java)
                        continueRecording = false
                        main {
                            suggestions.value = SuggestionsUpdate().apply {
                                this.success = true
                                this.suggestions = result.suggestions
                            }
                        }
                        stopAudio()
                        webSocket.close(1000, "JOB FINISHED")
                    }

                    if (message.code != null && message.message != null) {
                        main {
                            suggestions.value = SuggestionsUpdate().apply {
                                this.success = false
                                this.error = message.message
                            }
                        }
                        Timber.e(message.message)
                        stopAudio()
                        webSocket.close(1002, "JOB FINISHED WITH ERRORS")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    stopAudio()
                }
            })
        }
    }

    fun stopAudio() {
        main {
            val sound = MediaActionSound()
            sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
        }
        continueRecording = false
        recorder?.release()
    }

    fun processAudio(webSocket: WebSocket) {
        io {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDING_RATE,
                CHANNEL,
                FORMAT,
                bufferSize * 10
            ).also {
                main {
                    val sound = MediaActionSound()
                    sound.play(MediaActionSound.START_VIDEO_RECORDING)
                }
                val buffer = ByteArray(bufferSize)
                it.startRecording()
                while (continueRecording) {
                    it.read(buffer, 0, buffer.size)
                    webSocket.send(ByteString.of(*buffer))
                }
            }
        }
    }
}