/**
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crane.sms.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crane.helloworld.R;
import com.crane.smsapp.SMSConstant;
import com.crane.smsapp.SmsApp;
import com.crane.utils.AddressUtils;
import com.google.android.mms.pdu.PduHeaders;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.URLSpan;

/***
 * An utility class for managing messages.
 */
public class MessageUtils {

	private static String sLocalNumber;
	public static final char WILD = 'N';

	public static String getLocalNumber() {
		if (null == sLocalNumber) {
			sLocalNumber = SmsApp.getApplication()
					.getTelephonyManager().getLine1Number();
		}
		return sLocalNumber;
	}

	public static boolean isLocalNumber(String number) {
		if (number == null) {
			return false;
		}

		// we don't use Mms.isEmailAddress() because it is too strict for
		// comparing addresses like
		// "foo+caf_=6505551212=tmomail.net@gmail.com", which is the 'from'
		// address from a forwarded email
		// message from Gmail. We don't want to treat
		// "foo+caf_=6505551212=tmomail.net@gmail.com" and
		// "6505551212" to be the same.
		if (number.indexOf('@') >= 0) {
			return false;
		}

		return PhoneNumberUtils.compare(number, getLocalNumber());
	}

	public static String parseMmsAddress(String address) {
		if (isEmailAddress(address)) {
			return address;
		} else {
			return null;
		}
	}

	public static boolean isEmailAddress(String strEmail)

	{

		String strPattern = "^[a-zA-Z][//w//.-]*[a-zA-Z0-9]@[a-zA-Z0-9][//w//.-]*[a-zA-Z0-9]//.[a-zA-Z][a-zA-Z//.]*[a-zA-Z]$";

		Pattern p = Pattern.compile(strPattern);

		Matcher m = p.matcher(strEmail);

		return m.matches();

	}

	public static String formatTimeStampString(Context context, long when) {
		return formatTimeStampString(context, when, false);
	}

	public static String formatTimeStampString(Context context, long when,
			boolean fullFormat) {
		Time then = new Time();
		then.set(when);
		Time now = new Time();
		now.setToNow();

		// Basic settings for formatDateTime() we want for all cases.
		@SuppressWarnings("deprecation")
		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT
				| DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

		// If the message is from a different year, show the date and year.
		if (then.year != now.year) {
			format_flags |= DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_SHOW_DATE;
		} else if (then.yearDay != now.yearDay) {
			// If it is from a different day than today, show only the date.
			format_flags |= DateUtils.FORMAT_SHOW_DATE;
		} else {
			// Otherwise, if the message is from today, show the time.
			format_flags |= DateUtils.FORMAT_SHOW_TIME;
		}

		// If the caller has asked for full details, make sure to show the date
		// and time no matter what we've determined above (but still make
		// showing
		// the year only happen if it is a different year from today).
		if (fullFormat) {
			format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
		}

		return DateUtils.formatDateTime(context, when, format_flags);
	}

	public static boolean isPhoneNumber(String phoneNumber) {
		boolean isValid = false;
		/*
		 * 可接受的电话格式有：
		 */
		String expression = "^((\\+86)|(86))?\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";
		/*
		 * 可接受的电话格式有：
		 */
		String expression2 = "^((\\+86)|(86))?\\(?(\\d{3})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";

		// String expression3 = "^((\\+86)|(86))?(13)\\d{9}$";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);

		Pattern pattern2 = Pattern.compile(expression2);
		Matcher matcher2 = pattern2.matcher(inputStr);

		// Pattern pattern3 = Pattern.compile(expression3);
		// Matcher matcher3 = pattern3.matcher(inputStr);
		if (matcher.matches() || matcher2.matches() /* || matcher3.matches() */) {
			isValid = true;
		}

		if (phoneNumber.equals("10010") || phoneNumber.equals("10086")
				|| phoneNumber.equals("10000") || phoneNumber.equals("1008611")
				|| phoneNumber.equals("1001011")) {
			isValid = true;
		}

		return isValid;
	}

	/***
	 * Returns true iff the folder (message type) identifies an outgoing
	 * message.
	 */
	public static boolean isOutgoingFolder(int messageType) {
		return (messageType == SMSConstant.MESSAGE_TYPE_FAILED)
				|| (messageType == SMSConstant.MESSAGE_TYPE_OUTBOX)
				|| (messageType == SMSConstant.MESSAGE_TYPE_SENT)
				|| (messageType == SMSConstant.MESSAGE_TYPE_QUEUED);
	}

	public static void showDiscardDraftConfirmDialog(Context context,
			OnClickListener listener) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.discard_message)
				.setMessage(R.string.discard_message_reason)
				.setPositiveButton(R.string.yes, listener)
				.setNegativeButton(R.string.no, null).show();
	}

	public static ArrayList<String> extractUris(URLSpan[] spans) {
		int size = spans.length;
		ArrayList<String> accumulator = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			accumulator.add(spans[i].getURL());
		}
		return accumulator;
	}

	public static String replaceUnicodeDigits(String number) {
		StringBuilder normalizedDigits = new StringBuilder(number.length());
		for (char c : number.toCharArray()) {
			int digit = Character.digit(c, 10);
			if (digit != -1) {
				normalizedDigits.append(digit);
			} else {
				normalizedDigits.append(c);
			}
		}
		return normalizedDigits.toString();
	}
	
	public static String getMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor == null) {
            return null;
        }

        if ("mms".equals(cursor.getString(MessageListAdapter.COLUMN_MSG_TYPE))) {
//            int type = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_TYPE);
//            switch (type) {
//                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
//                    return getNotificationIndDetails(context, cursor);
//                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
//                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
//                    return getMultimediaMessageDetails(context, cursor, size);
//                default:
//                    Log.w(TAG, "No details could be retrieved.");
//                    return "";
//            }
        	 return "";
        } else {
            return getTextMessageDetails(context, cursor);
        }
    }
	
	 private static String getTextMessageDetails(Context context, Cursor cursor) {
//	        Log.d(TAG, "getTextMessageDetails");

	        StringBuilder details = new StringBuilder();
	        Resources res = context.getResources();

	        // Message Type: Text message.
	        details.append(res.getString(R.string.message_type_label));
	        details.append(res.getString(R.string.text_message));

	        // Address: ***
	        details.append('\n');
	        int smsType = cursor.getInt(MessageListAdapter.COLUMN_SMS_TYPE);
	        if (MessageUtils.isOutgoingFolder(smsType)) {
	            details.append(res.getString(R.string.to_address_label));
	        } else {
	            details.append(res.getString(R.string.from_label));
	        }
	        details.append(cursor.getString(MessageListAdapter.COLUMN_SMS_ADDRESS));

	        // Sent: ***
	        if (smsType == Sms.MESSAGE_TYPE_INBOX) {
	            long date_sent = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE_SENT);
	            if (date_sent > 0) {
	                details.append('\n');
	                details.append(res.getString(R.string.sent_label));
	                details.append(MessageUtils.formatTimeStampString(context, date_sent, true));
	            }
	        }

	        // Received: ***
	        details.append('\n');
	        if (smsType == Sms.MESSAGE_TYPE_DRAFT) {
	            details.append(res.getString(R.string.saved_label));
	        } else if (smsType == Sms.MESSAGE_TYPE_INBOX) {
	            details.append(res.getString(R.string.received_label));
	        } else {
	            details.append(res.getString(R.string.sent_label));
	        }

	        long date = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE);
	        details.append(MessageUtils.formatTimeStampString(context, date, true));

	        // Delivered: ***
	        if (smsType == Sms.MESSAGE_TYPE_SENT) {
	            // For sent messages with delivery reports, we stick the delivery time in the
	            // date_sent column (see MessageStatusReceiver).
	            long dateDelivered = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE_SENT);
	            if (dateDelivered > 0) {
	                details.append('\n');
	                details.append(res.getString(R.string.delivered_label));
	                details.append(MessageUtils.formatTimeStampString(context, dateDelivered, true));
	            }
	        }

	        // Error code: ***
	        int errorCode = cursor.getInt(MessageListAdapter.COLUMN_SMS_ERROR_CODE);
	        if (errorCode != 0) {
	            details.append('\n')
	                .append(res.getString(R.string.error_code_label))
	                .append(errorCode);
	        }

	        return details.toString();
	    }
	 
	 /**
	     * Normalize a phone number by removing the characters other than digits. If
	     * the given number has keypad letters, the letters will be converted to
	     * digits first.
	     *
	     * @param phoneNumber
	     *            the number to be normalized.
	     * @return the normalized number.
	     *
	     * @hide
	     */
	    public static String normalizeNumber(String phoneNumber) {
	        StringBuilder sb = new StringBuilder();
	        int len = phoneNumber.length();
	        for (int i = 0; i < len; i++) {
	            char c = phoneNumber.charAt(i);
	            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
	            int digit = Character.digit(c, 10);
	            if (digit != -1) {
	                sb.append(digit);
	            } else if (i == 0 && c == '+') {
	                sb.append(c);
	            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
	                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
	            }
	        }
	        return sb.toString();
	    }
	    
	    @SuppressLint("NewApi")
		public static void handleReadReport(final Context context,
	            final Collection<Long> threadIds,
	            final int status,
	            final Runnable callback) {
	        StringBuilder selectionBuilder = new StringBuilder(Mms.MESSAGE_TYPE + " = "
	                + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
	                + " AND " + Mms.READ + " = 0"
	                + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES);

	        String[] selectionArgs = null;
	        if (threadIds != null) {
	            String threadIdSelection = null;
	            StringBuilder buf = new StringBuilder();
	            selectionArgs = new String[threadIds.size()];
	            int i = 0;

	            for (long threadId : threadIds) {
	                if (i > 0) {
	                    buf.append(" OR ");
	                }
	                buf.append(Mms.THREAD_ID).append("=?");
	                selectionArgs[i++] = Long.toString(threadId);
	            }
	            threadIdSelection = buf.toString();

	            selectionBuilder.append(" AND (" + threadIdSelection + ")");
	        }

	        final Cursor c = context.getContentResolver().query(
	                        Mms.Inbox.CONTENT_URI, new String[] {Mms._ID, Mms.MESSAGE_ID},
	                        selectionBuilder.toString(), selectionArgs, null);

	        if (c == null) {
	            return;
	        }

	        final Map<String, String> map = new HashMap<String, String>();
	        try {
	            if (c.getCount() == 0) {
	                if (callback != null) {
	                    callback.run();
	                }
	                return;
	            }

	            while (c.moveToNext()) {
	                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
	                map.put(c.getString(1), AddressUtils.getFrom(context, uri));
	            }
	        } finally {
	            c.close();
	        }

	        OnClickListener positiveListener = new OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                for (final Map.Entry<String, String> entry : map.entrySet()) {
//	                    MmsMessageSender.sendReadRec(context, entry.getValue(),
//	                                                 entry.getKey(), status);
	                }

	                if (callback != null) {
	                    callback.run();
	                }
	                dialog.dismiss();
	            }
	        };

	        OnClickListener negativeListener = new OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                if (callback != null) {
	                    callback.run();
	                }
	                dialog.dismiss();
	            }
	        };

	        OnCancelListener cancelListener = new OnCancelListener() {
	            @Override
	            public void onCancel(DialogInterface dialog) {
	                if (callback != null) {
	                    callback.run();
	                }
	                dialog.dismiss();
	            }
	        };

	        confirmReadReportDialog(context, positiveListener,
	                                         negativeListener,
	                                         cancelListener);
	    }

	    private static void confirmReadReportDialog(Context context,
	            OnClickListener positiveListener, OnClickListener negativeListener,
	            OnCancelListener cancelListener) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        builder.setCancelable(true);
	        builder.setTitle(R.string.confirm);
	        builder.setMessage(R.string.message_send_read_report);
	        builder.setPositiveButton(R.string.yes, positiveListener);
	        builder.setNegativeButton(R.string.no, negativeListener);
	        builder.setOnCancelListener(cancelListener);
	        builder.show();
	    }


	    /**
	     * Format the given phoneNumber to the E.164 representation.
	     * <p>
	     * The given phone number must have an area code and could have a country
	     * code.
	     * <p>
	     * The defaultCountryIso is used to validate the given number and generate
	     * the E.164 phone number if the given number doesn't have a country code.
	     *
	     * @param phoneNumber
	     *            the phone number to format
	     * @param defaultCountryIso
	     *            the ISO 3166-1 two letters country code
	     * @return the E.164 representation, or null if the given phone number is
	     *         not valid.
	     *
	     * @hide
	     */
	    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
	        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
	        String result = null;
	        try {
	            PhoneNumber pn = util.parse(phoneNumber, defaultCountryIso);
	            if (util.isValidNumber(pn)) {
	                result = util.format(pn, PhoneNumberFormat.E164);
	            }
	        } catch (NumberParseException e) {
	        }
	        return result;
	    }
	    
	    /**
	     * Format the phone number only if the given number hasn't been formatted.
	     * <p>
	     * The number which has only dailable character is treated as not being
	     * formatted.
	     *
	     * @param phoneNumber
	     *            the number to be formatted.
	     * @param phoneNumberE164
	     *            the E164 format number whose country code is used if the given
	     *            phoneNumber doesn't have the country code.
	     * @param defaultCountryIso
	     *            the ISO 3166-1 two letters country code whose convention will
	     *            be used if the phoneNumberE164 is null or invalid, or if phoneNumber
	     *            contains IDD.
	     * @return the formatted number if the given number has been formatted,
	     *            otherwise, return the given number.
	     *
	     * @hide
	     */
	    public static String formatNumber(
	            String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
	        int len = phoneNumber.length();
	        for (int i = 0; i < len; i++) {
	            if (!isDialable(phoneNumber.charAt(i))) {
	                return phoneNumber;
	            }
	        }
	        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
	        // Get the country code from phoneNumberE164
	        if (phoneNumberE164 != null && phoneNumberE164.length() >= 2
	                && phoneNumberE164.charAt(0) == '+') {
	            try {
	                // The number to be parsed is in E164 format, so the default region used doesn't
	                // matter.
	                PhoneNumber pn = util.parse(phoneNumberE164, "ZZ");
	                String regionCode = util.getRegionCodeForNumber(pn);
	                if (!TextUtils.isEmpty(regionCode) &&
	                    // This makes sure phoneNumber doesn't contain an IDD
	                    normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(1)) <= 0) {
	                    defaultCountryIso = regionCode;
	                }
	            } catch (NumberParseException e) {
	            }
	        }
	        String result = formatNumber(phoneNumber, defaultCountryIso);
	        return result != null ? result : phoneNumber;
	    }
	    
	    /**
	     * Format a phone number.
	     * <p>
	     * If the given number doesn't have the country code, the phone will be
	     * formatted to the default country's convention.
	     *
	     * @param phoneNumber
	     *            the number to be formatted.
	     * @param defaultCountryIso
	     *            the ISO 3166-1 two letters country code whose convention will
	     *            be used if the given number doesn't have the country code.
	     * @return the formatted number, or null if the given number is not valid.
	     *
	     * @hide
	     */
	    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
	        // Do not attempt to format numbers that start with a hash or star symbol.
	        if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
	            return phoneNumber;
	        }

	        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
	        String result = null;
	        try {
	            PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
	            result = util.formatInOriginalFormat(pn, defaultCountryIso);
	        } catch (NumberParseException e) {
	        }
	        return result;
	    }

	    private static boolean isDialable(String address) {
	        for (int i = 0, count = address.length(); i < count; i++) {
	            if (!isDialable(address.charAt(i))) {
	                return false;
	            }
	        }
	        return true;
	    }
	    
	    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD  */
	    public final static boolean
	    isDialable(char c) {
	        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
	    }
}
