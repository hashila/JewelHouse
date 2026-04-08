package com.jwelhouse.backend.constants;

/**
 * Application-wide constants for status characters, messages, and other commonly used values.
 */
public class AppConstants {

	// Status Characters
	public static final char STATUS_ACTIVE = 'A';
	public static final char STATUS_INACTIVE = 'I';
	public static final char STATUS_DELETED = 'D';
	public static final char STATUS_PENDING = 'P';
	public static final char STATUS_APPROVED = 'R';
	public static final char STATUS_REJECTED = 'X';

	// Availability Characters
	public static final char AVAILABILITY_IN_STOCK = 'S';
	public static final char AVAILABILITY_OUT_OF_STOCK = 'O';
	public static final char AVAILABILITY_LIMITED_STOCK = 'L';

	// Status Messages
	public static final String STATUS_ACTIVE_MESSAGE = "Active";
	public static final String STATUS_INACTIVE_MESSAGE = "Inactive";
	public static final String STATUS_DELETED_MESSAGE = "Deleted";
	public static final String STATUS_PENDING_MESSAGE = "Pending";
	public static final String STATUS_APPROVED_MESSAGE = "Approved";
	public static final String STATUS_REJECTED_MESSAGE = "Rejected";

	// Response Messages
	public static final String SUCCESS_MESSAGE = "Operation completed successfully";
	public static final String ERROR_MESSAGE = "An error occurred while processing your request";
	public static final String NOT_FOUND_MESSAGE = "Resource not found";
	public static final String INVALID_REQUEST_MESSAGE = "Invalid request";
	public static final String UNAUTHORIZED_MESSAGE = "Unauthorized access";
	public static final String FORBIDDEN_MESSAGE = "Forbidden access";

	// Default Values
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int DEFAULT_PAGE_NUMBER = 0;
	public static final String DEFAULT_SORT_ORDER = "ASC";

	// Pagination
	public static final String PAGE_PARAM = "page";
	public static final String PAGE_SIZE_PARAM = "pageSize";
	public static final String SORT_PARAM = "sort";

	// Private constructor to prevent instantiation
	private AppConstants() {
		throw new AssertionError("Cannot instantiate AppConstants");
	}
}

