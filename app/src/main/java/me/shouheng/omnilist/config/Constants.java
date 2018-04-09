package me.shouheng.omnilist.config;

import me.shouheng.omnilist.BuildConfig;
import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;

/**
 * Created by shouh on 2018/4/8.*/
public class Constants {
    public static final String DEFAULT_LOG_TAG = "NotePal";

    // region Urls
    public final static String GOOGLE_PLAY_WEB_PAGE = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
    public final static String MARKET_PAGE = "market://details?id=" + BuildConfig.APPLICATION_ID;
    public final static String GITHUB_PAGE = "https://github.com/Shouheng88/NotePal-Page";
    public final static String GITHUB_DEVELOPER = "https://github.com/Shouheng88";
    public final static String GOOGLE_PLUS_URL = "https://plus.google.com/u/1/communities/102252970668657211916";
    public final static String WEIBO_PAGE = "https://weibo.com/5401152113/profile?rightmod=1&wvr=6&mod=personinfo";
    public final static String TWITTER_PAGE = "https://twitter.com/ShouhengWang";

    public final static String DEVELOPER_EMAIL = "shouheng2015@gmail.com";
    public final static String DEVELOPER_EMAIL_PREFIX = "【" + PalmApp.getStringCompact(R.string.app_name) +"|%s】";
    public final static String DEVELOPER_EMAIL_EMAIL_PREFIX = "\nContact Email:";
    // endregion

    // region Attachment
    public final static String MIME_TYPE_IMAGE = "image/jpeg";
    public final static String MIME_TYPE_AUDIO = "audio/amr";
    public final static String MIME_TYPE_VIDEO = "video/mp4";
    public final static String MIME_TYPE_SKETCH = "image/png";
    public final static String MIME_TYPE_FILES = "file/*";
    public final static String MIME_TYPE_HTML = "text/html";
    public final static String MIME_TYPE_IMAGE_EXTENSION = ".jpeg";
    public final static String MIME_TYPE_AUDIO_EXTENSION = ".amr";
    public final static String MIME_TYPE_VIDEO_EXTENSION = ".mp4";
    public final static String MIME_TYPE_SKETCH_EXTENSION = ".png";
    public final static String MIME_TYPE_CONTACT_EXTENSION = ".vcf";

    public final static String VIDEO_MIME_TYPE = "video/*";
    public final static String SCHEME_HTTPS = "https";
    public final static String SCHEME_HTTP = "http";
    public final static String PDF_MIME_TYPE = "application/pdf";
    public final static String _3GP = ".3gp";
    public final static String _MP4 = ".mp4";
    public final static String _PDF = ".pdf";
    // endregion

    public final static String REA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD0diKVSZ/U/KHuxZFYac3lLq7K\n" +
            "edqc+uOKSJgq26tgy4wmELCw8gJkempBm8NPf+uSOdWPlPLWijSf3W2KfzMMvZQ2\n" +
            "tfNQPQu+gXgdXuZC+fhqVqNgYtWVRMIspveSm3AK+52AxxzTlfAU1fpCEFOf4AHc\n" +
            "/E33toB493pf9gS2xwIDAQAB";
}
