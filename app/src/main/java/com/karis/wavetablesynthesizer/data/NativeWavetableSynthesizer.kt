package com.karis.wavetablesynthesizer.data

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.karis.wavetablesynthesizer.domain.WaveTable
import com.karis.wavetablesynthesizer.domain.WavetableSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class NativeWavetableSynthesizer @Inject constructor() : WavetableSynthesizer, DefaultLifecycleObserver {
    private var synthesizerHandle: Long = 0
    private val synthesizerMutex = Object()
    private external fun create(): Long
    private external fun delete(synthesizerHandle: Long)
    private external fun play(synthesizerHandle: Long)
    private external fun stop(synthesizerHandle: Long)
    private external fun isPlaying(synthesizerHandle: Long): Boolean
    private external fun setFrequency(synthesizerHandle: Long, frequencyInHz: Float)
    private external fun setVolume(synthesizerHandle: Long, amplitudeInDb: Float)
    private external fun setWavetable(synthesizerHandle: Long, wavetable: Int)

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        synchronized(synthesizerMutex) {
            Timber.d("onResume() called")
            createNativeHandleIfNotExists()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        synchronized(synthesizerMutex) {
            Timber.tag("NativeWavetableSynthesizer").d("onPause() called")

            if (synthesizerHandle == 0L) {
                Timber.e("Attempting to destroy a null synthesizer.")
                return
            }

            // Destroy the synthesizer
            delete(synthesizerHandle)
            synthesizerHandle = 0L
        }
    }

    override suspend fun play() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            play(synthesizerHandle)
        }
    }

    override suspend fun stop() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            stop(synthesizerHandle)
        }
    }

    override suspend fun isPlaying(): Boolean = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            return@withContext isPlaying(synthesizerHandle)
        }
    }

    override suspend fun setFrequency(frequencyInHz: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setFrequency(synthesizerHandle, frequencyInHz)
        }
    }

    override suspend fun setVolume(volumeInDb: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setVolume(synthesizerHandle, volumeInDb)
        }
    }

    override suspend fun setWavetable(wavetable: WaveTable) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setWavetable(synthesizerHandle, wavetable.ordinal)
        }
    }

    private fun createNativeHandleIfNotExists() {
        if (synthesizerHandle != 0L) {
            return
        }

        // create the synthesizer
        synthesizerHandle = create()
    }
}