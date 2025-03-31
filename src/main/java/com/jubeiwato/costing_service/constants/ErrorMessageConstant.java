package com.jubeiwato.costing_service.constants;

public class ErrorMessageConstant {  
    public static final String UNIT_CANNOT_BE_NULL_OR_EMPTY = "Unit cannot be null or empty";
    public static final String INVALID_UNIT = "Invalid Unit";
    public static final String INVALID_PART_ID = "Invalid Part ID";
    public static final String PART_DOESNOT_EXIST = "Part does not exist";
    public static final String INVALID_CATEGORY = "Invalid Category";
    public static final String INVALID_CHILD_PART = "Invalid Child Part";
    public static final String RESTRICT_CHILD_PART_DELETE = "Cannot Delete this part it is in BOM of other Part(s). Please remove from BOM first to delete the part.";
    public static final String PART_NOT_FOUND_TEMPLATE = "Part with ID %s does not exist";
    public static final String USER_DOES_NOT_EXIST = "User does not exist";
    public static final String USER_NOT_FOUND_TEMPLATE = "User with ID %s does not exist";
    public static final String USER_EMAIL_NOT_FOUND_TEMPLATE = "User with email ID %s does not exist";
    public static final String INVALID_INPUT = "Invalid input: Prohibited characters detected.";
    public static final String CHILD_PART_NOT_FOUND_TEMPLATE= "Child Part not found with ID:  %s";
    public static final String INVALID_PRICE_MODE= "Invalid price mode provided.";
    public static final String VENDOR_DOES_NOT_EXIST= "Vendor does not exist";

    public static final String JWT_TOKEN_EXPIRED = "JWT token has expired";
    public static final String UNEXPECTED_ERROR_OCCURED = "unexpected error occured";
    public static final String JWT_TOKEN_MISSING = "JWT token is missing";
    public static final String USER_ALREADY_EXISTS = "User with this email already exists";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String SUPER_ADMIN_DELETE_ERROR = "Super Admins cannot be deleted.";
    public static final String ADMIN_DELETE_ERROR = "Only Super Admins can delete Admins.";
    public static final String COMPANY_NOT_FOUND = "Current user does not belong to any company";
    public static final String INVALID_USER_ROLE = "Invalid role found for user.";
    public static final String DISPLAY_NAME_REQUIRED = "Display name is required.";
    public static final String EMAIL_REQUIRED = "Email ID is required.";
    public static final String ROLE_REQUIRED = "User role is required.";





    private ErrorMessageConstant() {

    }
    
    /**
     * Generic method to format error messages dynamically.
     * @param template The message template with placeholders.
     * @param args Values to replace placeholders.
     * @return Formatted error message.
     */
    public static String getFormattedMessage(String template, Object... args) {
        return String.format(template, args);
    }
}