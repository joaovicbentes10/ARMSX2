package com.armsx2.nevoa.intro

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IntroSoundTest {
    @Test fun accepts_small_audio() = assertNull(IntroSound.validate(300_000, "audio/mpeg"))
    @Test fun rejects_too_big() = assertNotNull(IntroSound.validate(IntroSound.MaxBytes + 1, "audio/mpeg"))
    @Test fun rejects_non_audio() = assertNotNull(IntroSound.validate(1000, "video/mp4"))
    @Test fun accepts_ogg_application_mime() = assertNull(IntroSound.validate(1024, "application/ogg"))
    @Test fun accepts_ogg_mime_case_insensitively() = assertNull(IntroSound.validate(1024, "APPLICATION/OGG"))
    @Test fun ogg_does_not_bypass_size_limit() = assertNotNull(IntroSound.validate(IntroSound.MaxBytes + 1, "application/ogg"))
    @Test fun rejects_unknown_mime() = assertNotNull(IntroSound.validate(1000, null))
}
