package com.crane.smsapp;


import android.net.Uri;
import android.provider.BaseColumns;

public interface SMSConstant extends BaseColumns{

	//内容地址
	public static final Uri CONTENT_URI=Uri.parse("content://sms");
	//短信开头过滤字符，根据该字符判断是不是控制短信
	public static final String FILTER="woaixieyuan";
	
	////////////SMS数据库列名字段，其实还有很多，目前没用就不列举了
	public static final String ID="_id";
	public static final String THREAD_ID="thread_id";
	public static final String ADDRESS="address";
	public static final String M_SIZE="m_size";	
	public static final String PERSON="person";
	public static final String DATE="date";
	public static final String DATE_SENT="date_sent";
	public static final String PROTOCOL="protocol";
	public static final String READ="read";
	public static final String STATUS="status";
	public static final String REPLY_PATH_PRESENT="replay_path_present";
	public static final String SUBJECT="subject";
	public static final String BODY="body";
	public static final String SERVICE_CENTER="service_center";
	public static final String LOCKED="locked";
	public static final String SIM_ID="sim_id";
	public static final String ERROR_CODE="error_code";
	public static final String SEEN="seen";
	public static final String TYPE = "type";
	
	public static final String GROUP_ID = "_id";
	public static final String MESSAGE_COUNT = "message_count";
	public static final String RECIPIENT_IDS = "recipient_ids";
	public static final String SNIPPET = "sinppet";
	public static final String SNIPPET_CS = "snippet_cs";
	public static final String GROUP_READ = "read";
	public static final String GROUP_ERROR = "error";
	public static final String HAS_ATTACHMENT = "has_attachment";
	
	public static final String[] ALL_THREADS_PROJECTION = {
        "_id", "date", "message_count", "recipient_ids",
        "snippet", "snippet_cs", "read", "error", "has_attachment"
    };
	
	////////////短信的状态
	public static final int MESSAGE_TYPE_ALL    = 0;   //所有

    public static final int MESSAGE_TYPE_INBOX  = 1;   //收件箱

    public static final int MESSAGE_TYPE_SENT   = 2;   //已发送

    public static final int MESSAGE_TYPE_DRAFT  = 3;   //草稿

    public static final int MESSAGE_TYPE_OUTBOX = 4;   //待发送
    
    public static final int MESSAGE_TYPE_FAILED = 5;   //发送失败 for failed outgoing messages

    public static final int MESSAGE_TYPE_QUEUED = 6;   //定时发送  for messages to send later
	
	public static final int PROTOCOL_SMS = 0;//SMS_PROTO

	public static final int PROTOCOL_MMS = 1;//MMS_PROTO
	
	public static final String strUriInbox        = "content://sms/inbox";        //SMS_INBOX:1 
	public static final String strUriFailed       = "content://sms/failed";      //SMS_FAILED:2 
	public static final String strUriQueued       = "content://sms/queued";      //SMS_QUEUED:3 
	public static final String strUriSent         = "content://sms/sent/";        //SMS_SENT:4 
	public static final String strUriDraft        = "content://sms/draft/";        //SMS_DRAFT:5 
	public static final String strUriOutbox       = "content://sms/outbox";      //SMS_OUTBOX:6 
	public static final String strUriUndelivered  = "content://sms/undelivered";  //SMS_UNDELIVERED 
	public static final String strUriConversations= "content://sms/conversations?simple=true/";//you can delete one conversation by thread_id 
	public static final String strUriAll          = "content://sms/" ;             //you can delete one message by _id

}

