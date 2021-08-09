# Unit Testing android fragments and activity with Hilt and Robolectric

In this blog, which will be a short one I'll discuss how to test Fragments and Activities.

The problem is that you can't use the standard `launchInContainer` when talking about Hilt activities. Especially so because Robolectric has it's own way of creating Activities which are NOT annotated with `@AndroidEntryPoint`. 

OK, as an example we'll create a new application, and within this project we'll test the activity.

I'll test detailing the process as we go. Let's start:

1. Create new project named `Hilt Test Robolectric`
2. Add hilt to the project https://developer.android.com/training/dependency-injection/hilt-android
3. Add Robolectric http://robolectric.org/getting-started/
4. Add a fragment



First we create a new fragment, I'll create it with the xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".BlankFragment">

    <TextView
        android:id="@+id/tv_blank"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="Hello From Blank" />

</FrameLayout>
```

And the Kotlin Class:

```kotlin
class BlankFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }
}
```



Now Let's test this:

First to use `launchInContainer` we should add the dependency:

```groovy
debugImplementation "androidx.fragment:fragment-testing:1.3.6"
```

Now, we can write our first test:

```kotlin
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.O_MR1],
)
class BlankFragmentTest {

    @Test
    fun testMainActivity() {
        val frag = launchFragmentInContainer<BlankFragment>()

        frag.onFragment {
            assert(it.view?.findViewById<TextView>(R.id.tv_blank)?.text.toString() == "Hello From Blank")
        }
    }
}
```

And here we find our problem, the one we are tying to fix:

```sh
Hilt Fragments must be attached to an @AndroidEntryPoint Activity. Found: class androidx.fragment.app.testing.FragmentScenario$EmptyFragmentActivity
```





#### Fixing this

First we create a new source set `debug`. and we add `HiltTestActivity` into it.

Since our package is `de.sixbits.testrobolectrichilt` we can create the `HiltTestActivity` inside `src/debug/de/sixbits/testrobolectrichilt` .

Now, in that package we can add the `HiltTestActivity` Like:

```kotlin
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {
}
```

Now in `src/debug` we'll create an android Manifest and add this activity to it like:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="de.sixbits.testrobolectrichilt">

    <application>
        <activity
            android:name=".HiltTestActivity"
            android:exported="true" />
    </application>
</manifest>
```

Next, we will create a helper Kotlin Extension which will inflate the fragment in this activity like:

```kotlin
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    crossinline action: Fragment.() -> Unit = {}
) {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    )

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        fragment.action()
    }
}
```

And now we are going to change the test to be like:

```kotlin
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

```

And now we have a working test!

The application and it's source is located at:

https://github.com/MickSawy3r/testrobolectrichilt
