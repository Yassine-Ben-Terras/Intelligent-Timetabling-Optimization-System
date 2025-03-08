// src/payloads/index.ts

// For user login
export interface LoginRequest {
    username?: string; // Backend might accept username
    email?: string;    // Or email for login
    password?: string; // Password is required
}

// For new user registration
export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string; // Aligns with User entity and database
    lastName?: string;  // Aligns with User entity and database
    roles?: string[];   // Optional: if frontend can suggest roles (backend should validate/override)
}

// For changing the current user's password
export interface PasswordUpdateRequest {
    currentPassword: string;
    newPassword: string;
}

// For an admin to create a new user (specifically an admin in this context)
export interface AdminUserCreateRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
    roles: string[]; // e.g., ['ROLE_ADMIN'] - Backend must validate this
}