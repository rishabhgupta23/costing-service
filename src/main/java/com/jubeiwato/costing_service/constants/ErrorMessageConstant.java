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
    public static final String CHILD_PART_NOT_FOUND_TEMPLATE = "Child Part not found with ID:  %s";
    public static final String INVALID_PRICE_MODE = "Invalid price mode provided.";
    public static final String VENDOR_DOES_NOT_EXIST = "Vendor does not exist";
    public static final String COMPANY_DOES_NOT_EXIST = "Company does not exist";
    public static final String PART_ATTRIBUTE_NOT_NULL = "Part attribute name must not be null or empty";
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
    public static final String COMPANY_ID_REQUIRED = "Company ID is required.";
    public static final String UNAUTHORIZED_ACCESS = "User not found.";
    public static final String PART_NOT_FOUND = "Part not found";
    public static final String INVALID_COMPANY = "Company does not exist";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String PASSWORD_INCORRECT = "Password Incorrect";
    public static final String CATEGORY_DOES_NOT_EXIST = "Category does not exist";
    public static final String PART_NUMBER_ALREADY_EXISTS_TEMPLATE = "Part with partNumber '%s' already exists";
    public static final String COST_FACTOR_DOES_NOT_EXIST = "Costfactor does not exist";
    public static final String SUPER_ADMIN_CREATION_ERROR = "Super Admins cannot be created or updated";
    public static final String ADMIN_CREATION_RESTRICTED = "Admins cannot be created or updated";
    public static final String ATTRIBUTE_NOT_FOUND = "Part attribute not found.";
    public static final String ATTRIBUTE_MARKED_DELETED = "Part attribute is marked as deleted and cannot be updated.";
    public static final String ATTRIBUTE_ALREADY_EXISTS = "Attribute with the same name already exists.";
    public static final String TEMPLATE_NULL ="Template name must not be null or empty";
    public static final String UPDATE_NOT_ALLOWED_SOFT_DELETED = "Update not allowed. A soft-deleted part attribute with the name '%s' already exists.";

    public static final String CATEGORY_ALREADY_EXISTS_TEMPLATE = "Category with categoryName '%s' already exists";
    public static final String TEMPLATE_NOT_FOUND = "Template not found";
    public static final String TEMPLATE_ALREADY_EXISTS = "Template name already exists for the company.";
    public static final String ATTRIBUTE_NOT_SELECTED = "Cannot create empty template, selecting part attribute is mandatory.";


    public static final String COST_FACTOR_ALREADY_EXISTS_TEMPLATE = "Cost factor with name '%s' already exists";
    public static final String COST_FACTOR_CREATED = "Cost Factor created successfully";
    public static final String INVALID_COST_FACTOR = "Cost Factor can not be empty or null";
    public static final String COST_FACTOR_SOFT_DELETED_EXISTS_TEMPLATE = "A deactivated cost factor with name '%s' already exists. To activate it, please create a cost factor with this name.";

    private ErrorMessageConstant() {

    }

    /**
     * Generic method to format error messages dynamically.
     * 
     * @param template The message template with placeholders.
     * @param args     Values to replace placeholders.
     * @return Formatted error message.
     */
    public static String getFormattedMessage(String template, Object... args) {
        return String.format(template, args);
    }
}