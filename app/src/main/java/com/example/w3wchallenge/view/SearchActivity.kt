package com.example.w3wchallenge.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.w3wchallenge.R
import com.example.w3wchallenge.callback.SuggestionClickCallback
import com.example.w3wchallenge.extensions.getViewModel
import com.example.w3wchallenge.view.adapter.SuggestionsAdapter
import com.example.w3wchallenge.viewmodel.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import com.what3words.javawrapper.response.Suggestion
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_search.*
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class SearchActivity : DaggerAppCompatActivity(), SearchView.OnQueryTextListener,
    SuggestionClickCallback {

    companion object {
        const val EXTRA_KEY_WORD = "EXTRA_KEY_WORD"
        const val AUDIO_PERMISSION = 1002
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: SearchViewModel by lazy {
        getViewModel(SearchViewModel::class.java, viewModelFactory)
    }

    private val adapter: SuggestionsAdapter by lazy {
        SuggestionsAdapter(this)
    }

    private val speakNowSnackbar: Snackbar by lazy {
        Snackbar.make(
            holderSearch,
            R.string.speak_now,
            Snackbar.LENGTH_INDEFINITE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(false)
        searchView.onActionViewExpanded()

        listSuggestions.layoutManager = LinearLayoutManager(this)
        listSuggestions.adapter = adapter
        listSuggestions.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )

        buttonVoice.setOnClickListener {
            searchView.clearFocus()
            try {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                ) {
                    handleAudioRecord()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION)
                }
            } catch (e: SecurityException) {
                Timber.e(e)
            }
        }

        viewModel.suggestions.observe(this, Observer {
            speakNowSnackbar.dismiss()
            if (it?.success == true) {
                adapter.setData(it.suggestions)
                if (it.suggestions.isEmpty()) {
                    labelSearch.visibility = VISIBLE
                    Snackbar.make(
                        holderSearch,
                        R.string.no_suggestions_found,
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    labelSearch.visibility = GONE
                }
            } else if (!it.error.isNullOrEmpty()) {
                Snackbar.make(holderMap, it.error!!, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun handleAudioRecord() {
        speakNowSnackbar.show()
        viewModel.handleAudioRecord()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION -> {
                if (grantResults.count() > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    handleAudioRecord()
                } else {
                    Snackbar.make(holderSearch, R.string.voice_permission, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (Pattern.compile(W3W_REGEX).matcher(query).find()) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_KEY_WORD, query)
            })
            finish()
        } else {
            Snackbar.make(
                holderSearch,
                R.string.invalid_word,
                Snackbar.LENGTH_LONG
            ).show()
        }
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.autosuggest(newText)
        return false
    }

    override fun onClick(suggestion: Suggestion) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_KEY_WORD, suggestion.words)
        })
        finish()
    }
}