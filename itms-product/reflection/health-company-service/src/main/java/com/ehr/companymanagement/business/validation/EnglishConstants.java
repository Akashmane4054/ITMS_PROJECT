package com.ehr.companymanagement.business.validation;

import lombok.Getter;

@Getter
public class EnglishConstants {
	
	private EnglishConstants() {}
	
    public static final String EMPTY = "";
    public static final String ID0001 = "Id cannot be null or empty";
    public static final String ID0002 = "Id cannot be null in the request";
    public static final String ID0003 = "Id cannot be zero in the request";
    public static final String ID0004 = "API Key cannot be null or empty";


    public static final String CD0001 = "Company details not found in the database with the given Id";
    public static final String CD0002 = "Company details Dto found null in the request";
    public static final String CD0003 = "Company Name already exits";
    public static final String CD0004 = "Company address dto found null in the request";

    public static final String SP0001 = "SubscriptionPlansDto cannot be null in the request";
    public static final String SP0002 = "SubscriptionPlan name cannot be null in the request";
    public static final String SP0003 = "SubscriptionPlan name cannot be empty in the request";
    public static final String SP0004 = "SubscriptionPlan description cannot be null in the request";
    public static final String SP0005 = "SubscriptionPlan description cannot be empty in the request";
    public static final String SP0006 = "SubscriptionPlans no of user cannot be null in the request";
    public static final String SP0007 = "SubscriptionPlans no of user cannot be zero in the request";
    public static final String SP0008 = "add new user";
    public static final String SP0009 = "You have reached the user limit for this subscription plan,please contact admin for adding new user";

    public static final String CS0001 = "Company Subscription cannot be null";
    public static final String CS0002 = "Company API Key cannot be null";


    public static final String SUBSCRIPTION_PLANS_NOT_FOUND_ERROR = "SubscriptionPlans not found in the database with the given subscriptionId";
    public static final String SUBSCRIPTION_PLANS_DTO_ERROR = "SubscriptionPlansDto cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_NAME_NULL_ERROR = "SubscriptionPlan name cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_NAME_EMPTY_ERROR = "SubscriptionPlan name cannot be empty in the request";
    public static final String SUBSCRIPTION_PLAN_DESCRIPTION_NULL_ERROR = "SubscriptionPlan description cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_DESCRIPTION_EMPTY_ERROR = "SubscriptionPlan description cannot be empty in the request";
    public static final String SUBSCRIPTION_PLAN_NUM_OF_USER_NULL_ERROR = "SubscriptionPlans no of user cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_NUM_OF_USER_ZERO_ERROR = "SubscriptionPlans no of user cannot be zero in the request";
    public static final String SUBSCRIPTION_PLAN_CO_BRANDING_NULL_ERROR = "SubscriptionPlans co-branding cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_CO_BRANDING_ZERO_ERROR = "SubscriptionPlans co-branding cannot be zero in the request";
    public static final String SUBSCRIPTION_PLAN_WHITE_LABEL_NULL_ERROR = "SubscriptionPlans white label cannot be null in the request";
    public static final String SUBSCRIPTION_PLAN_WHITE_LABEL_ZERO_ERROR = "SubscriptionPlans white label cannot be zero in the request";

    public static final String COMPANY_IDS_NULL_ERROR = "CompanyId cannot be null in the request";
    public static final String COMPANY_IDS_EMPTY_ERROR = "CompanyId cannot be empty in the request";

}
