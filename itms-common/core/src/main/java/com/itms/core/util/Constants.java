package com.itms.core.util;

public class Constants {

	public static final String TECHNICAL_ERROR = "Some technical exception while performing the operation, please check logs for more info.";
	public static final String INVALID_CREDENTIALS = "Invalid credentials. Please verify";
	public static final Long COMMON_PROFILE_IMAGE = 1468L;
	public static final String IS_ALREADY_LOGGIN = "isAlreadyLoggin";
	public static final String FIRST_TIME_LOGIN = "firstTimeLogin";
	public static final String USER_AUTHENTICATED_SUCCESSFULLY = "User Authenticated Successfully";
	public static final String USER_IS_BLOCK_FOR_1_HOUR = "User is blocked for next 1 hour";
	public static final String X_B3_TraceId = "X-B3-TraceId";
	public static final String X_B3_SpanId = "X-B3-SpanId";

	public static final String SPACE = " ";
	public static final String EMPTYSTRING = "";
	public static final String HYPHEN = "-";
	public static final String SVM_VALUE = "Svm_Value";

	public static final Integer SIX = 6;
	public static final Integer FIFTY = 50;
	public static final String LIKE = "LIKE";
	public static final String LIKE_STRING = "%LIKE%";

	public static final String USER_MASTER_ID = "userMasterId";
	public static final String USER_MASTER = "userMaster";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String USER_LANGUAGE = "userLanguage";
	public static final String USER_NAME = "userName";
	public static final String USER_FIRST_NAME = "userFirstName";
	public static final String USER_LAST_NAME = "userLastName";
	public static final String USER_COMPANY_ID = "userCompanyId";
	public static final String EVENT_DETAILS_ID = "eventDetailsId";
	public static final String EVENT_DETAILS = "eventDetails";
	public static final String USER_EMAIL = "userEmail";
	public static final String COUNTRY_ID = "countryId";
	public static final String DATE_FORMAT = "dateFormat";
	public static final String HISTORY_FEED_ID = "historyFeedId";
	public static final String COMPANY_NAME = "companyName";
	public static final String TYPE = "type";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String ERROR_CODE = "errorCode";
	public static final String FAILED = "Failed";
	public static final String TYPE_NAME = "typeName";
	public static final String DATA = "data";

	public static final String USER_COUNT = "userCount";
	public static final String USERS_COUNT = "usersCount";
	public static final String USER_EXISTS = "userExists";
	public static final String ERROR_COUNT = "errorCount";
	public static final String SUCCESS_COUNT = "successCount";
	public static final String TIME_ZONE = "timeZone";

	public static final String PROCESS_COUNT = "processCount";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";

	public static final String COLUMN = "columns";
	public static final String TOTAL_COUNT = "totalCount";
	public static final String TOTAL_USER = "attendee";
	public static final String STATUS_FILE = "statusFile";
	public static final String FILE_NAME_WITH_EXTENSION = "fileNameWithExtension";
	public static final String ACTIVE = "active";
	public static final String COMPANY_ID = "companyId";
	public static final String STATUS = "status";
	public static final String IS_ROLE_FILTER = "isRoleFilter";

	public static final String SORT = "sort";
	public static final String SEARCH = "search";
	public static final String DEFAULT_SORT = "defaultSort";

	public static final String NA = "N/A";
	public static final Integer ONE_BILLION = 1000000000;
	public static final String ROLELIST = "RoleList";
	public static final String SERIAL_ID = "serialId";
	public static final String IS_FIRST_LOGIN = "isFirstLogin";
	public static final String ROLES = "roles";
	public static final String USER_FULL_NAME = "userFullName";
	public static final String FULL_NAME = "fullName";
	public static final String USER_TIME_ZONE = "userTimeZone";
	public static final String SUPER_ADMIN_ID = "superAdminId";
	public static final String ADDRESS_ID = "AddressId";
	public static final String ADDRESS_DETAILS = "AddressDetails";
	public static final String USERS = "users";
	public static final String USER_LIST = "userList";
	public static final String COUNT = "count";
	public static final String EVENT_CREATED = "eventCreated";
	public static final String COUNTER = "Counter";
	public static final String USER_ID = "userId";
	public static final String NULL_STRING = "null";
	public static final String APIKEY = "apiKey";
	public static final String AUTHORIZATION = "authorization";

	public static final String ERROR_LOG = "Error log: ";

	public static final String CUSTOM_AUTH = "customAuth";
	public static final Integer one = 1;

	public static final String DTO = "dto";
	public static final Integer ZERO = 0;
	public static final Long ZERO_LONG = 0L;
	public static final String PATIENT_ID = "patientId";
	public static final String APPOINTMENT_ID = "appointmentId";
	public static final Long RESTAURANT_PRODUCT_ID = 0L;
	public static final Long OUTLET_ADMIN_ID = 6L;
	public static final Long OUTLET_SUB_ADMIN_ID = 7L;
	public static final String USER_ROLES = "User Roles : ";
	public static final String EVENT_COUNT = "Event_Count";

	public interface Gateway {
		String cName = "SFE";
		String productId = "MF";

		String aadhaarServiceId = "AADHAAR";
		String smsServiceId = "SMS";
		String emailServiceId = "EMAIL";
		String cibilServiceId = "CIBIL";
		String dlServiceId = "DL";
		String voterServiceId = "VOTERID";
		String panServiceId = "PAN";
		String hdfcServiceId = "HDFC";

		String epfServiceId = "EPF";
		String gstServiceId = "GST";

	}

	public static final String DTO_LIST = "dtoList";

	public static final String AVERAGE_OF_EACH_CATEGORY = "averageOfEachCategory";

	public static final String AVERAGE_OF_EACH_REVEIW_POINTS = "averageOfEachReveiwPoints";

	public static final String DB_CALL = "===DB CALL===";

	public static final String VENDOR_ID = "vendorId";

	public static final String __HOST = "__Host";

	public static final String FORWARD_SLASH = "/";

	public static final String HTTP_HTTPS = "^(http://|https://)";

	public static final String CATEGORY_ID = "categoryId";
	public static final String SYSTEM_SERVICE = "system-service/";
	public static final String LOCATION_SERVICE = "location-service/";
	public static final String CODE = "code";
	public static final String LANGUAGE_ID = "languageId";
	public static final String LOWERCASE_LANGUAGE = "language";
	public static final String XLSX = ".xlsx";

	public static final String CREATED_ON = "createdOn";
	public static final String MODIFIED_ON = "modifiedOn";
	public static final String INCIDENT_DATE = "incident date : ";
	public static final String SECTION_1 = "Section 1";
	public static final String MULTIPLE_LANGUAGE_DTO = "multipleLanguageDto";
	public static final String EDGE_SERVICE = "edge-service/";
	public static final String SCAN = "scan";
	public static final String DOT = ".";
	public static final String LISTING_RESPONSE = "listingResponse";
	public static final String COMPANY_SERVICE = "/company-service/";
	public static final String COMPANY_BRANCH_SERVICE = "/company-branch-service/";
	public static final String LIFEMART_COMPANY_SERVICE = "/lifemart-company-service/";

	public static final String OTP_SERVICE = "/otp-service/";

	public static final String DETAILED_EXCEPTION = "Detailed Exception!";

	public static final String COUNTS = "Count";

	public static final String OTHER = "other";

	public static final String WINDOWS = "windows";
	public static final String LINUX = "linux";
	public static final String MAC = "mac";

	public static final String DESKTOP = "Desktop";
	public static final String MOBILE = "Mobile";
	public static final String UNKNOWN = "Unknown";

	public static final String ANDROID = "android";
	public static final String IOS = "ios";

	public static final String PERSONAL_COMPUTER = "personal computer";
	public static final String SMARTPHONE = "smartphone";
	public static final String TABLET = "tablet";
	public static final String TABLET_CAP = "Tablet";

	public static final String HTTP = "http";
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";
	public static final String WWW_PREFIX = "www.";
	public static final String URL_SEPARATOR = "://";

	public static final String DATE_FORMAT_DD_MM_YY = "dd/MM/yy";

	public static final String ANCHOR_SELECTOR = "a[href]";
	public static final String HREF_ATTRIBUTE = "href";

	public static final String COOKIE_SCAN_DTO = "cookieScanDto";

	public static final String HYPEN = "-";

	public static final String LIST = "list";

	public static final String SYSTEM_VALUE_MASTER = "systemValueMaster";

	public static final String ROLE_ID = "roleId";

	public static final String OLD_ADDRESS_DTO = "oldAddressDTO";
	public static final String NEW_ADDRESS_DTO = "newAddressDTO";

	public static final String LISTED_SUCCESSFULLY = "listed successfully";

	public static final String RESPONSE_PLACEHOLDER = "response {}";

	public static final String IS_ALREADY_EXISTS = "isAlreadyExists";
	public static final String CONFLICT = "CONFLICT";

	public static final String USER_SERVICE = "/user-service";
	public static final String DESC = "desc";
	public static final String DOCUMENT_SERVICE = "document-service";

	// Portals
	public static final String REFLECTION = "Reflection";
	public static final String LIFE_MART = "LifeMart";
	public static final String EH_NOTE = "EhNote";

	public static final int MAX_ADDRESS_LINE_LENGTH = 50;
	public static final String PINCODE_ID = "pincodeId";
	public static final String CITY_MASTER_ID = "cityMasterId";
	public static final String CITY_MASTER_DTO = "cityMasterDto";
	public static final String STATE_ID = "stateId";
	public static final String PINCODE = "pincode";
	public static final String COMPANY_PRODUCT_DETAIL_ID = "companyProductDetailId";
	public static final String PATH = "path";

	public static final String DOCUMENT_IDS = "documentIds";
	public static final String ROLE_LIST_FOR_USER = "roleListForUser";
	public static final String SOURCE = "source";
	public static final String PROPOSAL_REF_ID = "proposalRefId";
	public static final String PROPOSAL_SERVICE = "proposal-service";
	public static final String INSURER_ID = "insurerId";
	public static final String PRODUCT_ID = "productId";
	public static final String FILE = "file";

	public static final Long OUTLET_STAFF_ID = 7L;

	public static final String IS_USER_ALREADY_EXISTS = "isUserAlreadyExists";
	
	public static final String EMAIL_OTP_VERIFIED = "emailOtpVerified";
	public static final String SMS_OTP_VERIFIED = "smsOtpVerified";
	
	public static final String ASSESSMENT_SERVICE = "/assessment-service/";
	
	public static final String PDF = "pdf";
	
	public static final String OUTLET_ID = "outletId";
	public static final String COMPANY_DETAILS = "companyDetails";
	public static final String ROLE_NAME = "roleName";
	public static final String INVOICE_ID = "invoiceId";
	
	public static final String SUBSCRIPTION_ID = "subscriptionId";
	public static final String SUBSCRIPTION_LIMIT = "subscriptionLimit";
	
	//_______________SPIRNG PROFILE_________________________
	public static final String PROD = "prod";
	
}
