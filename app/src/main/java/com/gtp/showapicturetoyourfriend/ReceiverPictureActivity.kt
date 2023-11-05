package com.gtp.showapicturetoyourfriend

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentSanitizer
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.ortiz.touchview.TouchImageView

class ReceiverPictureActivity : AppCompatActivity() {
    private lateinit var handly: Handler
    private lateinit var goahead: Runnable
    private var page = 0
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        setContentView(R.layout.activity_receiverpictureactivity)
        window.findViewById<View>(R.id.button).setOnClickListener { screenIsLocked() }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("ready", false)) {
                page = savedInstanceState.getInt("pageItem", 0)
                screenIsLocked()
            }
        }

        // periodically checks if the screen is locked, if it is calls screenIsLocked()
        handly = Handler(Looper.getMainLooper())
        goahead = Runnable {
            val myKM = application.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (myKM.inKeyguardRestrictedInputMode()) {
                screenIsLocked()
            } else {
                handly.postDelayed(goahead, 40)
            }
        }
        goahead.run()
    }

    // to make sure back button doesn't open old images
    override fun onNewIntent(intent: Intent) {
        try {
            val sanitizedIntent = IntentSanitizer.Builder()
                .allowType("image/*")
                .allowType("video/*")
                .build()
                .sanitizeByThrowing(intent)
            super.onNewIntent(sanitizedIntent)
            finish()
            startActivity(sanitizedIntent)
        } catch (e: SecurityException) {
            Log.e("IntentSanitizer", "Security Exception occured while sanitizing new intent")
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mViewPager != null) {
            outState.putInt("pageItem", mViewPager!!.currentItem)
            outState.putBoolean("ready", true)
        } else {
            outState.putBoolean("ready", false)
        }
    }

    private fun screenIsLocked() {
        handly.removeCallbacks(goahead)
        val screenLock = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "showapicturetoyourfriend:wakelock"
        )
        // removes handler, wakes up screen and releases wakelock immediately
        screenLock.acquire(1)
        screenLock.release()

        val action = intent.action
        setContentView(R.layout.activity_receivemultiple)
        var imageUris: ArrayList<Uri>? = null

        // puts uris into an array, whether there is one or multiple
        if (Intent.ACTION_SEND == action) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            imageUris = ArrayList()
            if (imageUri != null) {
                imageUris.add(imageUri)
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            imageUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                 intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
        }

        if (imageUris == null) return

        val mDemoCollectionPagerAdapter = DemoCollectionPagerAdapter(supportFragmentManager).apply {
            setCounts(imageUris.size)
            setUris(imageUris, this@ReceiverPictureActivity)
            setAdapter(this)
        }
        findViewById<ViewPager>(R.id.pager).apply {
            offscreenPageLimit = 2
            adapter = mDemoCollectionPagerAdapter
            currentItem = page
        }
    }

    override fun onDestroy() {
        handly.removeCallbacks(goahead)
        super.onDestroy()
    }

    class DemoCollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var uris: java.util.ArrayList<Uri>? = null
        private var context: Context? = null
        private var atp: PagerAdapter? = null
        private var count = 0

        override fun getItem(position: Int): Fragment {
            val fragment = DemoObjectFragment()
            val args = Bundle()
            val uri = uris!![position]
            val stringUri = uri.toString()
            args.putString("Uri", stringUri)
            fragment.arguments = args
            return fragment
        }

        override fun getCount() = count

        override fun getPageTitle(position: Int) = "OBJECT " + (position + 1)

        fun setUris(uris: java.util.ArrayList<Uri>, context: Context) {
            this.uris = uris
            this.context = context
        }

        fun setAdapter(adapter: PagerAdapter?) {
            atp = adapter
        }

        fun setCounts(count: Int) {
            this.count = count
        }
    }

    class DemoObjectFragment : Fragment() {
        private lateinit var videoView: VideoView
        private lateinit var controller: MediaController
        private var viewVisibleInOnCreate = false
        private var isVideo = false
        private var isControllerShowing = false

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            var rootView: View? = null
            val forUri = requireArguments().getString("Uri")
            val uriNormal = Uri.parse(forUri)
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(forUri!!.replace("~", ""))
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            val startWith = requireActivity().contentResolver.getType(uriNormal)
            if (startWith != null) {
                if (startWith.startsWith("image/")) {
                    rootView = inflater.inflate(R.layout.adapterimage, container, false)
                    pictureSet(
                        rootView.findViewById<View>(R.id.touchImageView) as TouchImageView,
                        uriNormal
                    )
                } else if (startWith.startsWith("video/")) {
                    rootView = inflater.inflate(R.layout.adaptervideo, container, false)
                    videoSet(rootView.findViewById<View>(R.id.videoview) as VideoView, uriNormal)
                }
            } else {
                if (type != null) {
                    if (requireActivity().checkSelfPermission(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) {
                        rootView = typeMethod(uriNormal, container, type, inflater)
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            1
                        )
                        Toast.makeText(activity, R.string.permission, Toast.LENGTH_LONG).show()
                    }
                }
            }
            if (viewVisibleInOnCreate) {
                viewNowVisible(true)
            }
            return rootView
        }

        private fun typeMethod(
            uriNormal: Uri,
            container: ViewGroup?,
            type: String,
            inflater: LayoutInflater
        ): View? {
            var rootView: View? = null
            if (type.startsWith("image/")) {
                rootView = inflater.inflate(R.layout.adapterimage, container, false)
                pictureSetFile(
                    rootView.findViewById<View>(R.id.touchImageView) as TouchImageView,
                    uriNormal
                )
            } else if (type.startsWith("video/")) {
                rootView = inflater.inflate(R.layout.adaptervideo, container, false)
                videoSet(rootView.findViewById<View>(R.id.videoview) as VideoView, uriNormal)
            }
            return rootView
        }

        private fun pictureSet(imageSet: TouchImageView, uriNormal: Uri) {
            imageSet.maxZoom = 30f
            Glide.with(requireContext())
                .load(uriNormal)
                .override(2000, 2000) //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(object : DrawableImageViewTarget(imageSet) {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        super.onResourceReady(resource, transition)
                        imageSet.setZoom(1f)
                    }
                })
        }

        private fun pictureSetFile(imageSet: TouchImageView, uriNormal: Uri) {
            imageSet.maxZoom = 30f
            Glide.with(requireContext())
                .load(uriNormal)
                .override(2000, 2000) //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(object : DrawableImageViewTarget(imageSet) {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        super.onResourceReady(resource, transition)
                        imageSet.setZoom(1f)
                    }
                })
        }

        private fun videoSet(video: VideoView, uriNormal: Uri) {
            video.setVideoURI(uriNormal)
            video.seekTo(1)
            controller = MediaController(activity)
            videoView = video
            isVideo = true
        }

        @Deprecated("Deprecated in Java")
        override fun setUserVisibleHint(isVisibleToUser: Boolean) {
            super.setUserVisibleHint(isVisibleToUser)
            if (view != null) {
                viewNowVisible(isVisibleToUser)
            } else {
                viewVisibleInOnCreate = isVisibleToUser
            }
        }

        private fun viewNowVisible(isVisibleToUser: Boolean) {
            if (!isVideo) return
            if (isVisibleToUser) {
                Log.d("r", "VIDEO ON")
                if (isControllerShowing) {
                    controller.show()
                } else {
                    controller.setAnchorView(videoView)
                    controller.setMediaPlayer(videoView)
                    videoView.setMediaController(controller)
                    isControllerShowing = true
                }
                videoView.start()
            } else {
                Log.d("r", "VIDEO OFF")
                videoView.pause()
                controller.hide()
            }
        }
    }
}
