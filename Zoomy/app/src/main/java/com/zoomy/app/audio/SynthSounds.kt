package com.zoomy.app.audio

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.sin

object SynthSounds {
    private const val SAMPLE_RATE = 44100

    fun generateAllSounds(cacheDir: File) {
        val sfxDir = File(cacheDir, "sfx")
        if (!sfxDir.exists()) sfxDir.mkdirs()

        // 1. tap: sine 1000 Hz, 40ms, tau 12ms.
        generateSoundIfMissing(sfxDir, "tap.wav") {
            genSineWithDecay(freq = 1000f, durationMs = 40, tauMs = 12f)
        }

        // 2. swipe: sine sweeping 500 -> 700 Hz over 70ms, tau 25ms.
        generateSoundIfMissing(sfxDir, "swipe.wav") {
            genSweepWithDecay(startFreq = 500f, endFreq = 700f, durationMs = 70, tauMs = 25f)
        }

        // 3. coin: two sines 1318 Hz then 1760 Hz, 60ms each, tau 30ms.
        generateSoundIfMissing(sfxDir, "coin.wav") {
            genCoin()
        }

        // 4. close_2 .. close_5: sine sweeping f -> 1.5 f over 120ms (tau 45ms) with f = 600 * 2^((combo - 2) * 2 / 12) Hz.
        for (combo in 2..5) {
            val f = 600f * Math.pow(2.0, ((combo - 2) * 2.0 / 12.0)).toFloat()
            generateSoundIfMissing(sfxDir, "close_$combo.wav") {
                genSweepWithDecay(startFreq = f, endFreq = f * 1.5f, durationMs = 120, tauMs = 45f)
            }
        }

        // 5. shield: sine sweeping 400 -> 800 Hz over 150ms, tau 60ms;
        //    shield_break: noise 120ms at 0.2 plus sine 300 Hz 100ms.
        generateSoundIfMissing(sfxDir, "shield.wav") {
            genSweepWithDecay(startFreq = 400f, endFreq = 800f, durationMs = 150, tauMs = 60f)
        }
        generateSoundIfMissing(sfxDir, "shield_break.wav") {
            genShieldBreak()
        }

        // 6. magnet: sine 520 Hz with 8 Hz tremolo, 220ms, tau 100ms;
        //    slowmo: sine sweeping 700 -> 300 Hz over 250ms, tau 90ms.
        generateSoundIfMissing(sfxDir, "magnet.wav") {
            genMagnet()
        }
        generateSoundIfMissing(sfxDir, "slowmo.wav") {
            genSweepWithDecay(startFreq = 700f, endFreq = 300f, durationMs = 250, tauMs = 90f)
        }

        // 7. crash: noise burst 300ms (amplitude 0.35, exp decay tau 90ms) plus sine sweeping 160 -> 70 Hz over 300ms.
        generateSoundIfMissing(sfxDir, "crash.wav") {
            genCrash()
        }

        // 8. gameover: 4 descending notes 392, 330, 262, 196 Hz, 150ms each, tau 90ms.
        generateSoundIfMissing(sfxDir, "gameover.wav") {
            genGameOver()
        }

        // 9. newbest: 5-note ascending arpeggio 523, 659, 784, 1046, 1318 Hz, 80ms each.
        generateSoundIfMissing(sfxDir, "newbest.wav") {
            genNewBest()
        }

        // 10. reward: 2-note 784 -> 1046 Hz, 100ms each.
        generateSoundIfMissing(sfxDir, "reward.wav") {
            genReward()
        }

        // 11. pop: sine 700 -> 1400 Hz over 80ms, tau 25ms.
        generateSoundIfMissing(sfxDir, "pop.wav") {
            genSweepWithDecay(startFreq = 700f, endFreq = 1400f, durationMs = 80, tauMs = 25f)
        }
    }

    private fun generateSoundIfMissing(dir: File, filename: String, generator: () -> ShortArray) {
        val file = File(dir, filename)
        if (!file.exists() || file.length() == 0L) {
            val samples = generator()
            writeWavFile(file, samples)
        }
    }

    private fun genEnvelope(tMs: Float, totalMs: Float, tauMs: Float): Float {
        // 5ms linear attack, exponential decay exp(-t / tau), peak 0.5, final 10ms linear fade to zero
        val attack = if (tMs < 5f) tMs / 5f else 1f
        val decay = exp(-tMs / tauMs)
        val fadeStart = totalMs - 10f
        val fade = if (tMs > fadeStart) (totalMs - tMs).coerceIn(0f, 10f) / 10f else 1f
        return 0.5f * attack * decay * fade
    }

    private fun genSineWithDecay(freq: Float, durationMs: Int, tauMs: Float): ShortArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val env = genEnvelope(tMs, durationMs.toFloat(), tauMs)
            val sample = sin(2.0 * Math.PI * freq * tSec).toFloat() * env
            buffer[i] = (sample.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genSweepWithDecay(startFreq: Float, endFreq: Float, durationMs: Int, tauMs: Float): ShortArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        val durSec = durationMs / 1000f
        for (i in 0 until totalSamples) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val currentFreq = startFreq + (endFreq - startFreq) * (tSec / durSec)
            phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
            val env = genEnvelope(tMs, durationMs.toFloat(), tauMs)
            val sample = sin(phase).toFloat() * env
            buffer[i] = (sample.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genCoin(): ShortArray {
        val n1 = (SAMPLE_RATE * 0.06f).toInt()
        val n2 = (SAMPLE_RATE * 0.06f).toInt()
        val total = n1 + n2
        val buffer = ShortArray(total)
        for (i in 0 until n1) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val env = genEnvelope(tMs, 60f, 30f)
            val s = sin(2.0 * Math.PI * 1318.0 * tSec).toFloat() * env
            buffer[i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        for (i in 0 until n2) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val env = genEnvelope(tMs, 60f, 30f)
            val s = sin(2.0 * Math.PI * 1760.0 * tSec).toFloat() * env
            buffer[n1 + i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genShieldBreak(): ShortArray {
        val totalSamples = (SAMPLE_RATE * 0.12f).toInt()
        val buffer = ShortArray(totalSamples)
        val rnd = java.util.Random()
        for (i in 0 until totalSamples) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val noise = (rnd.nextFloat() * 2f - 1f) * 0.2f
            val sine = if (tMs <= 100f) sin(2.0 * Math.PI * 300.0 * tSec).toFloat() * 0.3f else 0f
            val env = genEnvelope(tMs, 120f, 50f)
            val s = (noise + sine) * env
            buffer[i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genMagnet(): ShortArray {
        val durationMs = 220
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            val tremolo = 0.5f + 0.5f * sin(2.0 * Math.PI * 8.0 * tSec).toFloat()
            val env = genEnvelope(tMs, durationMs.toFloat(), 100f)
            val s = sin(2.0 * Math.PI * 520.0 * tSec).toFloat() * tremolo * env
            buffer[i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genCrash(): ShortArray {
        val durationMs = 300
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)
        val rnd = java.util.Random()
        var phase = 0.0
        val durSec = durationMs / 1000f
        for (i in 0 until totalSamples) {
            val tSec = i / SAMPLE_RATE.toFloat()
            val tMs = tSec * 1000f
            // noise burst amplitude 0.35, exp decay tau 90ms
            val noise = (rnd.nextFloat() * 2f - 1f) * 0.35f * exp(-tMs / 90f)
            // sine sweeping 160 -> 70 Hz
            val freq = 160f + (70f - 160f) * (tSec / durSec)
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE
            val sine = sin(phase).toFloat() * 0.3f * exp(-tMs / 120f)
            val fade = if (tMs > 280f) (300f - tMs) / 20f else 1f
            val s = (noise + sine) * fade
            buffer[i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun genGameOver(): ShortArray {
        val notes = listOf(392f, 330f, 262f, 196f)
        val noteSamples = (SAMPLE_RATE * 0.15f).toInt()
        val buffer = ShortArray(noteSamples * notes.size)
        notes.forEachIndexed { idx, freq ->
            for (i in 0 until noteSamples) {
                val tSec = i / SAMPLE_RATE.toFloat()
                val tMs = tSec * 1000f
                val env = genEnvelope(tMs, 150f, 90f)
                val s = sin(2.0 * Math.PI * freq * tSec).toFloat() * env
                buffer[idx * noteSamples + i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
            }
        }
        return buffer
    }

    private fun genNewBest(): ShortArray {
        val notes = listOf(523f, 659f, 784f, 1046f, 1318f)
        val noteSamples = (SAMPLE_RATE * 0.08f).toInt()
        val buffer = ShortArray(noteSamples * notes.size)
        notes.forEachIndexed { idx, freq ->
            for (i in 0 until noteSamples) {
                val tSec = i / SAMPLE_RATE.toFloat()
                val tMs = tSec * 1000f
                val env = genEnvelope(tMs, 80f, 50f)
                val s = sin(2.0 * Math.PI * freq * tSec).toFloat() * env
                buffer[idx * noteSamples + i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
            }
        }
        return buffer
    }

    private fun genReward(): ShortArray {
        val notes = listOf(784f, 1046f)
        val noteSamples = (SAMPLE_RATE * 0.10f).toInt()
        val buffer = ShortArray(noteSamples * notes.size)
        notes.forEachIndexed { idx, freq ->
            for (i in 0 until noteSamples) {
                val tSec = i / SAMPLE_RATE.toFloat()
                val tMs = tSec * 1000f
                val env = genEnvelope(tMs, 100f, 60f)
                val s = sin(2.0 * Math.PI * freq * tSec).toFloat() * env
                buffer[idx * noteSamples + i] = (s.coerceIn(-1f, 1f) * 32767).toInt().toShort()
            }
        }
        return buffer
    }

    private fun writeWavFile(file: File, samples: ShortArray) {
        val dataSize = samples.size * 2
        val totalSize = 36 + dataSize
        val out = FileOutputStream(file)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        header.put("RIFF".toByteArray())
        header.putInt(totalSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // SubChunk1Size (PCM)
        header.putShort(1) // AudioFormat (PCM)
        header.putShort(1) // NumChannels (Mono)
        header.putInt(SAMPLE_RATE)
        header.putInt(SAMPLE_RATE * 2) // ByteRate
        header.putShort(2) // BlockAlign
        header.putShort(16) // BitsPerSample
        header.put("data".toByteArray())
        header.putInt(dataSize)

        out.write(header.array())

        val pcm = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { pcm.putShort(it) }
        out.write(pcm.array())
        out.close()
    }
}
