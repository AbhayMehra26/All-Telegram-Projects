package com.example.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class UserState 

{
    private static final Map<Long, String> userStates = new HashMap<>();
    private static final Map<Long, UserData> tempUserData = new HashMap<>();

    public static void setUserState(Long chatId, String state)
    {
        userStates.put(chatId, state);
    }

    public static String getUserState(Long chatId) 
    {
        return userStates.get(chatId);
    }

    public static void setTempUserData(Long chatId, UserData userData) 
    {
        tempUserData.put(chatId, userData);
    }

    public static UserData getTempUserData(Long chatId) 
    {
        return tempUserData.get(chatId);
    }

    public static void clearUserState(Long chatId)
    {
        userStates.remove(chatId);
        tempUserData.remove(chatId);
    }
}
