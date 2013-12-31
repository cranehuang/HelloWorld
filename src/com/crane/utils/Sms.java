package com.crane.utils;

import com.google.android.mms.util.SqliteWrapper;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony.TextBasedSmsColumns;

/***
 * Contains all text based SMS messages.
 */
@SuppressLint("NewApi")
public final class Sms implements BaseColumns ,TextBasedSmsColumns{
    public static final Cursor query(ContentResolver cr, String[] projection) {
        return cr.query(CONTENT_URI, projection, null, null, DEFAULT_SORT_ORDER);
    }

    public static final Cursor query(ContentResolver cr, String[] projection,
            String where, String orderBy) {
        return cr.query(CONTENT_URI, projection, where,
                                     null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
    }

    /***
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
        Uri.parse("content://sms");

    /***
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "date DESC";

    /***
     * Add an SMS to the given URI.
     *
     * @param resolver the content resolver to use
     * @param uri the URI to add the message to
     * @param address the address of the sender
     * @param body the body of the message
     * @param subject the psuedo-subject of the message
     * @param date the timestamp for the message
     * @param read true if the message has been read, false if not
     * @param deliveryReport true if a delivery report was requested, false if not
     * @return the URI for the new message
     */
    public static Uri addMessageToUri(ContentResolver resolver,
            Uri uri, String address, String body, String subject,
            Long date, boolean read, boolean deliveryReport) {
        return addMessageToUri(resolver, uri, address, body, subject,
                date, read, deliveryReport, -1L);
    }

    /***
     * Add an SMS to the given URI with thread_id specified.
     *
     * @param resolver the content resolver to use
     * @param uri the URI to add the message to
     * @param address the address of the sender
     * @param body the body of the message
     * @param subject the psuedo-subject of the message
     * @param date the timestamp for the message
     * @param read true if the message has been read, false if not
     * @param deliveryReport true if a delivery report was requested, false if not
     * @param threadId the thread_id of the message
     * @return the URI for the new message
     */
    public static Uri addMessageToUri(ContentResolver resolver,
            Uri uri, String address, String body, String subject,
            Long date, boolean read, boolean deliveryReport, long threadId) {
        ContentValues values = new ContentValues(7);

        values.put(ADDRESS, address);
        if (date != null) {
            values.put(DATE, date);
        }
        values.put(READ, read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put(SUBJECT, subject);
        values.put(BODY, body);
        if (deliveryReport) {
            values.put(STATUS, STATUS_PENDING);
        }
        if (threadId != -1L) {
            values.put(THREAD_ID, threadId);
        }
        return resolver.insert(uri, values);
    }

    /***
     * Move a message to the given folder.
     *
     * @param context the context to use
     * @param uri the message to move
     * @param folder the folder to move to
     * @return true if the operation succeeded
     */
    public static boolean moveMessageToFolder(Context context,
            Uri uri, int folder, int error) {
        if (uri == null) {
            return false;
        }

        boolean markAsUnread = false;
        boolean markAsRead = false;
        switch(folder) {
        case MESSAGE_TYPE_INBOX:
        case MESSAGE_TYPE_DRAFT:
            break;
        case MESSAGE_TYPE_OUTBOX:
        case MESSAGE_TYPE_SENT:
            markAsRead = true;
            break;
        case MESSAGE_TYPE_FAILED:
        case MESSAGE_TYPE_QUEUED:
            markAsUnread = true;
            break;
        default:
            return false;
        }

        ContentValues values = new ContentValues(3);

        values.put(TYPE, folder);
        if (markAsUnread) {
            values.put(READ, Integer.valueOf(0));
        } else if (markAsRead) {
            values.put(READ, Integer.valueOf(1));
        }
        values.put(ERROR_CODE, error);

        return 1 == SqliteWrapper.update(context, context.getContentResolver(),
                        uri, values, null, null);
    }

    /***
     * Returns true iff the folder (message type) identifies an
     * outgoing message.
     */
    public static boolean isOutgoingFolder(int messageType) {
        return  (messageType == MESSAGE_TYPE_FAILED)
                || (messageType == MESSAGE_TYPE_OUTBOX)
                || (messageType == MESSAGE_TYPE_SENT)
                || (messageType == MESSAGE_TYPE_QUEUED);
    }

    /***
     * Contains all text based SMS messages in the SMS app's inbox.
     */
    @SuppressLint("NewApi")
	public static final class Inbox implements BaseColumns, TextBasedSmsColumns {
        /***
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://sms/inbox");

        /***
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /***
         * Add an SMS to the Draft box.
         *
         * @param resolver the content resolver to use
         * @param address the address of the sender
         * @param body the body of the message
         * @param subject the psuedo-subject of the message
         * @param date the timestamp for the message
         * @param read true if the message has been read, false if not
         * @return the URI for the new message
         */
        public static Uri addMessage(ContentResolver resolver,
                String address, String body, String subject, Long date,
                boolean read) {
            return addMessageToUri(resolver, CONTENT_URI, address, body,
                    subject, date, read, false);
        }
    }

    /***
     * Contains all sent text based SMS messages in the SMS app's.
     */
    public static final class Sent implements BaseColumns {
        /***
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://sms/sent");

        /***
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /***
         * Add an SMS to the Draft box.
         *
         * @param resolver the content resolver to use
         * @param address the address of the sender
         * @param body the body of the message
         * @param subject the psuedo-subject of the message
         * @param date the timestamp for the message
         * @return the URI for the new message
         */
        public static Uri addMessage(ContentResolver resolver,
                String address, String body, String subject, Long date) {
            return addMessageToUri(resolver, CONTENT_URI, address, body,
                    subject, date, true, false);
        }
    }
}
