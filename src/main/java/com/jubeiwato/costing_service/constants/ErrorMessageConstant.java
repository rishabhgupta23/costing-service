package com.jubeiwato.costing_service.constants;

public class ErrorMessageConstant {  
    public static final String UNIT_CANNOT_BE_NULL_OR_EMPTY = "Unit cannot be null or empty";
    public static final String INVALID_UNIT = "Invalid Unit";
    public static final String INVALID_PART_ID = "Invalid Part ID";
    public static final String PART_DOESNOT_EXIST = "Part doesnot exist";
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