package com.xlythe.saolauncher.smsextension.util;

import java.util.ArrayList;

import com.xlythe.saolauncher.smsextension.BuildConfig;
import com.xlythe.saolauncher.smsextension.R;
import com.xlythe.saolauncher.smsextension.provider.SmsMmsMessage;
import com.xlythe.saolauncher.smsextension.provider.SmsPopupContract.ContactNotifications;
import com.xlythe.saolauncher.smsextension.receiver.ReminderReceiver;
import com.xlythe.saolauncher.smsextension.service.SmsMonitorService;
import com.xlythe.saolauncher.smsextension.util.ManagePreferences.Defaults;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

/*
 * This class handles the Notifications (sounds/vibrate/LED)
 */
public class ManageNotification {
    public static final int NOTIFICATION_ALERT = 1337;
    public static final int NOTIFICATION_TEST = 888;
    public static final int NOTIFICATION_SEND_FAILED = 100;
    public static final String defaultRingtone =
            Settings.System.DEFAULT_NOTIFICATION_URI.toString();
    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");

    private static MediaPlayer mPlayer = null;

    private static final int NOTIFY = 0;
    private static final int FAILED = 1;
    public static final int[][] NOTIF_ICON_RES = {
            { R.drawable.stat_notify_sms, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc, R.drawable.stat_notify_sms_failed_htc },
            { R.drawable.stat_notify_sms_blur, R.drawable.stat_notify_sms_failed },

            { R.drawable.stat_notify_sms_blue, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_gray, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_green, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_lb, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_orange, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_pink, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_purple, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_red, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_white, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_yellow, R.drawable.stat_notify_sms_failed },

            { R.drawable.stat_notify_sms_old_blue, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_gray, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_green, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_lb, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_orange, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_pink, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_purple, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_red, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_white, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_old_yellow, R.drawable.stat_notify_sms_failed },

            { R.drawable.stat_notify_sms_htc_blue, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_gray, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_green, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_lb, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_orange, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_pink, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_purple, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_red, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_white, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_htc_yellow, R.drawable.stat_notify_sms_failed },

            { R.drawable.stat_notify_sms_blur_blue, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_gray, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_green, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_lb, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_orange, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_pink, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_purple, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_red, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_white, R.drawable.stat_notify_sms_failed },
            { R.drawable.stat_notify_sms_blur_yellow, R.drawable.stat_notify_sms_failed },
    };

    /*
     * Class to hold the popup notification elements
     */
    static class PopupNotification {
        public NotificationCompat.Builder notificationBuilder;
        public boolean privacyMode;
        public boolean privacySender;
        public boolean privacyAlways;
        public int notifIcon;
        public int notifFailedIcon;

        public boolean replyToThread;

        PopupNotification(NotificationCompat.Builder builder) {
            notificationBuilder = builder;
        }

        public void notify(Context context, int notif) {
            NotificationManager myNM =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Seems this is needed for the number value to take effect on the Notification
            myNM.cancel(notif);

            if (BuildConfig.DEBUG)
                Log.v("*** Notify running ***");
            myNM.notify(notif, notificationBuilder.build());
        }
    }

    /*
     * Default to NOTIFICATION_ALERT if notif is left out
     */
    public static void show(Context context, SmsMmsMessage message) {
        show(context, message, 1);
    }

    public static void show(Context context, SmsMmsMessage message, int messageCount) {
        show(context, message, messageCount, NOTIFICATION_ALERT);
    }

    /*
     * Show/play the notification given a SmsMmsMessage and a notification ID (really just
     * NOTIFICATION_ALERT for the main alert and NOTIFICATION_TEST for the test notification from
     * the preferences screen)
     */
    public static void show(Context context, SmsMmsMessage message, int messageCount, int notif) {
        notify(context, message, messageCount, false, notif);
    }

    /*
     * Only update the notification given the SmsMmsMessage (ie. do not play the vibrate/sound, just
     * update the text).
     */
    public static void update(Context context, SmsMmsMessage message, int messageCount) {

        // TODO: can I just use Notification.setLatestEventInfo() to update instead?
        if (message != null && messageCount > 0) {
            notify(context, message, messageCount, true, NOTIFICATION_ALERT);
        } else {
            // TODO: Should reply flag be set to true?
            ManageNotification.clearAll(context, true);
        }
    }

    /*
     * The main notify method
     */
    private static void notify(Context context, SmsMmsMessage message, int messageCount,
            boolean onlyUpdate, int notif) {

        if (message == null || messageCount < 1) {
            return;
        }

        final String messageBody = message.getMessageBody();
        final String contactName = message.getContactName();
        final long timestamp = message.getTimestamp();

        final PopupNotification n =
                buildNotification(context, message.getContactId(), message.getContactLookupKey(),
                        onlyUpdate, notif);

        if (n == null) {
            return;
        }

        // The notification title, sub-text and text that will scroll
        String contentTitle = "";
        String contentText = "";
        SpannableString scrollText;

        /*
         * This service runs a content observer on the system sms db to help clear the
         * notification icon in the case the user reads the messages outside of sms popup. the
         * service will be stopped when unread messages = 0
         */
        SmsMonitorService.beginStartingService(context);

        // If we're in privacy mode and the keyguard is on then just display
        // the name of the person, otherwise scroll the name and message
        if (n.privacyMode &&
                (ManageKeyguard.inKeyguardRestrictedInputMode() || n.privacyAlways)) {

            if (n.privacySender) {
                scrollText = new SpannableString(context.getString(
                        R.string.notification_scroll_privacy_no_name));
                contentTitle = scrollText.toString();
            } else {
                scrollText = new SpannableString(context.getString(
                        R.string.notification_scroll_privacy, contactName));
                contentTitle = contactName;
                contentText = context.getString(R.string.notification_scroll_privacy_no_name);
            }

        } else {

            contentTitle = contactName;
            contentText = messageBody;

            scrollText = new SpannableString(context.getString(R.string.notification_scroll,
                    contactName, messageBody));

            // Set contact name as bold
            scrollText.setSpan(new StyleSpan(Typeface.BOLD), 0, contactName.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        // If updating don't set scroll text
        if (onlyUpdate) {
            scrollText = null;
        }

        // Flag that a notification has been sent for this message
        message.setNotify(false);

        // The default intent when the notification is clicked (Inbox)
        Intent smsIntent = SmsPopupUtils.getSmsInboxIntent();

        // If more than one message waiting ...
        if (messageCount > 1) {
            contentTitle = context.getString(R.string.notification_multiple_title);
            contentText = context.getString(R.string.notification_multiple_text, messageCount);
        } else { // Else 1 message, set intent accordingly
            smsIntent = message.getReplyIntent(n.replyToThread);
        }

        /*
         * Ok, let's create our Notification object and set up all its parameters.
         */

        // Set the icon, scrolling text and timestamp
        n.notificationBuilder.setSmallIcon(n.notifIcon);
        n.notificationBuilder.setTicker(scrollText);
        n.notificationBuilder.setWhen(timestamp);

        // Notification notification =
        // new Notification(R.drawable.stat_notify_sms, scrollText, timestamp);

        // Set the PendingIntent if the status message is clicked
        PendingIntent notifIntent = PendingIntent.getActivity(context, 0, smsIntent, 0);

        // Set the messages that show when the status bar is pulled down
        n.notificationBuilder
                .setContentIntent(notifIntent)
                .setContentTitle(contentTitle)
                .setContentText(contentText);

        // Set number of events that this notification signifies (unread messages)
        if (messageCount > 1) {
            n.notificationBuilder.setNumber(messageCount);
        }

        n.notify(context, notif);
    }


    /*
     * Build the notification from user preferences
     */
    private static PopupNotification buildNotification(Context context, String contactId,
            String contactLookupKey, boolean onlyUpdate, int notif) {

        ManagePreferences mPrefs = new ManagePreferences(context, contactId, contactLookupKey);
        AudioManager mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Check if notifications are enabled - if not, we're done :)
        if (!mPrefs.getBoolean(
                R.string.pref_notif_enabled_key,
                Defaults.PREFS_NOTIF_ENABLED,
                ContactNotifications.ENABLED)) {

            return null;
        }

        // Get some preferences: vibrate and vibrate_pattern prefs
        boolean vibrate =
                mPrefs.getBoolean(
                        R.string.pref_vibrate_key,
                        Defaults.PREFS_VIBRATE_ENABLED,
                        ContactNotifications.VIBRATE_ENABLED);

        String vibrate_pattern_raw =
                mPrefs.getString(
                        R.string.pref_vibrate_pattern_key,
                        Defaults.PREFS_VIBRATE_PATTERN,
                        ContactNotifications.VIBRATE_PATTERN);

        String vibrate_pattern_custom_raw =
                mPrefs.getString(
                        R.string.pref_vibrate_pattern_custom_key,
                        Defaults.PREFS_VIBRATE_PATTERN,
                        ContactNotifications.VIBRATE_PATTERN_CUSTOM);

        // Get LED preferences
        boolean flashLed =
                mPrefs.getBoolean(
                        R.string.pref_flashled_key,
                        Defaults.PREFS_LED_ENABLED,
                        ContactNotifications.LED_ENABLED);

        String flashLedCol =
                mPrefs.getString(
                        R.string.pref_flashled_color_key,
                        Defaults.PREFS_LED_COLOR,
                        ContactNotifications.LED_COLOR);

        String flashLedColCustom =
                mPrefs.getString(
                        R.string.pref_flashled_color_custom_key,
                        Defaults.PREFS_LED_COLOR,
                        ContactNotifications.LED_COLOR_CUSTOM);

        String flashLedPattern =
                mPrefs.getString(
                        R.string.pref_flashled_pattern_key,
                        Defaults.PREFS_LED_PATTERN,
                        ContactNotifications.LED_PATTERN);

        String flashLedPatternCustom =
                mPrefs.getString(
                        R.string.pref_flashled_pattern_custom_key,
                        Defaults.PREFS_LED_PATTERN,
                        ContactNotifications.LED_PATTERN_CUSTOM);

        // Try and parse the user ringtone, use the default if it fails
        Uri notifSoundUri =
                Uri.parse(mPrefs.getString(
                        R.string.pref_notif_sound_key,
                        defaultRingtone,
                        ContactNotifications.RINGTONE));

        if (BuildConfig.DEBUG)
            Log.v("Sounds URI = " + notifSoundUri.toString());

        // Fetch privacy settings
        boolean privacyMode =
                mPrefs.getBoolean(R.string.pref_privacy_key, Defaults.PREFS_PRIVACY);

        boolean privacySender =
                mPrefs.getBoolean(R.string.pref_privacy_sender_key, Defaults.PREFS_PRIVACY_SENDER);

        boolean privacyAlways =
                mPrefs.getBoolean(R.string.pref_privacy_always_key, Defaults.PREFS_PRIVACY_ALWAYS);

        // Fetch notification icon
        int notifIcon =
                Integer.valueOf(mPrefs.getString(R.string.pref_notif_icon_key,
                        Defaults.PREFS_NOTIF_ICON));
        if (notifIcon < 0 || notifIcon >= NOTIF_ICON_RES.length) {
            notifIcon = Integer.valueOf(Defaults.PREFS_NOTIF_ICON);
            mPrefs.putString(R.string.pref_notif_icon_key, String.valueOf(notifIcon));
        }

        boolean replyToThread =
                mPrefs.getBoolean(R.string.pref_reply_to_thread_key, Defaults.PREFS_REPLY_TO_THREAD);

        boolean notifyOnCall =
                mPrefs.getBoolean(R.string.pref_notifyOnCall_key, Defaults.PREFS_NOTIFY_ON_CALL);

        // All done with prefs, close it up
        mPrefs.close();

        /*
         * Ok, let's create our Notification object and set up all its parameters.
         */
        Notification notification = new Notification();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setAutoCancel(true);

        /*
         * If this is a new notification (not updating a notification) then set LED, vibrate and
         * ringtone to fire
         */
        if (!onlyUpdate) {

            // Set up LED pattern and color
            if (flashLed) {
                /*
                 * Set up LED blinking pattern
                 */
                int[] ledPattern = null;
                if (context.getString(R.string.pref_custom_val).equals(flashLedPattern)) {
                    ledPattern = parseLEDPattern(flashLedPatternCustom);
                } else {
                    ledPattern = parseLEDPattern(flashLedPattern);
                }
                // Set to default if there was a problem
                if (ledPattern == null) {
                    ledPattern = parseLEDPattern(Defaults.PREFS_LED_PATTERN);
                }

                /*
                 * Set up LED color
                 */
                // Check if a custom color is set
                if (context.getString(R.string.pref_custom_val).equals(flashLedCol)) {
                    flashLedCol = flashLedColCustom;
                }

                // Default in case the parse fails
                int finalLedColor = Color.parseColor(Defaults.PREFS_LED_COLOR);
                // Try and parse the color
                if (flashLedCol != null) {
                    try {
                        finalLedColor = Color.parseColor(flashLedCol);
                    } catch (IllegalArgumentException e) {}
                }
                notificationBuilder.setLights(finalLedColor, ledPattern[0], ledPattern[1]);
            }

            // Get system telephony manager
            TelephonyManager mTM =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Setup vibrate and notification sound only if call state is idle (not on a call)
            if (mTM.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                /*
                 * Set up vibrate pattern
                 */
                // If vibrate is ON, or if phone is set to vibrate
                if ((vibrate || AudioManager.RINGER_MODE_VIBRATE == mAM.getRingerMode())) {

                    long[] vibratePattern = null;
                    if (context.getString(R.string.pref_custom_val).equals(vibrate_pattern_raw)) {
                        vibratePattern = parseVibratePattern(vibrate_pattern_custom_raw);
                    } else {
                        vibratePattern = parseVibratePattern(vibrate_pattern_raw);
                    }

                    if (vibratePattern != null) {
                        notificationBuilder.setVibrate(vibratePattern);
                    } else {
                        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                    }
                }

                /*
                 * Set up notification sound
                 */
                notificationBuilder.setSound(notifSoundUri);

            } else if (notifyOnCall) { // On a call or making a call

                try {
                    // Use MediaPlayer to play so they can hear the notification over the ear piece
                    if (mPlayer == null) {
                        mPlayer = MediaPlayer.create(context, notifSoundUri);
                    }

                    // Check null again in case mediaplayer couldn't be created
                    if (mPlayer != null) {
                        mPlayer.start();
                    }

                } catch (IllegalStateException e) {
                    if (BuildConfig.DEBUG)
                        Log.v("MediaPlayer, IllegalStateException - " + e);
                } finally {
                    if (mPlayer != null) {
                        mPlayer.release();
                    }
                }
            }
        }

        // Set intent to execute if the "clear all" notifications button is pressed -
        // basically stop any future reminders.
        Intent deleteIntent = new Intent(new Intent(context, ReminderReceiver.class));
        deleteIntent.setAction(Intent.ACTION_DELETE);
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        notificationBuilder.setDeleteIntent(pendingDeleteIntent);

        PopupNotification popupNotification = new PopupNotification(notificationBuilder);
        popupNotification.replyToThread = replyToThread;
        popupNotification.privacyMode = privacyMode;
        popupNotification.privacySender = privacySender;
        popupNotification.privacyAlways = privacyAlways;
        popupNotification.notifIcon = NOTIF_ICON_RES[notifIcon][NOTIFY];
        popupNotification.notifFailedIcon = NOTIF_ICON_RES[notifIcon][FAILED];

        return popupNotification;
    }

    // Clear the standard notification alert
    public static void clear(Context context) {
        clear(context, NOTIFICATION_ALERT);
    }

    // Clear a single notification
    public static void clear(Context context, int notif) {
        NotificationManager myNM =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        myNM.cancel(notif);
    }

    /*
     * Build the notification with no contactId
     */
    private static PopupNotification buildNotification(Context context,
            boolean onlyUpdate, int notif) {
        return buildNotification(context, null, null, onlyUpdate, notif);
    }

    public static void clearAll(Context context, boolean reply) {
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (reply
                || myPrefs.getBoolean(context.getString(R.string.pref_markread_key),
                        Defaults.PREFS_MARK_READ)) {

            NotificationManager myNM =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            myNM.cancelAll();

        }
    }

    public static void clearAll(Context context) {
        clearAll(context, true);
    }

    /**
     * Parse the user provided custom vibrate pattern into a long[]
     *
     */
    // TODO: tidy this up
    public static long[] parseVibratePattern(String stringPattern) {
        ArrayList<Long> arrayListPattern = new ArrayList<Long>();
        Long l;

        if (stringPattern == null)
            return null;

        String[] splitPattern = stringPattern.split(",");
        int VIBRATE_PATTERN_MAX_SECONDS = 60000;
        int VIBRATE_PATTERN_MAX_PATTERN = 100;

        for (int i = 0; i < splitPattern.length; i++) {
            try {
                l = Long.parseLong(splitPattern[i].trim());
            } catch (NumberFormatException e) {
                return null;
            }
            if (l > VIBRATE_PATTERN_MAX_SECONDS) {
                return null;
            }
            arrayListPattern.add(l);
        }

        // TODO: can i just cast the whole ArrayList into long[]?
        int size = arrayListPattern.size();
        if (size > 0 && size < VIBRATE_PATTERN_MAX_PATTERN) {
            long[] pattern = new long[size];
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = arrayListPattern.get(i);
            }
            return pattern;
        }

        return null;
    }

    /**
     * Parse LED pattern string into int[]
     *
     * @param stringPattern
     * @return
     */
    public static int[] parseLEDPattern(String stringPattern) {
        int on, off;

        if (stringPattern == null)
            return null;

        String[] splitPattern = stringPattern.split(",");

        if (splitPattern.length != 2)
            return null;

        final int LED_PATTERN_MIN_SECONDS = 0;
        final int LED_PATTERN_MAX_SECONDS = 60000;

        try {
            on = Integer.parseInt(splitPattern[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        try {
            off = Integer.parseInt(splitPattern[1]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (on >= LED_PATTERN_MIN_SECONDS && on <= LED_PATTERN_MAX_SECONDS
                && off >= LED_PATTERN_MIN_SECONDS && off <= LED_PATTERN_MAX_SECONDS) {
            return new int[] { on, off };
        }

        return null;
    }

    public static void notifySendFailed(Context context) {
        PopupNotification n = buildNotification(context, false, NOTIFICATION_SEND_FAILED);
        if (n == null)
            return;

        // The notification title, sub-text and text that will scroll
        String contentTitle;
        String contentText;
        // SpannableString scrollText;

        long[] threadIdResult = { 0 };
        int failedCount = getUndeliveredMessageCount(context, threadIdResult);

        // The default intent when the notification is clicked (Inbox)
        Intent smsIntent = SmsPopupUtils.getSmsInboxIntent();

        // If more than one message failed
        if (failedCount > 1) {
            contentTitle = "Multiple errors";
            contentText = "Multiple errors when sending messages";
            // smsIntent = SMSPopupUtils.getSmsIntent();
        } else { // Else 1 message failed
            contentTitle = "Error sending message";
            contentText = "Error sending message";
            long threadId = (threadIdResult[0] != 0 ? threadIdResult[0] : 0);
            smsIntent = SmsPopupUtils.getSmsToIntent(context, threadId);
        }

        // Set the icon, scrolling text and timestamp
        n.notificationBuilder.setSmallIcon(n.notifFailedIcon)
                .setTicker(contentTitle)
                .setWhen(System.currentTimeMillis());

        // Set the PendingIntent if the status message is clicked
        PendingIntent notifIntent = PendingIntent.getActivity(context, 0, smsIntent, 0);

        // Set the messages that show when the status bar is pulled down
        n.notificationBuilder
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(notifIntent);

        n.notify(context, NOTIFICATION_SEND_FAILED);
    }

    // threadIdResult[0] contains the thread id of the first message.
    // threadIdResult[1] is nonzero if the thread ids of all the messages are the same.
    // You can pass in null for threadIdResult.
    // You can pass in a threadIdResult of size 1 to avoid the comparison of each thread id.
    private static int getUndeliveredMessageCount(Context context, long[] threadIdResult) {

        // TODO: switch projection to use common static variables
        Cursor undeliveredCursor = context.getContentResolver().query(
                UNDELIVERED_URI, new String[] { "thread_id" // Mms.THREAD_ID
                }, "read=0", null, null);
        if (undeliveredCursor == null) {
            return 0;
        }
        int count = undeliveredCursor.getCount();
        try {
            if (threadIdResult != null && undeliveredCursor.moveToFirst()) {
                threadIdResult[0] = undeliveredCursor.getLong(0);

                if (threadIdResult.length >= 2) {
                    // Test to see if all the undelivered messages belong to the same thread.
                    long firstId = threadIdResult[0];
                    while (undeliveredCursor.moveToNext()) {
                        if (undeliveredCursor.getLong(0) != firstId) {
                            firstId = 0;
                            break;
                        }
                    }
                    threadIdResult[1] = firstId; // non-zero if all ids are the same
                }
            }
        } finally {
            undeliveredCursor.close();
        }
        return count;
    }

    public static void updateSendFailedNotification(Context context) {
        if (getUndeliveredMessageCount(context, null) < 1) {
            clear(context, NOTIFICATION_SEND_FAILED);
        } else {
            notifySendFailed(context); // rebuild and adjust the message count if necessary.
        }
    }

}
