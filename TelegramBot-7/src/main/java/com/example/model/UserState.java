package com.example.model;

public enum UserState 
{
    AWAITING_USERNAME, 
    AWAITING_PASSWORD, 
    AWAITING_EMAIL,
    AWAITING_PHONE,
    AWAITING_LOGIN_USERNAME,
    AWAITING_LOGIN_PASSWORD, 
    AWAITING_APPOINTMENT
}

/* ✅ When to Use enum?
 
✔ When you have a fixed set of values (like Days, States, Status Codes).
✔ When you want better readability compared to using int or String constants.
✔ When you want to prevent invalid values from being used.
✔ When you want additional properties associated with constants.
✔ When using switch-case statements for better structure.

❌ When NOT to Use enum?

❌ If the values change frequently (e.g., items in a database table).
❌ If you need dynamic values that can be modified at runtime.
❌ If you need a large number of constant values (can make the enum bloated).


*/ 