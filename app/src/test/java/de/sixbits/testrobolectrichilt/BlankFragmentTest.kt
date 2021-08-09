package de.sixbits.testrobolectrichilt

import android.os.Build
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.O_MR1],
    application = HiltTestApplication::class
)
class BlankFragmentTest {
    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Test
    fun testMainActivity() {
        launchFragmentInHiltContainer<BlankFragment> {
            assert(
                this.view?.findViewById<TextView>(
                    R.id.tv_blank
                )?.text.toString() == "Hello From Blank"
            )
        }
    }
}
